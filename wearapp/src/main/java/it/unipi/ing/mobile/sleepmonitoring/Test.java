package it.unipi.ing.mobile.sleepmonitoring;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Test extends Thread {

    final  Scanner inputScanner;
    private final OutputStream outStream;

    public Test(InputStream inStream, OutputStream outStream) {
        inputScanner = new Scanner(inStream);
        this.outStream = outStream;
    }

    @Override
    public void run() {
        while (!isInterrupted()) try {//TODO close input stream
            JSONObject data = new JSONObject(inputScanner.nextLine());
            Log.i("consumer", "received values " + data);
            //TODO process data
            //TODO send events to mobile using outStream
        } catch (JSONException | NoSuchElementException e) {
            e.printStackTrace();
        }
    }
}
