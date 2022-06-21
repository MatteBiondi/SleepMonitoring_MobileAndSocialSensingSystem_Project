package it.unipi.ing.mobile.sleepmonitoring.sensors;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;

/**
 * this implementation will read data from sensor, aggregate them in batch and send it to the
 * next processing unit via an OutputStream
 */
public class OnlineSensorsManager extends SensorsManager  {

    final private long batchWindowSize;

    final protected OutputStream outStream;
    public OnlineSensorsManager(Context context,OutputStream outStream ){
        super(context);
        this.outStream=outStream;
        batchWindowSize= 2_000;
    }

    @Override
    public void registerListeners() throws Exception {
        acc_batch[0]=new DataBatch("acc",0);
        acc_batch[1]=new DataBatch("acc",0);
        rot_batch[0]=new DataBatch("rot",0);
        rot_batch[1]=new DataBatch("rot",0);
        super.registerListeners();
        writer = new PrintWriter(outStream);

    }

    @Override
    public void unregisterListeners(){
        super.unregisterListeners();
        writer.close();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (accelerometer.equals(event.sensor)  ) {
            accelerometerHandler(event.values, event.timestamp);
        }
        if (rotation_vector.equals(event.sensor)) {
            rotationHandler(event.values, event.timestamp);
        }
    }

    // data batches for overlapping windows
    private final DataBatch [] acc_batch= new DataBatch[2];
    private final DataBatch [] rot_batch= new DataBatch[2];

    /**
     *  build overlapping windows batches for rotation sensor readings
     * @param values rotation vector values
     * @param timestamp timestamp of sensor reading
     */
    protected void rotationHandler(float[] values, long timestamp){
        Log.d("rotation_vector", "received values " +
                new JSONArray(Collections.singletonList(values)));
        try {
            JSONArray data = new JSONArray(Arrays.copyOfRange(values, 0, 3));
            long ms_time=timestamp/1000_000;
            for (int i=0;i<rot_batch.length;i++) {

                if (rot_batch[i].getStartTime() == 0) {
                    Log.i("rot sender", "initialized");
                    rot_batch[0] = new DataBatch("rot", ms_time);
                    rot_batch[1] = new DataBatch("rot", ms_time + (batchWindowSize/2));
                    rot_batch[0].getData().put(data);
                    break;
                }
                //Log.i("rot sender",String.format("%d %d",rot_batch[0].getStartTime(), rot_batch[1].getStartTime()));
                if(ms_time <rot_batch[i].getStartTime() )
                    continue;
                if (ms_time - rot_batch[i].getStartTime() >batchWindowSize ) {
                    writer.println(rot_batch[i].getJson());
                    Log.v("rot sender",String.format("sent out %d samples",rot_batch[i].getData().length()));
                    rot_batch[i] = new DataBatch("rot", ms_time);

                }
                rot_batch[i].getData().put(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  build overlapping windows batches for rotation sensor readings
     * @param values rotation vector values
     * @param timestamp timestamp of sensor reading
     */
    protected void accelerometerHandler(float[] values, long timestamp){
        try {
            JSONArray data = new JSONArray(values);
            Log.d("accelerometer", "received values " + data);
            long ms_time=timestamp/1000_000;
            for (int i=0;i<acc_batch.length;i++) {
                if (acc_batch[i].getStartTime() == 0) {
                    Log.i("acc sender", "initialized");
                    acc_batch[0] = new DataBatch("acc", ms_time );
                    acc_batch[1] = new DataBatch("acc", ms_time + (batchWindowSize/2));
                    acc_batch[0].getData().put(data);
                    break;
                }
                if(ms_time <acc_batch[i].getStartTime() )
                    continue;
                if (ms_time - acc_batch[i].getStartTime() > batchWindowSize) {

                    Log.i("acc sender",String.format("sent out %d samples",acc_batch[i].getData().length()));
                    writer.println(acc_batch[i].getJson());
                    acc_batch[i] = new DataBatch("acc", ms_time);

                }
                acc_batch[i].getData().put(data);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
