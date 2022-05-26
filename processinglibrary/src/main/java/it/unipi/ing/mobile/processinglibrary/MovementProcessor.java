package it.unipi.ing.mobile.processinglibrary;

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
    private Float NOISE_THRESHOLD = 0.03f;

    private float MOVEMENT_PEAK_THRESHOLD_LOW = 1.0f;
    private float MOVEMENT_PEAK_THRESHOLD_HIGH = 1.5f;

    private long SHORT_MOVEMENT_LOW = 800;
    private long SHORT_MOVEMENT_HIGH = 1200;

    private long LONG_MOVEMENT_LOW = 1500;
    private long LONG_MOVEMENT_HIGH = 2000;

    private float lastMovementPeak;
    private long lastMovementStartTime;
    private long lastMovementDuration;


    enum Movement{
         NO_MOVEMENT("No_movement"),
         BODY_TREMBLING("micro"),
         HAND_MOVEMENT("micro"),
         ARM_RISING("macro");

         private final String movement;

         Movement(String movement) {
             this.movement = movement;
         }

        @NonNull
        @Override
        public String toString(){
            return this.movement.toUpperCase();
        }
    }

    public MovementProcessor(){
         lastMovementPeak = 0;
         lastMovementStartTime = 0;
         lastMovementDuration = 0;
    }

    /**
     * Classify the type of movement based on the duration and its acceleration peak
     * @param duration Duration of the movement as read from the accelerometer data
     * @param peak Maximum peak as read from the accelerometer data
     * @return A Movement enum type among no_movement, body_trembling, hand_movement and arm_rising
     */
    private Movement getMovementFromDurationAndPeak(Long duration, Float peak){

        Movement ret = Movement.NO_MOVEMENT;

        if(peak > MOVEMENT_PEAK_THRESHOLD_HIGH && duration > SHORT_MOVEMENT_LOW
                && duration < SHORT_MOVEMENT_HIGH) {
            ret = Movement.BODY_TREMBLING;
        }

        if(peak < MOVEMENT_PEAK_THRESHOLD_LOW){
            if(duration > SHORT_MOVEMENT_LOW && duration < SHORT_MOVEMENT_HIGH) {
                ret = Movement.HAND_MOVEMENT;
            }
            else if(duration > LONG_MOVEMENT_LOW && duration < LONG_MOVEMENT_HIGH){
                ret = Movement.ARM_RISING;
            }
        }
        return ret;
    }

    /**
     * Detect the type of movement based on the acceleration sensor data
     * @param accelerationData JSON
     * @return
     */
    public Movement detectMovement(JSONObject accelerationData) {

        Movement movement = Movement.NO_MOVEMENT;

        JSONArray values = null;
        try {
            values = accelerationData.getJSONArray("values");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // iterate through the whole window to check for spikes
        if (values != null) {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (derivative > NOISE_THRESHOLD) {
                    if(derivative > lastMovementPeak){
                        lastMovementPeak = derivative;
                    }
                    if(lastMovementStartTime == 0){
                        lastMovementStartTime = System.currentTimeMillis();
                    }
                    else{
                        lastMovementDuration = System.currentTimeMillis() - lastMovementStartTime;
                    }
                    movement = Movement.NO_MOVEMENT;
                }
                // movement has ended and we can signal it
                else{
                    movement = getMovementFromDurationAndPeak(lastMovementDuration, lastMovementPeak);
                }
            }
        }
        return movement;
    }

}
