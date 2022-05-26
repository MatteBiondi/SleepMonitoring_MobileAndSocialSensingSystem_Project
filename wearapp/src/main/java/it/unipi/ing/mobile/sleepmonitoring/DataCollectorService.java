package it.unipi.ing.mobile.sleepmonitoring;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import it.unipi.ing.mobile.sleepmonitoring.sensors.SensorsManager;


public class DataCollectorService extends Service {
    public final String TAG = "DATA_COLLECTOR";
    private SensorsManager sensor_manager;

    public void onCreate(){
        super.onCreate();
        this.sensor_manager = new SensorsManager(getApplicationContext());

        try {
            sensor_manager.registerListeners(MainActivity.getStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        sensor_manager.unregisterListeners();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(getString(R.string.start_service))) {
            Log.i(TAG, "Foreground service started");

            // Define notification used by foreground service
            String NOTIFICATION_CHANNEL_ID = getString(R.string.channel_id);
            String channelName = getString(R.string.channel_name);
            int NOTIFICATION_ID = 3000;

            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    this,
                    NOTIFICATION_CHANNEL_ID
            );

            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle(getString(R.string.notification_title))
                    .build();

            startForeground(NOTIFICATION_ID, notification);
        }
        else if (intent.getAction().equals(getString(R.string.stop_service))) {
            Log.i(TAG, "Foreground service stopped");
            stopForeground(true);
            stopSelfResult(startId);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
