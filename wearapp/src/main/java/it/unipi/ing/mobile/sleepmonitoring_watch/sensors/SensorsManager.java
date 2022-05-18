package it.unipi.ing.mobile.sleepmonitoring_watch.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class SensorsManager implements SensorEventListener {
    final private Context context;
    final private Sensor rotation_vector;
    final private Sensor accelerometer;
    final private SensorManager sm;
    String directory;

    public PipedOutputStream getOutputStream() {
        return outputStream;
    }

    final protected PipedOutputStream outputStream=new PipedOutputStream();
    final protected PrintWriter writer = new PrintWriter(outputStream);
    public SensorsManager(Context context){
        this.context=context;
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> list = sm.getSensorList(Sensor.TYPE_ALL);
        for(Sensor x: list)
            Log.i("SM", "Type: " + x.getStringType() +
                    ", name: " + x.getName() +
                    ", vendor: " + x.getVendor() +
                    ", current (mA): " + x.getPower() +
                    ", is wakeup: " + x.isWakeUpSensor() +
                    ", FIFO: " + x.getFifoMaxEventCount()
            );
        rotation_vector = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        directory = context.getFilesDir().getPath();


    }

    private void testPipe() throws IOException {
        final PipedInputStream inputStream  = new PipedInputStream(outputStream);
        Thread testConsumer = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner r = new Scanner(inputStream);
                while (true) {
                    try {
                        JSONObject data= new JSONObject(r.nextLine());
                        Log.i("consumer", "received values " + data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        testConsumer.start();
    }

    public void registerListeners() throws Exception {
        if(!sm.registerListener(this, rotation_vector, SensorManager.SENSOR_DELAY_NORMAL))
            throw new Exception("Unable to register to rotation vector sensor");
        if(!sm.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL))
            throw new Exception("Unable to register to accelerometer sensor");
        testPipe();
    }

    public void unregisterListeners(){
        sm.unregisterListener(this);
    }


    private long acc_batch_start=0;
    private long rot_batch_start=0;
    private JSONArray acc_batch= new JSONArray();
    private JSONArray rot_batch= new JSONArray();
    //private int delay=10;
    @Override
    public void onSensorChanged(SensorEvent event) {
        /*if(delay>0) {
            delay--;
            return;
        }*/
        JSONArray data=null;

        if (accelerometer.equals(event.sensor)){
            try {
                data = new JSONArray(event.values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("accelerometer", "received values "+data);
            if(event.timestamp-acc_batch_start>1000000000 ){
                try {
                    if(acc_batch_start!=0)
                        writer.println(new JSONObject().
                                put("values",acc_batch).
                                put("type", "acc"));
                    acc_batch=new JSONArray();
                    acc_batch_start=event.timestamp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            acc_batch.put(data);
        }
        if (rotation_vector.equals(event.sensor)) {
            final float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientationAngle = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationAngle);
            Log.d("rotation_vector", "received values " +
                    new JSONArray(Collections.singletonList(event.values)));
            Log.v("rotation_matrix", "received values " +
                    new JSONArray(Collections.singletonList(rotationMatrix)));
            try {
                data = new JSONArray(orientationAngle);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("orientationAngle", "received values " + data);

            if (event.timestamp - rot_batch_start > 1000000000) {
                try {
                    if(rot_batch_start!=0) {
                        writer.println(new JSONObject().
                                put("values",rot_batch).
                                put("type", "rot"));
                        Log.i("sender", rot_batch.toString());
                    }
                    rot_batch = new JSONArray();
                    rot_batch_start = event.timestamp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            rot_batch.put(data);

        }
        //delay=10;
        // same for every sensor
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
