package it.unipi.ing.mobile.sleepmonitoring.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;


public abstract class SensorsManager implements SensorEventListener  {


    final protected Sensor rotation_vector;
    final protected Sensor accelerometer;
    final protected SensorManager sm;

    protected PrintWriter writer;


    public SensorsManager(Context context){
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
    }

    public void registerListeners() throws Exception {
//        if(!sm.registerListener(this, rotation_vector, SensorManager.SENSOR_DELAY_NORMAL))
        if(!sm.registerListener(this, rotation_vector, SensorManager.SENSOR_DELAY_GAME))
            throw new Exception("Unable to register to rotation vector sensor");
//        if(!sm.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL))
        if(!sm.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_GAME))
            throw new Exception("Unable to register to accelerometer sensor");
    }

    public void unregisterListeners(){
        sm.unregisterListener(this);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
