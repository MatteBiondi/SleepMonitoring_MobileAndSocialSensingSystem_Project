package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;
import java.io.InputStream;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.notification.Notification;

/**
 * The event from paired wearable app trigger the execution of the correspondent method defined in
 * in this class. The method is invoked only for those events thar are registered in the manifest file
 */

public class WearableListener extends WearableListenerService {
    private static InputStream data_stream = null;
    private static boolean running = false;
    private static Notification notification;

    public final String TAG = "WEARABLE_LISTENER";
    public final Integer NOTIFICATION_ID = 1000;

    // Used to update status
    public static boolean isRunning(){
        return running;
    }

    public static InputStream getDataStream() throws IOException {
        if (data_stream == null)
            throw new IOException("Stream is null");
        return data_stream;
    }

    @Override
    public void onChannelOpened(@NonNull ChannelClient.Channel channel) {
        if (!channel.getPath().equals(getString(R.string.start_endpoint))){
            Log.i(TAG, "Unexpected endpoint");
            return;
        }
        Wearable.getChannelClient(getApplicationContext()).getInputStream(channel).addOnCompleteListener(task -> {

            // Update UI, if activity is started
            Intent statusIntent = new Intent(getString(R.string.status_intent));
            statusIntent.putExtra("status",getString(R.string.status_running).toUpperCase());
            sendBroadcast(statusIntent);
            running = true;

            // Start foreground service
            Intent serviceIntent = new Intent(getApplicationContext(), WorkerService.class);
            serviceIntent.setAction(getString(R.string.start_action));
            data_stream = task.getResult();
            startForegroundService(serviceIntent);
        });
    }

    @Override
    public void onChannelClosed(@NonNull ChannelClient.Channel channel, int closeReason, int appSpecificErrorCode) {

        Log.i(TAG, "Channel closed: " + closeReason);

        // Update UI, if activity is started
        Intent statusIntent = new Intent(getString(R.string.status_intent));
        statusIntent.putExtra("status",getString(R.string.status_connected).toUpperCase());
        sendBroadcast(statusIntent);
        running = false;

        // Stop foreground service
        Intent serviceIntent = new Intent(this, WorkerService.class);
        serviceIntent.setAction(getString(R.string.stop_action));
        startForegroundService(serviceIntent);
        data_stream = null;
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.i("TAG", "Available nodes: " + capabilityInfo.getNodes().size());

        if (capabilityInfo.getNodes().size() == 0){

            // Update status
            Intent statusIntent = new Intent(getString(R.string.status_intent));
            statusIntent.putExtra("status",getString(R.string.status_disconnected).toUpperCase());
            sendBroadcast(statusIntent);

            if (running){
                // Stop foreground service
                Intent serviceIntent = new Intent(this, WorkerService.class);
                serviceIntent.setAction(getString(R.string.stop_action));
                startForegroundService(serviceIntent);
                data_stream = null;

                //Notify user
                notifyDisconnection(getApplicationContext());
            }
        }
        else{
            //Update status
            Intent statusIntent = new Intent(getString(R.string.status_intent));
            statusIntent.putExtra("status",getString(R.string.status_connected).toUpperCase());
            sendBroadcast(statusIntent);

            // Remove notification, if necessary
            if (running && notification != null){
                notification.destroy(getApplicationContext());
            }
        }
    }

    public void notifyDisconnection(Context context){
        notification = new Notification(
                getApplicationContext(),
                getString(R.string.notification_disconnection_title),
                getString(R.string.notification_disconnection_text),
                NOTIFICATION_ID,
                getString(R.string.notification_service_tag)
        );
        notification.launch(getApplicationContext());
    }
}