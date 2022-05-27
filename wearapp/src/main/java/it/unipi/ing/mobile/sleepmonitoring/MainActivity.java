package it.unipi.ing.mobile.sleepmonitoring;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Set;

import it.unipi.ing.mobile.processinglibrary.Util;
import it.unipi.ing.mobile.sleepmonitoring.communication.StreamChannel;
import it.unipi.ing.mobile.sleepmonitoring.communication.StreamHandler;
import it.unipi.ing.mobile.sleepmonitoring.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements CapabilityClient.OnCapabilityChangedListener {
    private static MainActivity instance;
    private final String TAG = "MainActivity_LogTag";
    private ActivityMainBinding binding;
    private StreamChannel stream_channel = null;
    private PipedOutputStream piped_stream = null;
    private String paired_node_id = null;
    private Worker worker_thread = null;

    public static MainActivity getInstance(){
        return instance;
    }

    public OutputStream getStream() throws IOException {
        if (!Util.OFFLOADED){ // Computation on watch
            return piped_stream;
        }
        else { // Computation on mobile
            return stream_channel.getOutStream();
        }
    }

    public Worker getWorker() {
        return worker_thread;
    }

    public StreamChannel getChannel(){
        return stream_channel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Used by getInstance method
        instance = this;

        // User interface
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Register capabilities listener
        Wearable.getCapabilityClient(getApplicationContext()).addListener(
                this, getString(R.string.mobile_capability));

        if (DataCollectorService.isRunning()){ // Check if service is running
            paired_node_id = DataCollectorService.getPairedNodeId();
            binding.playStopButton.setOnClickListener(this::stop_recording);
            updateUIStatus(Status.RUNNING);
        }
        else{ // Set status otherwise
            Wearable.getCapabilityClient(this).getCapability(
                    getString(R.string.mobile_capability),
                    CapabilityClient.FILTER_REACHABLE
            ).addOnSuccessListener(task -> setPairedDeviceNodeId(task.getNodes()));
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "OnDestroy");

        // Paired device listener remove
        Wearable.getCapabilityClient(getApplicationContext())
                .removeListener(this, getString(R.string.mobile_capability));
    }

    public void start_recording(View view){
        try {
            if(paired_node_id == null){
                Toast.makeText(this, getString(R.string.no_devices), Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i(TAG,"startRecording");

            stream_channel = new StreamChannel (
                    paired_node_id,
                    getString(R.string.start_endpoint),
                    Wearable.getChannelClient(getApplicationContext()),
                    new StreamHandler() {
                        @Override
                        public void setInputStream(InputStream input_stream) {
                            // Not needed
                            Log.d(TAG, "Input stream handler not defined");
                        }

                        @Override
                        public void setOutputStream(OutputStream output_stream) {
                            try {
                                if (!Util.OFFLOADED){ // Computation on watch

                                    // Stream used for communication between main thread and worker thread
                                    PipedOutputStream outS = new PipedOutputStream();
                                    InputStream inS = new PipedInputStream(outS);
                                    piped_stream = outS;

                                    // Start worker thread
                                    worker_thread=new Worker(inS, output_stream);
                                    worker_thread.start();
                                }

                                // Start data collection
                                startDataCollection();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop_recording(View view){
        try {
            Log.i(TAG,"stopRecording");

            // Stop foreground service
            Intent stopServiceIntent = new Intent(this, DataCollectorService.class);
            stopServiceIntent.setAction(getString(R.string.stop_service));
            // Available from Android 8 (API 26)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                startForegroundService(stopServiceIntent);
            }
            else {// Android API < 26
               startService(stopServiceIntent);
            }

            // Update button listener
            binding.playStopButton.setOnClickListener(this::start_recording);

            //Update UI
            updateUIStatus(Status.CONNECTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUIStatus(Status status){
        binding.statusLabel.setText(status.toString());
        binding.statusLabel.setTextColor(status.getColor());
        binding.playStopButton.setImageResource(status.getButtonImage());
    }

    private void startDataCollection(){
        // Start foreground service
        Intent startServiceIntent = new Intent(getApplicationContext(), DataCollectorService.class);
        startServiceIntent.setAction(getString(R.string.start_service));
        startServiceIntent.putExtra("paired_node_id", paired_node_id);
        // Available from Android 8 (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(startServiceIntent);
        }
        else {// Android API < 26
            startService(startServiceIntent);
        }

        // Update button listener
        binding.playStopButton.setOnClickListener(this::stop_recording);

        // Update UI
        updateUIStatus(Status.RUNNING);
    }

    private void setPairedDeviceNodeId(Set<Node> nodes){
        Log.i(TAG, "Available nodes: " + nodes.size());

        // Is possible to pair only one smartphone to the watch,
        //      the nodes should contain one node or zero
        if (nodes.size() == 0){// No nodes connected to watch
            paired_node_id = null;
            binding.playStopButton.setOnClickListener(this::start_recording);
            updateUIStatus(Status.DISCONNECTED);
            return;
        }

        for (Node node : nodes) { // Should be just one
            Log.i(TAG, node.getDisplayName());
            if (node.isNearby()){ // There is a node connected to watch
                paired_node_id = node.getId();
                updateUIStatus(Status.CONNECTED);
            }
            else { // No nodes connected to watch
                paired_node_id = null;
                binding.playStopButton.setOnClickListener(this::start_recording);
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