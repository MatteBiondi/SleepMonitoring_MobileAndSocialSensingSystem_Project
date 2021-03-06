package it.unipi.ing.mobile.sleepmonitoring.sensors;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;


/**
 * this implementation will use data coming from a json file instead of those coming sensors
 */
public class FromFileSensorsManager extends OnlineSensorsManager{

    protected final Scanner reader;
    public static final String FILE_NAME="/federico_2.json";

    public FromFileSensorsManager(Context context,OutputStream outStream ) throws IOException {
        super(context, outStream);
        String directory = context.getFilesDir().getPath();
        this.reader=new Scanner(
            new File(directory +FILE_NAME));
    }

    @Override
    public void registerListeners() throws Exception {
        super.registerListeners();
        //this.writer=new PrintWriter("/data/data/it.unipi.ing.mobile.sleepmonitoring/files/window.json");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.d("FromFileSensorManager:onSensorChanged", "Reading");
        if(!reader.hasNextLine()){
            Log.w("FILE_READER", "sensors data file ended");
            this.unregisterListeners();
            return;
        }
        try {
            JSONObject data =new JSONObject(reader.nextLine());
            Log.v("FILE_READER", data.toString());
            if(data.getString("sensor").equals("LSM6DS3 Accelerometer"))
                accelerometerHandler(
                        JSONArrayToArray(
                                data.getJSONArray("values")),
                        data.getLong("timestamp"));
            if(data.getString("sensor").equals("Game Rotation Vector"))
                rotationHandler(
                        JSONArrayToArray(
                                data.getJSONArray("values")),
                        data.getLong("timestamp"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregisterListeners(){
        super.unregisterListeners();
        reader.close();
    }
    /**
     *
     * @param ja a jsonArray of float/double
     * @return an array of float
     * @throws JSONException if the jsonArray is not convertible to array of float
     */
    private float[] JSONArrayToArray(JSONArray ja) throws JSONException {
        float[]fa=new float[ja.length()];
        for (int i = 0; i < ja.length(); i++) {
            fa[i]=(float)ja.getDouble(i);
        }
        return fa;

    }
}
