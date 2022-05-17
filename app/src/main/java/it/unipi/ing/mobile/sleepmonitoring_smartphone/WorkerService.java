package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.bluetooth.BluetoothBroadcastReceiver;

public class WorkerService extends Service {
    public final String TAG = "WorkerService";
    public final String CHANNEL_ID = "WorkerServiceChannel";
    public final int NOTIFICATION_ID = 2000;

    private BluetoothBroadcastReceiver bluetooth_receiver = null;
    private Thread worker = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate");

        // register broadcast listener for bluetooth status
        bluetooth_receiver = new BluetoothBroadcastReceiver();
        registerReceiver(bluetooth_receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // create new worker thread
        worker = new Thread(() -> {
           // TODO: read data from wearable, process if necessary, store into db
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");

        // unregister broadcast listener for bluetooth status
        if (bluetooth_receiver != null){
            unregisterReceiver(bluetooth_receiver);
        }

        // stop worker thread
        if (worker != null){
            worker.interrupt();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals("/start-service")) {
            Log.i(TAG, "Foreground service started");

            // Define notification channel
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "ForegroundServiceChannel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);

            // Define pending intent
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            // Create new notification
            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle("Sleep Monitoring")
                    .setContentText("Sleep monitoring running")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .build();

            // Start service
            startForeground(NOTIFICATION_ID, notification);

            // Do work on a background thread
            worker.start();
        }
        else if (intent.getAction().equals("/stop-service")) {
            Log.i(TAG, "Foreground service stopped");
            stopForeground(true);
            stopSelfResult(startId);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}