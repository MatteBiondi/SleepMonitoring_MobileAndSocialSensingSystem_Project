package it.unipi.ing.mobile.sleepmonitoring;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Wearable;

import it.unipi.ing.mobile.sleepmonitoring.communication.StreamChannel;
import it.unipi.ing.mobile.sleepmonitoring.sensors.DumperSensorsManager;
import it.unipi.ing.mobile.sleepmonitoring.sensors.FromFileSensorsManager;
import it.unipi.ing.mobile.sleepmonitoring.sensors.OnlineSensorsManager;
import it.unipi.ing.mobile.sleepmonitoring.sensors.SensorsManager;


public class DataCollectorService extends Service implements  CapabilityClient.OnCapabilityChangedListener{
    private static boolean running = false;
    private static String paired_node_id = null;
    private static Worker worker = null;
    private static StreamChannel stream_channel = null;

    public static boolean isRunning(){
        return running;
    }

    public static String getPairedNodeId(){
        return paired_node_id;
    }

    private SensorsManager sensor_manager = null;
    public final String TAG = "DATA_COLLECTOR";

    public void onCreate(){
        super.onCreate();
        try {
            // TODO change functionality
//            this.sensor_manager = new OnlineSensorsManager(getApplicationContext(), MainActivity.getInstance().getStream());
            this.sensor_manager = new FromFileSensorsManager(getApplicationContext(), MainActivity.getInstance().getStream());
            Log.d("DataCollector:onCreate", "Reading sensor data from file");
            //            this.sensor_manager = new DumperSensorsManager(getApplicationContext());
            sensor_manager.registerListeners();

            // Register capabilities listener
            Wearable.getCapabilityClient(getApplicationContext()).addListener(
                    this, getString(R.string.mobile_capability));

            running = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            sensor_manager = null;
            MainActivity.getInstance().finishAndRemoveTask();
            Toast.makeText(getApplicationContext(), getString(R.string.missing_sensor), Toast.LENGTH_SHORT).show();
            selfStop();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        running = false;
        paired_node_id = null;

        // Stop worker if active
        if (worker != null)
            worker.interrupt();
        worker = null;

        // Close communication channel and related streams
        if (stream_channel != null)
            stream_channel.close();

        // Unregister capabilities listener
        Wearable.getCapabilityClient(getApplicationContext()).removeListener(
                this, getString(R.string.mobile_capability));

        if (sensor_manager != null)
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

            // Available from Android 8 (API 26)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH
                );

                NotificationManager manager = (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    this,
                    NOTIFICATION_CHANNEL_ID
            );

            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle(getString(R.string.notification_title))
                    .build();

            // Save instances
            paired_node_id = intent.getStringExtra("paired_node_id");
            worker = MainActivity.getInstance().getWorker();
            stream_channel = MainActivity.getInstance().getChannel();

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

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        selfStop();
    }

    private void selfStop(){
        // If capability changes no nodes are connected to watch, then stop foreground service
        Intent stopServiceIntent = new Intent(this, DataCollectorService.class);
        stopServiceIntent.setAction(getString(R.string.stop_service));

        // Available from Android 8 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(stopServiceIntent);
        }
        else {// Android API < 26
            startService(stopServiceIntent);
        }
    }
}
