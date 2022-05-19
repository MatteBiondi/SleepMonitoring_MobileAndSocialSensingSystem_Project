package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.ChannelClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;
import java.io.InputStream;

/**
 * The event from paired wearable app trigger the execution of the correspondent method defined in
 * in this class. The method is invoked only for those events thar are defined in the manifest file
 */

public class WearableListener extends WearableListenerService {
    private static boolean running = false;
    private static InputStream data_stream = null;

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
        Wearable.getChannelClient(getApplicationContext()).getInputStream(channel).addOnCompleteListener(task -> {

            // Update UI, if activity is started
            Intent statusIntent = new Intent("it.unipi.ing.mobile.sleepmonitoring_smartphone.RUNNING");
            sendBroadcast(statusIntent);
            running = true;

            // Start foreground service
            Intent serviceIntent = new Intent(getApplicationContext(), WorkerService.class);
            serviceIntent.setAction("/start-service");
            data_stream = task.getResult();
            startForegroundService(serviceIntent);
        });
    }

    @Override
    public void onChannelClosed(@NonNull ChannelClient.Channel channel, int i, int i1) {

        // Update UI, if activity is started
        Intent startIntent = new Intent("it.unipi.ing.mobile.sleepmonitoring_smartphone.RUNNING");
        sendBroadcast(startIntent);
        running = false;

        // Stop foreground service
        Intent serviceIntent = new Intent(this, WorkerService.class);
        serviceIntent.setAction("/stop-service");
        startForegroundService(serviceIntent);
        data_stream = null;
    }
}