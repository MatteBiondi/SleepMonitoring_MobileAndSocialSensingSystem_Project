package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.database.SleepEvent;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.database.SleepEventDatabase;

public class WorkerService extends Service {
    public final String TAG = "WorkerService";
    public final String CHANNEL_ID = "WorkerServiceChannel";
    public final int NOTIFICATION_ID = 2000;
    private Thread worker = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        // create new worker thread
        worker = new Thread(() -> {
            SleepEventDatabase sleepDB = SleepEventDatabase.build(getApplicationContext());
            sleepDB.startSession();
            try {
                // Get stream
                InputStream data_stream = WearableListener.getDataStream();
                Scanner inputScanner = new Scanner(data_stream);

                while(!worker.isInterrupted()){
                    JSONObject data = new JSONObject(inputScanner.nextLine());
                    Log.i("consumer", "received values " + data);

                    // TODO: process if necessary

                    String timestamp = ""; // TODO
                    String event = ""; // TODO

                    sleepDB.insertSleepEvents(new SleepEvent(timestamp, event));
                }
                // Close stream
                data_stream.close();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            finally {
                sleepDB.stopSession();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");

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
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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