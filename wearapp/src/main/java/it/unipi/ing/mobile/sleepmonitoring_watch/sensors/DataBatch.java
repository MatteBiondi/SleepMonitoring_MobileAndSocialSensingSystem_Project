package it.unipi.ing.mobile.sleepmonitoring_watch.sensors;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataBatch {
    private String sensorName;
    private JSONArray data;

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    private long startTime;

    public JSONArray getData() {
        return data;
    }





    public DataBatch(String sensorName, long startTime) {
        this.sensorName = sensorName;
        this.data = new JSONArray();
        this.startTime = startTime;
    }

    public JSONObject getJson() throws JSONException {
        return new JSONObject().
                put("values",data).
                put("type", sensorName);
    }
}
