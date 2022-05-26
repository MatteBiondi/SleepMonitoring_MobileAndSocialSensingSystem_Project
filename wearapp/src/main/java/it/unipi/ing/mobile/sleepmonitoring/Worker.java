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

    final  Scanner inputScanner;
    private PrintWriter printWriter;
    private DataProcessor dataProcessor;

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
                }
            } catch (JSONException | NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        inputScanner.close();
    }
}
