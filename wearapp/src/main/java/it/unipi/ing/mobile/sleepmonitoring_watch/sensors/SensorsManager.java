package it.unipi.ing.mobile.sleepmonitoring_watch.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;


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

    public SensorsManager(Context context){
        this.context=context;
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotation_vector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerometer = sm.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));
        directory = context.getFilesDir().getPath();


    }

    private void testPipe() throws IOException {
        final PipedInputStream inputStream  = new PipedInputStream(outputStream);
        Thread testConsumer = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[100];
                while (true) {
                    try {
                        int len=inputStream.read(buffer, 0, 100);
                        String data = new String(
                                buffer);
                        Log.i("consumer", "received values " + data);
                    } catch (IOException e) {
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
        //testPipe();
    }

    public void unregisterListeners(){
        sm.unregisterListener(this);
    }


    //private int delay=10;
    @Override
    public void onSensorChanged(SensorEvent event) {
        /*if(delay>0) {
            delay--;
            return;
        }*/
        JSONArray data;
        JSONObject json = new JSONObject();

        if (accelerometer.equals(event.sensor)){
            data=new JSONArray(Collections.singletonList(event.values));
            Log.d("accelerometer", "received values "+data);
            try {
                outputStream.write(data.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (rotation_vector.equals(event.sensor)){
            final float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientationAngle = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationAngle);
            Log.d("rotation_vector","received values "+
                    new JSONArray(Arrays.asList(event.values)));
            Log.v("rotation_matrix","received values "+
                    new JSONArray(Arrays.asList(rotationMatrix)));
            data=new JSONArray(Arrays.asList(orientationAngle));
            Log.v("orientationAngle","received values "+data);
            try {
                outputStream.write(data.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //delay=10;
        // same for every sensor
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
