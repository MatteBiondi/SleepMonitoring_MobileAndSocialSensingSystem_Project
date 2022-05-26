package it.unipi.ing.mobile.sleepmonitoring;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import it.unipi.ing.mobile.sleepmonitoring.database.SleepEvent;
import it.unipi.ing.mobile.sleepmonitoring.database.SleepEventDatabase;
import it.unipi.ing.mobile.sleepmonitoring.notification.Notification;

public class WorkerService extends Service {
    public final String TAG = "WorkerService";
    public final int NOTIFICATION_ID = 2000;
    private Thread worker = null;

    private static boolean local = true; //TODO

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        // create new worker thread
        worker = new Thread(() -> {
            SleepEventDatabase sleepDB = SleepEventDatabase.build(getApplicationContext());

            // Start new session
            sleepDB.startSession();

            try (InputStream data_stream = WearableListener.getDataStream()) {
                // Get stream
                Scanner inputScanner = new Scanner(data_stream);

                String event = null;
                String timestamp;

                while (!worker.isInterrupted()) {
                    String line = inputScanner.nextLine();
                    timestamp = SleepEventDatabase.getCurrentTimestamp();

                    try {
                        JSONObject data = new JSONObject(line);
                        Log.i("consumer", "received values " + data);

                        if (local) { // Computation on mobile TODO
                            event = "micro"; // TODO: process raw data
                        } else { // Computation on smartwatch
                            event = data.getString("event");
                        }

                        if (event != null) {
                            sleepDB.insertSleepEvents(new SleepEvent(timestamp, event));
                        }
                        event = null;
                    }
                    catch (JSONException e){ Log.w(TAG, e.getMessage()); }
                }
            } catch (IOException | NoSuchElementException e) {
                e.printStackTrace();
            } finally {
                // Stop session
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
        if (intent.getAction().equals(getString(R.string.start_action))) {
            Log.i(TAG, "Foreground service started");

            // Define notification used by foreground service
            Notification notification = new Notification(
                    getApplicationContext(),
                    getString(R.string.notification_service_title),
                    getString(R.string.notification_service_text),
                    NOTIFICATION_ID,
                    null
            );

            // Start foreground mode
            startForeground(NOTIFICATION_ID, notification.get());

            // Do work on a background thread
            worker.start();
        }
        else if (intent.getAction().equals(getString(R.string.stop_action))) {
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