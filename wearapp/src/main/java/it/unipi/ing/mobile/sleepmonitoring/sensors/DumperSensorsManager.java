package it.unipi.ing.mobile.sleepmonitoring.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;

/**
 * this implementation will read data from sensors and dump them to a json file for future reuse
 */
public class DumperSensorsManager extends SensorsManager {

    public static final String FILE_NAME="/dataDump.json";
    final private String directory;

    public DumperSensorsManager(Context context){
        super(context);
        directory = context.getFilesDir().getPath();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (accelerometer.equals(event.sensor) ||rotation_vector.equals(event.sensor) ) try {
            JSONArray values = new JSONArray(event.values);
            JSONObject data = new JSONObject();
            data.put("values",values );
            data.put("timestamp", event.timestamp);
            data.put("sensor", event.sensor.getName());
            writer.println(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void registerListeners() throws Exception {
        super.registerListeners();
        writer = new PrintWriter(directory+FILE_NAME);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
