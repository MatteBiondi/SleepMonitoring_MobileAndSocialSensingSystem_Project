package it.unipi.ing.mobile.processinglibrary;

import java.util.ArrayList;
import java.util.HashMap;

public class PostureProcessor {

    public enum Posture{
        SUPINE,
        LEFT,
        RIGHT,
        PRONE
    }

    private static HashMap<Posture, Float[]> postureTilts = new HashMap<Posture, Float[]>();
    private static HashMap<Posture, Float[]> postureOrientations = new HashMap<Posture, Float[]>();

    /**
     * Since the angle data read from the sensors ranges from -180.0  to 180.0, in order to make the
     * Euclidean distance computations easier, every vector is shifted by 180 degrees. In this way,
     * the angle values can only assume non-negative values (from 0.0 to 360) and the Euclidean
     * distance algorithm is more straightforward for vectors for which wrap-around has to be taken
     * into account.
     */

    // angles wrap back to 0 when they reach 360
    private static final Float[] ANGLE_SHIFT = {180.0f, 180.0f, 180.0f};
    private static final Float[] ANGLE_MODULO = {360.0f, 360.0f, 360.0f};

    private static HashMap<Posture, Float[]> shiftedPostureTilts = new HashMap<Posture, Float[]>();
    private static HashMap<Posture, Float[]> shiftedPostureOrientations = new HashMap<Posture, Float[]>();


    public PostureProcessor(){

        /*
        * Data taken from "SleepGuard: Capturing Rich Sleep Information Using Smartwatch
        * Sensing Data" (Chang et al.), fig. 4
        * */
        // TODO: 14/05/2022 change values from paper with measurements taken from smartwatch
        Float[] supinePostureTilt = {84.0f, 112.0f, 21.0f};
        Float[] leftPostureTilt = {82.0f, 60.0f, 148.0f};
        Float[] rightPostureTilt = {136.0f, 114.0f, 48.0f};
        Float[] pronePostureTilt = {75.0f, 94.0f, 21.0f};

        postureTilts.put(Posture.SUPINE, supinePostureTilt);
        postureTilts.put(Posture.LEFT, leftPostureTilt);
        postureTilts.put(Posture.RIGHT, rightPostureTilt);
        postureTilts.put(Posture.PRONE, pronePostureTilt);

        shiftedPostureTilts.put(Posture.SUPINE, Util.vectorSum(supinePostureTilt, ANGLE_SHIFT));
        shiftedPostureTilts.put(Posture.LEFT, Util.vectorSum(leftPostureTilt, ANGLE_SHIFT));
        shiftedPostureTilts.put(Posture.RIGHT, Util.vectorSum(rightPostureTilt, ANGLE_SHIFT));
        shiftedPostureTilts.put(Posture.PRONE, Util.vectorSum(pronePostureTilt, ANGLE_SHIFT));


        // TODO: 15/05/2022 same as above
        Float[] supinePostureOrientation = {90.0f, 90.0f, 90.0f};
        Float[] pronePostureOrientation = {0.0f, 0.0f, 0.0f};

        postureOrientations.put(Posture.SUPINE, supinePostureOrientation);
        postureOrientations.put(Posture.PRONE, Util.vectorSum(pronePostureOrientation, ANGLE_SHIFT));

        shiftedPostureOrientations.put(Posture.SUPINE, supinePostureOrientation);
        shiftedPostureOrientations.put(Posture.PRONE, Util.vectorSum(pronePostureOrientation, ANGLE_SHIFT));
    }

    private Posture getPostureFromTiltData(Float[] tiltData) throws IllegalArgumentException{
        Float[] shiftedTiltData = Util.vectorSum(tiltData, ANGLE_SHIFT);

        int closestPostureIndex = Util.getClosestPointWithWrap(shiftedTiltData,
                                                (ArrayList<Float[]>) shiftedPostureTilts.values(),
                                                ANGLE_MODULO
                                                );
        return Posture.values()[closestPostureIndex];
    }

    private Posture getPostureFromOrientationData(Float[] orientationData) throws IllegalArgumentException{
        Float[] shiftedOrientationData = Util.vectorSum(orientationData, ANGLE_SHIFT);
        int closestPostureIndex = Util.getClosestPointWithWrap(shiftedOrientationData,
                                        (ArrayList<Float[]>) shiftedPostureOrientations.values(),
                                        ANGLE_MODULO);
        return Posture.values()[closestPostureIndex];
    }

    public Posture getPostureFromSensorData(Float[] tiltData, Float[] orientationData) throws IllegalArgumentException {
        if(tiltData.length != 3){
            throw new IllegalArgumentException("tiltData must be a 3-dimensional vector");
        }
        if(orientationData.length != 3){
            throw new IllegalArgumentException("orientationData must be a 3-dimensional vector");
        }

        Posture posture;
        posture = getPostureFromTiltData(tiltData);

        // if posture is LEFT or RIGHT, we're confident enough and return
        if(posture != Posture.PRONE && posture != Posture.SUPINE){
            return posture;
        }

        // otherwise we use data from the orientation sensor to discern between prone and supine
        posture = getPostureFromOrientationData(orientationData);

        // TODO: 15/05/2022 check for better way of returning Posture enum
        return posture;
    }
}
