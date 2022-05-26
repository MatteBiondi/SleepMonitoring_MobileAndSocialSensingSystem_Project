package it.unipi.ing.mobile.sleepmonitoring;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Set;

import it.unipi.ing.mobile.sleepmonitoring.communication.StreamChannel;
import it.unipi.ing.mobile.sleepmonitoring.communication.StreamHandler;
import it.unipi.ing.mobile.sleepmonitoring.databinding.ActivityMainBinding;
import it.unipi.ing.mobile.sleepmonitoring.sensors.SensorsManager;

public class MainActivity extends Activity implements CapabilityClient.OnCapabilityChangedListener {

    private final String TAG = "MainActivity_LogTag";

    private ActivityMainBinding binding;
    private SensorsManager sensorsManager;
    private StreamChannel stream_channel = null;
    private String paired_node_id = null;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sensorsManager =new SensorsManager(this.getApplicationContext());

        // Update status
        Wearable.getCapabilityClient(this).getCapability(
                getString(R.string.mobile_capability),
                CapabilityClient.FILTER_REACHABLE
        ).addOnSuccessListener(task -> setPairedDeviceNodeId(task.getNodes()));

        // Register capabilities listener
        Wearable.getCapabilityClient(getApplicationContext()).addListener(
                this, getString(R.string.mobile_capability));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e(TAG, "OnDestroy");

        // Paired device listener remove
        Wearable.getCapabilityClient(getApplicationContext())
                .removeListener(this, getString(R.string.mobile_capability));

        if (running)
            stop_recording(null); // TODO: error on mobile app if not stop
    }

    private void updateUIStatus(Status status){
        binding.statusLabel.setText(status.toString());
        binding.statusLabel.setTextColor(status.getColor());
        binding.playStopButton.setImageResource(status.getButtonImage());
    }

    Test testThread;
    public void start_recording(View view){
        try {
            if(paired_node_id == null){
                Toast.makeText(this, getString(R.string.no_devices), Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i(TAG,"startRecording");
            PipedOutputStream outS= new PipedOutputStream();
            InputStream inS=new PipedInputStream(outS);
            stream_channel = new StreamChannel(
                    paired_node_id,
                    getString(R.string.start_endpoint),
                    Wearable.getChannelClient(getApplicationContext()),
                    new StreamHandler() {
                        @Override
                        public void setInputStream(InputStream input_stream) {
                            Log.i(TAG, "Input stream handler not defined");
                        }

                        @Override
                        public void setOutputStream(OutputStream output_stream) {
                            try {
                                sensorsManager.registerListeners(output_stream);
                                running = true;
                                binding.playStopButton.setOnClickListener(view -> stop_recording(view));
                                updateUIStatus(Status.RUNNING);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
            //TODO testing
            //testThread=new Test(inS);
            //testThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop_recording(View view){
        try {
            Log.i(TAG,"stopRecording");
            //testThread.interrupt();
            //todo Unregister
            sensorsManager.unregisterListeners();

            // Close channel
            if (stream_channel != null)
                stream_channel.close();
            stream_channel = null;

            // Reset running only when the function is invoked by user
            if (view != null){
                running = false;
            }

            // Update button listener
            binding.playStopButton.setOnClickListener(this::start_recording);

            //Update UI
            updateUIStatus(Status.CONNECTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPairedDeviceNodeId(Set<Node> nodes){
        Log.i(TAG, "Available nodes: " + nodes.size());

        // Is possible to pair only one smartphone to the watch,
        //      the nodes should contain one node or zero
        if (nodes.size() == 0){// No nodes connected to watch
            paired_node_id = null;
            if (running){  // Stop sensing data
                stop_recording(null);
            }
            updateUIStatus(Status.DISCONNECTED);
            return;
        }

        for (Node node : nodes) { // Should be just one
            Log.i(TAG, node.getDisplayName());
            if (node.isNearby()){ // There is a node connected to watch
                paired_node_id = node.getId();

                if(running){ // Auto-restart
                    //start_recording(null);
                    updateUIStatus(Status.RUNNING);
                }
                else {
                    updateUIStatus(Status.CONNECTED);
                }
            }
            else { // No nodes connected to watch
                updateUIStatus(Status.DISCONNECTED);
            }
        }
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        setPairedDeviceNodeId(capabilityInfo.getNodes());
    }

    enum Status{
        CONNECTED(
                "Connected",
                Color.GREEN,
                R.drawable.ic_baseline_play_circle_filled
        ),
        DISCONNECTED(
                "Disconnected",
                Color.RED,
                R.drawable.ic_baseline_play_circle_filled
        ),
        RUNNING(
                "Running",
                Color.BLUE,
                R.drawable.ic_baseline_stop_circle
        );

        private final String status;
        private final int color;
        private final int button;

        Status(String status, int color, int button) {
            this.status = status;
            this.color = color;
            this.button = button;
        }

        @NonNull
        @Override
        public String toString(){
            return this.status.toUpperCase();
        }

        public int getColor(){
            return this.color;
        }

        public int getButtonImage(){
            return button;
        }
    }
}