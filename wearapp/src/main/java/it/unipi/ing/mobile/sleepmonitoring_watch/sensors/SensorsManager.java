package it.unipi.ing.mobile.sleepmonitoring_watch.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;

import java.util.Arrays;


public class SensorsManager implements SensorEventListener {
    final private Context context;
    final private Sensor rotation_vector;
    final private Sensor accelerometer;
    final private SensorManager sm;

    public SensorsManager(Context context){
        this.context=context;
        sm = (SensorManager) context.getSystemService(this.context.SENSOR_SERVICE);
        rotation_vector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerometer = sm.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));

    }

    public void registerListeners() throws Exception {
        if(!sm.registerListener(this, rotation_vector, SensorManager.SENSOR_DELAY_NORMAL))
            throw new Exception("Unable to register to rotation vector sensor");
        if(!sm.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL))
            throw new Exception("Unable to register to accelerometer sensor");
    }

    public void unregisterListeners(){
        sm.unregisterListener(this);
    }


    //private int delay=10;
    @Override
    public void onSensorChanged(SensorEvent event) {
        /**if(delay>0) {
            delay--;
            return;
        }**/
        if (accelerometer.equals(event.sensor)){
            Log.d("accelerometer", "received values "+
                    new JSONArray(Arrays.asList(event.values)));
        }

        if (rotation_vector.equals(event.sensor)){
            final float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientationAngle = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationAngle);
            /**
            Log.d("rotation_vector","received values "+
                    new JSONArray(Arrays.asList(event.values)));
            Log.d("rotation_matrix","received values "+
                    //new JSONArray(Arrays.asList(rotationMatrix)));
             **/
            Log.d("orientationAngle","received values "+
                    new JSONArray(Arrays.asList(orientationAngle)));
        }
        //delay=10;
        // same for every sensor
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
