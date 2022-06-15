package it.unipi.ing.mobile.sleepmonitoring.sensors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * this class represent a data batch to be sent out when full
 */
public class DataBatch {
    private final String sensorName;
    private final JSONArray data;
    private final long startTime; // timestamp of the first sample of the batch


    public long getStartTime() {
        return startTime;
    }

    public JSONArray getData() {
        return data;
    }

    public DataBatch(String sensorName, long startTime) {
        this.sensorName = sensorName;
        this.data = new JSONArray();
        this.startTime = startTime;
    }

    /**
     *
     * @return jsonObject containing the batch
     * @throws JSONException is something goes wrong
     */
    public JSONObject getJson() throws JSONException {
        return new JSONObject().
                put("values",data).
                put("type", sensorName);
    }
}
