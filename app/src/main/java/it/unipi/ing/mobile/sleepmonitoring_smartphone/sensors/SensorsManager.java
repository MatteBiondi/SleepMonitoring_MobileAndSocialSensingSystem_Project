package it.unipi.ing.mobile.sleepmonitoring_smartphone.sensors;

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
        Sensor gyroscope = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensors.put("gyroscope", gyroscope);
        // ...
        // add other sensors

        for (Sensor s : sensors.values()) { // register this class as listener
            if(!sm.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME))
                throw new Exception("Unable to register to Sensor: " + s.getName());
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensors.get("gyroscope").equals(sensorEvent.sensor)){
            Log.i("SENSORS", "received values "+sensorEvent.values.toString());
        }

        // same for every sensor
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
