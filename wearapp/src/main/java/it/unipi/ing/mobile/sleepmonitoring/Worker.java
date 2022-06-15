package it.unipi.ing.mobile.sleepmonitoring;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import it.unipi.ing.mobile.processinglibrary.DataProcessor;

public class Worker extends Thread {
    public final String TAG = "Worker";
    final  Scanner inputScanner;
    private final PrintWriter printWriter;
    private final DataProcessor dataProcessor;

    /**
     *
     * @param inStream stream with main thread to receive data from sensors
     * @param outStream stream with mobile app to send final values
     */
    public Worker(InputStream inStream, OutputStream outStream) {
        inputScanner = new Scanner(inStream);
        printWriter = new PrintWriter(outStream);

        dataProcessor = new DataProcessor();
    }

    @Override
    public void run() {
        String event;

        while (!isInterrupted()){
            try {
                JSONObject data = new JSONObject(inputScanner.nextLine());
                Log.i("consumer", "received values " + data);

                event = dataProcessor.getEventFromSensorData(data);
                if(event != null){
                    JSONObject eventObject = new JSONObject();
                    eventObject.put("event", event);

                    printWriter.println(eventObject);
                    printWriter.flush();
                }
            } catch (JSONException | NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        // Close piped stream with main thread (output stream is closed with the channel)
        inputScanner.close();

        Log.i(TAG, "Stopped");
    }
}
