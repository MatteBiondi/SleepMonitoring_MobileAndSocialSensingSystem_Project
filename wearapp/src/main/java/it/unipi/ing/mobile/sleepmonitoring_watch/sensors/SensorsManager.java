package it.unipi.ing.mobile.sleepmonitoring_watch.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class SensorsManager implements SensorEventListener {
    final private Context context;
    final private Map<String,Sensor> sensors=new HashMap<String, Sensor>();
    final private SensorManager sm;

    public SensorsManager(Context context) throws Exception {
        this.context=context;
        sm = (SensorManager) context.getSystemService(this.context.SENSOR_SERVICE);
        Sensor rotation_vector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor accelerometer = sm.getDefaultSensor((Sensor.TYPE_ACCELEROMETER));

        sensors.put("rotation_vector", rotation_vector);
        sensors.put("accelerometer", accelerometer);
        // ...
        // add other sensors

        for (Sensor s : sensors.values()) { // register this class as listener
            if(!sm.registerListener(this, s, 1000000))
                throw new Exception("Unable to register to Sensor: " + s.getName());
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensors.get("accelerometer").equals(event.sensor)){
            Log.d("accelerometer", String.format("received values %.2f %.2f %.2f",
                    event.values[0], event.values[1], event.values[2]));
        }

        if (sensors.get("rotation_vector").equals(event.sensor)){
            Log.d("rotation_vector", String.format("received values %.2f %.2f %.2f",
                    event.values[0], event.values[1], event.values[2]));
        }

        // same for every sensor
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
