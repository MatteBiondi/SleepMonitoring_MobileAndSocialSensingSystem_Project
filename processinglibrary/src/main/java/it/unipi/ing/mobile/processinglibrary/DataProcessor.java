package it.unipi.ing.mobile.processinglibrary;

import org.json.JSONException;
import org.json.JSONObject;

public class DataProcessor {

    private MovementProcessor movementProcessor;
    private RolloverProcessor rolloverProcessor;

    public DataProcessor() {
        movementProcessor = new MovementProcessor();
        rolloverProcessor = new RolloverProcessor();
    }


    public String getEventFromSensorData(JSONObject sensorData) throws JSONException {

        String event = null;

        if (sensorData.getString("sensorName").equals("acc")) {
            boolean rollover = rolloverProcessor.hasRolloverOccurred(sensorData);
            if (rollover) {
                event = "roll";
            }
        } else if (sensorData.getString("sensorName").equals("rot")) {
            MovementProcessor.Movement movement = movementProcessor.detectMovement(sensorData);
            if (!movement.equals(MovementProcessor.Movement.NO_MOVEMENT)) {
                event = movement.toString();
            }
        }

        return event;
    }
}
