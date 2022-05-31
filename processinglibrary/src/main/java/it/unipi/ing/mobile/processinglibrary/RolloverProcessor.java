package it.unipi.ing.mobile.processinglibrary;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class RolloverProcessor {

    private final Float DISTANCE_THRESHOLD = 90.0f;
    private final Float STABILITY_THRESHOLD = 180*180*0.01f;
    private final Float[] ANGLE_WRAP_VALUE = {180.0f, 180.0f, 180.0f};

    private boolean firstStablePostureReached;
    private Float[] lastStablePosture;
    private Float[] currentStablePosture;
    private int lastStableWindowIndex;
    private int currentWindowIndex;

    public RolloverProcessor() {
        firstStablePostureReached = false;
        lastStablePosture = new Float[3];
        currentStablePosture = new Float[3];
        currentWindowIndex = 0;
        lastStableWindowIndex = -1;
    }


    private boolean isPostureStable(Float[] XData, Float[] YData, Float[] ZData){


        Float heightX = Util.getMax(XData) - Util.getMin(XData);
        Float heightY = Util.getMax(YData) - Util.getMin(YData);
        Float heightZ = Util.getMax(ZData) - Util.getMin(ZData);

        if(heightX > STABILITY_THRESHOLD || heightY > STABILITY_THRESHOLD || heightZ > STABILITY_THRESHOLD)
            return false;
        else
            return true;
    }

    /**
     * Detect if a rollover has occurred by analyzing data from the rotation sensor
     * @param rotationData
     * @return true if a rollover has occurred, false otherwise
     */
    public boolean hasRolloverOccurred(JSONObject rotationData){

        Log.d("RolloverProc:hasRolloverOccurred", "ROT_WINDOW: " + rotationData.toString());
        ArrayList<Float> XData = new ArrayList<Float>();
        ArrayList<Float> YData = new ArrayList<Float>();
        ArrayList<Float> ZData = new ArrayList<Float>();

        JSONArray values = null;
        try {
            values = rotationData.getJSONArray("values");

            for (int i = 0; i < values.length(); i++) {
                // TODO: 27/05/2022 remove magic number 
                XData.add(180*(float)values.getJSONArray(i).getDouble(0));
                YData.add(180*(float)values.getJSONArray(i).getDouble(1));
                ZData.add(180*(float)values.getJSONArray(i).getDouble(2));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean ret = false;

        Float[] Xarray = XData.toArray(new Float[0]);
        Float[] Yarray = YData.toArray(new Float[0]);
        Float[] Zarray = ZData.toArray(new Float[0]);

        if(isPostureStable(Xarray, Yarray, Zarray)){

            currentStablePosture[0] = Util.getAverage(Xarray);
            currentStablePosture[1] = Util.getAverage(Yarray);
            currentStablePosture[2] = Util.getAverage(Zarray);

            // if it is the first measurement, we do not detect a rollover regardless
            if(!firstStablePostureReached){
                firstStablePostureReached = true;
                for (int i = 0; i < 3; i++) {
                    lastStablePosture[i] = currentStablePosture[i];
                }
            }
            else{
                Float postureDistance = new Float(0);
                postureDistance = Util.getEuclideanDistanceWithWrap(currentStablePosture, lastStablePosture, ANGLE_WRAP_VALUE);


                // if rollover happened
                for (int i = 0; i < 3; i++) {
                    lastStablePosture[i] = currentStablePosture[i];
                }
                if(postureDistance > DISTANCE_THRESHOLD){
                    Log.i("RolloverProcessor:hasRolloverOccurred", "Threshold exceeded: " + postureDistance.toString());
                    ret = true;
                    Log.d("RolloverProcessor", "rolloverOccurred is TRUE inside window n. " +
                            Integer.toString(currentWindowIndex) + " with respect to " + Integer.toString(lastStableWindowIndex));
                    lastStableWindowIndex = currentWindowIndex;
                }
            }
        }

        if(!ret){
        Log.d("RolloverProcessor", "rolloverOccurred is FALSE");
        }
        currentWindowIndex++;
        return ret;
    }
}
