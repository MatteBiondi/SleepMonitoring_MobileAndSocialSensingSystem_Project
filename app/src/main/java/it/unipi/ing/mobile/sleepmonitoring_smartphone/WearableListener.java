package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.wearable.CapabilityInfo;
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
    public final Integer NOTIFICATION_ID = 1000;
    public final String NOTIFICATION_TAG = "WearableListener";
    public final String NOTIFICATION_CHANNEL_ID = "sleep_monitoring_channel";
    public final String STATUS_INTENT = "it.unipi.ing.mobile.sleepmonitoring_smartphone.RUNNING";
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
            Intent statusIntent = new Intent(STATUS_INTENT);
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
    public void onChannelClosed(@NonNull ChannelClient.Channel channel, int closeReason, int appSpecificErrorCode) {

        // Update UI, if activity is started
        Intent startIntent = new Intent(STATUS_INTENT);
        sendBroadcast(startIntent);
        running = false;

        // Stop foreground service
        Intent serviceIntent = new Intent(this, WorkerService.class);
        serviceIntent.setAction("/stop-service");
        startForegroundService(serviceIntent);
        data_stream = null;
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Log.i("TAG", "Available nodes: " + capabilityInfo.getNodes().size());
        if (capabilityInfo.getNodes().size() == 0 && running){
            // Update UI, if activity is started
            Intent startIntent = new Intent(STATUS_INTENT);
            sendBroadcast(startIntent);
            running = false;

            // Stop foreground service
            Intent serviceIntent = new Intent(this, WorkerService.class);
            serviceIntent.setAction("/stop-service");
            startForegroundService(serviceIntent);
            data_stream = null;

            //Notify user
            notifyDisconnection(getApplicationContext());
        }
        else {
            //TODO: correct ?
            destroyNotification(getApplicationContext());
        }
    }

    public void notifyDisconnection(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel
        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "SleepMonitor",
                NotificationManager.IMPORTANCE_HIGH
        );

        // Configure the notification channel.
        notificationChannel.setDescription("SleepMonitor");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setVibrationPattern(new long[]{100, 500});
        notificationChannel.enableVibration(true);

        notificationManager.createNotificationChannel(notificationChannel);

        // Define new notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context,
                NOTIFICATION_CHANNEL_ID
        );

        // Set notification attributes
        notificationBuilder
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setTicker("Hearty365")
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setContentInfo("Info");

        // Send notification using intent
        Intent notificationIntent = new Intent();
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.setContentIntent(intent);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);
    }

    public void destroyNotification(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
    }
}