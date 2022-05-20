package it.unipi.ing.mobile.sleepmonitoring_watch;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

public class Test extends Thread {

    final  Scanner inputScanner;
    public Test(InputStream inStream) {
        inputScanner = new Scanner(inStream);
    }

    @Override
    public void run() {
        while (!isInterrupted()) try {
            JSONObject data = new JSONObject(inputScanner.nextLine());
            Log.i("consumer", "received values " + data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
