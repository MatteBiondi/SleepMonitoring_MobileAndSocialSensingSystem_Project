package it.unipi.ing.mobile.sleepmonitoring_smartphone.bluetooth;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "BLUETOOTH_BRC_RECEIVER";
    public static final Integer NOTIFICATION_ID = 1000;
    public static final String NOTIFICATION_TAG = "BLUETOOTH_BRD_RECEIVER";
    public static final String NOTIFICATION_CHANNEL_ID = "sleep_monitoring_channel";

    public BluetoothBroadcastReceiver(){ }

    public void NotifyBluetoothOff(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel
        // TODO: notification channel
        /*NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "SleepMonitor",
                NotificationManager.IMPORTANCE_HIGH
        );

        // Configure the notification channel.
        notificationChannel.setDescription("SleepMonitor BLE status");
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
                .setContentTitle(context.getString(R.string.bluetooth_notification_title))
                .setContentText(context.getString(R.string.bluetooth_notification_text))
                .setContentInfo("Info");

        // Send notification using intent
        Intent notificationIntent = new Intent();
        notificationIntent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        notificationBuilder.setContentIntent(intent);

        Notification notification = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notification);*/
    }

    public void destroyNotification(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
            );

            switch(state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.i(TAG,"Bluetooth off");
                    NotifyBluetoothOff(context);
                    break;
                case BluetoothAdapter.STATE_ON:
                    destroyNotification(context);
                    break;
                default:
                    break;
            }
        }
    }
}