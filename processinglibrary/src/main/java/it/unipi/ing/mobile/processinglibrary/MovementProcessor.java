package it.unipi.ing.mobile.processinglibrary;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.*;
import java.lang.System.*;


public class MovementProcessor {

    /*
     * Data taken from "SleepGuard: Capturing Rich Sleep Information Using Smartwatch
     * Sensing Data" (Chang et al.), section 2.3
     * */

    private final Float NOISE_THRESHOLD = .8f;


    //private float MOVEMENT_PEAK_THRESHOLD_LOW = 1f;
    //private float MOVEMENT_PEAK_THRESHOLD_HIGH = 1.5f;


    private final float SHORT_MOVEMENT_LOW = .5f;
    private final float SHORT_MOVEMENT_HIGH = 1.2f;


    private float LONG_MOVEMENT_LOW = SHORT_MOVEMENT_HIGH;
    private float LONG_MOVEMENT_HIGH = 2.2f;


    private long n_samples=0;

    private long current_window=-1;
    private boolean currentlyInMovement;
    private float lastMovementPeak;
    private float lastMovementStartTime;
    private float lastMovementDuration;


    enum Movement{
         NO_MOVEMENT("No_movement"),
         MICRO("micro"),
         MACRO("macro");

         private final String movement;

         Movement(String movement) {
             this.movement = movement;
         }

        @NonNull
        @Override
        public String toString(){
            return this.movement.toLowerCase();
        }
    }

    public MovementProcessor(){
        currentlyInMovement = false;
        lastMovementPeak = -1;
        lastMovementStartTime = -1;
        lastMovementDuration = -1;
    }

    /**
     * Classify the type of movement based on the duration and its acceleration peak
     * @param duration Duration of the movement as read from the accelerometer data
     * @param peak Maximum peak as read from the accelerometer data
     * @return A Movement enum type among no_movement, body_trembling, hand_movement and arm_rising
     */
    private Movement getMovementFromDurationAndPeak(Float duration, Float peak){

        Log.i("movementProcessor:getMovement", "msTime"+n_samples*20+" Duration: " + duration.toString() + "; peak: " + peak.toString());

        Movement ret = Movement.NO_MOVEMENT;
        if(duration == -1  && peak == -1)
            return ret;

        if(duration > SHORT_MOVEMENT_LOW && duration < SHORT_MOVEMENT_HIGH) {
            ret = Movement.MICRO;
        }

       if(duration > LONG_MOVEMENT_LOW && duration < LONG_MOVEMENT_HIGH) {
                ret = Movement.MACRO;
       }

        return ret;
    }

    /**
     * Detect the type of movement based on the acceleration sensor data
     * @param accelerationData JSON
     * @return
     */
    public Movement detectMovement(JSONObject accelerationData) {
        current_window+=1;
        Movement movement = Movement.NO_MOVEMENT;

        JSONArray values = null;
        try {
            values = accelerationData.getJSONArray("values");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // iterate through the whole window to check for spikes
        boolean window_has_peak=false;
        if (values != null) {
            n_samples+=values.length()*(current_window%2);
            for (int i = 1; i < values.length(); i++) {
                Float derivative = null;
                try {
                    Float[] current_array = new Float[3];
                    current_array[0] = (float)values.getJSONArray(i).getDouble(0);
                    current_array[1] = (float)values.getJSONArray(i).getDouble(1);
                    current_array[2] = (float)values.getJSONArray(i).getDouble(2);

                    Float[] previous_array = new Float[3];
                    previous_array[0] = (float)values.getJSONArray(i-1).getDouble(0);
                    previous_array[1] = (float)values.getJSONArray(i-1).getDouble(1);
                    previous_array[2] = (float)values.getJSONArray(i-1).getDouble(2);

                    derivative = Util.getRSS(current_array) - Util.getRSS(previous_array);
                    derivative = Math.abs(derivative);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Movement processor", "derivative "+derivative.toString());
                if(currentlyInMovement){
                    if (derivative > NOISE_THRESHOLD) {
                        window_has_peak=true;
                        if(derivative > lastMovementPeak)
                            lastMovementPeak = derivative;
                    }
                }else if (derivative > NOISE_THRESHOLD) {
                        currentlyInMovement = true;
                        window_has_peak=true;
                        lastMovementStartTime = (0.5f*current_window) + ((float)i / values.length());
                        lastMovementPeak=derivative;
                        Log.v("Movement processor", "Threshold exceeded. Movement detected");
                    }
            }
        }
        if(currentlyInMovement && !window_has_peak){
            lastMovementDuration = (0.5f * current_window) - lastMovementStartTime;
            currentlyInMovement = false;
            movement = getMovementFromDurationAndPeak(lastMovementDuration, lastMovementPeak);
        }else
            movement = Movement.NO_MOVEMENT;
        Log.i("Movement processor", "Returning movement: " + movement.toString());
        return movement;
    }

}
