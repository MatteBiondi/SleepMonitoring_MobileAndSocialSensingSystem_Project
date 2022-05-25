package it.unipi.ing.mobile.sleepmonitoring.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import it.unipi.ing.mobile.sleepmonitoring.R;

public class Notification {
    private final Integer ID;
    private final String TAG;
    private final android.app.Notification notification;

    public Notification(Context context, String title, String text, Integer id, String tag){
        this.ID = id;
        this.TAG = tag;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel
        NotificationChannel notificationChannel = new NotificationChannel(
                context.getString(R.string.notification_channel_id),
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );

        // Configure the notification channel.
        notificationChannel.setVibrationPattern(new long[]{100, 500});
        notificationChannel.enableVibration(true);

        notificationManager.createNotificationChannel(notificationChannel);

        // Set intent used to send notification
        Intent notificationIntent = new Intent();
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent intent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Define new notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
               context,
               context.getString(R.string.notification_channel_id)
        );

        // Set notification attributes
        notificationBuilder
                .setAutoCancel(true)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(intent);

        notification = notificationBuilder.build();
    }

    public android.app.Notification get(){
        return notification;
    }

    public void launch(Context context){
        // Send notification using intent
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(TAG, ID, notification);
    }

    public void destroy(Context context){
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(TAG, ID);
    }
}
