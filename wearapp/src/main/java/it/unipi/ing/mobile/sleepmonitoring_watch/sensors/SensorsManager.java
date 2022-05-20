package it.unipi.ing.mobile.sleepmonitoring_watch.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.json.JSONArray;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;


public class SensorsManager implements SensorEventListener  {

    final private Sensor rotation_vector;
    final private Sensor accelerometer;
    final private SensorManager sm;
    final private long batchWindowSize;
    String directory;


    final private OutputStream outputStream=new PipedOutputStream();
    protected PrintWriter writer;
    public SensorsManager(Context context){
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> list = sm.getSensorList(Sensor.TYPE_ALL);
        for(Sensor x: list)
            Log.i("SM", "Type: " + x.getStringType() +
                    ", name: " + x.getName() +
                    ", vendor: " + x.getVendor() +
                    ", current (mA): " + x.getPower() +
                    ", is wakeup: " + x.isWakeUpSensor() +
                    ", FIFO: " + x.getFifoMaxEventCount()
            );
        rotation_vector = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        directory = context.getFilesDir().getPath();
        batchWindowSize= 5_000;


    }



    public void registerListeners(OutputStream outStream) throws Exception {
        acc_batch[0]=new DataBatch("acc",0);
        acc_batch[1]=new DataBatch("acc",0);
        rot_batch[0]=new DataBatch("rot",0);
        rot_batch[1]=new DataBatch("rot",0);
        if(!sm.registerListener(this, rotation_vector, SensorManager.SENSOR_DELAY_NORMAL))
            throw new Exception("Unable to register to rotation vector sensor");
        if(!sm.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL))
            throw new Exception("Unable to register to accelerometer sensor");
        writer = new PrintWriter(outStream);
    }

    public void unregisterListeners(){
        sm.unregisterListener(this);
    }


    private final DataBatch [] acc_batch= new DataBatch[2];

    private final DataBatch [] rot_batch= new DataBatch[2];
    //private int delay=10;
    @Override
    public void onSensorChanged(SensorEvent event) {
        /*if(delay>0) {
            delay--;
            return;
        }*/
        if (accelerometer.equals(event.sensor)) {
            try {
                JSONArray data = new JSONArray(event.values);
                Log.d("accelerometer", "received values " + data);
                long ms_time=event.timestamp/1000_000;
                for (int i=0;i<acc_batch.length;i++) {
                    if (acc_batch[i].getStartTime() == 0) {
                        Log.i("acc sender", "initialized");
                        acc_batch[0] = new DataBatch("acc", ms_time );
                        acc_batch[1] = new DataBatch("acc", ms_time + (batchWindowSize/2));
                        acc_batch[0].getData().put(data);
                        break;
                    }
                    if(ms_time <acc_batch[i].getStartTime() )
                        break;
                    if (ms_time - acc_batch[i].getStartTime() > batchWindowSize) {

                        Log.i("acc sender",String.format("sent out %d samples",acc_batch[i].getData().length()));
                        writer.println(acc_batch[i].getJson());
                        acc_batch[i] = new DataBatch("acc", ms_time);

                    }
                    acc_batch[i].getData().put(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (rotation_vector.equals(event.sensor)) {
            final float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientationAngle = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientationAngle);
            Log.d("rotation_vector", "received values " +
                    new JSONArray(Collections.singletonList(event.values)));
            Log.v("rotation_matrix", "received values " +
                    new JSONArray(Collections.singletonList(rotationMatrix)));
            try {
                JSONArray data = new JSONArray(orientationAngle);
                long ms_time=event.timestamp/1000_000;
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
                        break;
                    if (ms_time - rot_batch[i].getStartTime() >batchWindowSize ) {
                        writer.println(rot_batch[i].getJson());
                        Log.i("rot sender",String.format("sent out %d samples",rot_batch[i].getData().length()));
                        rot_batch[i] = new DataBatch("rot", ms_time);

                    }
                    rot_batch[i].getData().put(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //delay=10;
            // same for every sensor
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
