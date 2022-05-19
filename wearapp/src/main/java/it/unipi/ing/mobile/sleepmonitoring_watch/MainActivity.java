package it.unipi.ing.mobile.sleepmonitoring_watch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import it.unipi.ing.mobile.sleepmonitoring_watch.communication.StreamChannel;
import it.unipi.ing.mobile.sleepmonitoring_watch.databinding.ActivityMainBinding;
import it.unipi.ing.mobile.sleepmonitoring_watch.sensors.SensorsManager;

public class MainActivity extends Activity implements CapabilityClient.OnCapabilityChangedListener {
    public final String MOBILE_CAPABILITY = "it.unipi.ing.mobile.sleepmonitoring.mobile";
    private TextView mTextView;
    private TextView status_label;
    private ActivityMainBinding binding;
    private SensorsManager sensorsManager;
    private StreamChannel stream_channel = null;
    private ImageButton play_stop_button;
    private String paired_node_id = null;
    private final String TAG = "MainActivity_LogTag";
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        play_stop_button = findViewById(R.id.play_stop_button);
        status_label = findViewById(R.id.status_label);
        sensorsManager =new SensorsManager(this.getApplicationContext());
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Check if status changed
        getPairedDeviceNodeId();

        // Paired device listener add
        Wearable.getCapabilityClient(getApplicationContext()).addListener(
                this, MOBILE_CAPABILITY);
    }

    @Override
    protected void onStop(){
        super.onStop();

        // Paired device listener remove
        Wearable.getCapabilityClient(getApplicationContext())
                .removeListener(this, MOBILE_CAPABILITY);
    }

    private void setStatusLabel(int nodes){
        String status = (nodes > 0) ? " connected":" disconnected";
        status_label.setText(getString(R.string.status_label) + status);
    }

    public void start_recording(View view){
        try {
            if(paired_node_id == null){
                Toast.makeText(this, "No devices connected", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.i(TAG,"startRecording");


            // Open channel
            stream_channel = new StreamChannel(
                    paired_node_id,
                    "start-service",
                    Wearable.getChannelClient(getApplicationContext())
            );

            //todo register
            //sensorsManager.registerListeners();
            running = true;
            play_stop_button.setImageResource(R.drawable.ic_baseline_stop_circle);
            play_stop_button.setOnClickListener(this::stop_recording);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop_recording(View view){
        try {
            Log.i(TAG,"stopRecording");

            //todo Unregister
            sensorsManager.unregisterListeners();

            // Close channel
            if (stream_channel != null)
                stream_channel.close();
            stream_channel = null;

            if (view != null){
                running = false;
            }
            play_stop_button.setImageResource(R.drawable.ic_baseline_play_circle_filled);
            play_stop_button.setOnClickListener(this::start_recording);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPairedDeviceNodeId(){
        //Get capabilities node
        Task<CapabilityInfo> capability_task = (
                Wearable.getCapabilityClient(this).getCapability(
                        MOBILE_CAPABILITY, CapabilityClient.FILTER_REACHABLE));

        capability_task.addOnCompleteListener(task -> {
            Log.i(TAG, "Searching nodes ...");
            Set<Node> nodes = capability_task.getResult().getNodes();

            setStatusLabel(nodes.size());
            // Is possible to pair only one smartphone to the watch,
            //      the nodes should contain one node or zero
            for (Node node : nodes){
                Log.i(TAG, node.toString());
                paired_node_id = node.getId();
            }
        });
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        Set<Node> nodes = capabilityInfo.getNodes();
        Log.i(TAG, "Available nodes: " + nodes.size());
        // Is possible to pair only one smartphone to the watch,
        //      the nodes should contain one node or zero

        if (nodes.size() == 0){
            paired_node_id = null;

            // Stop sensing data
            if (running){
                stop_recording(null);
            }
        }

        for (Node node : nodes) {
            Log.i(TAG, node.getDisplayName());
            paired_node_id = node.getId();
        }

        // Auto-restart
         if(running && paired_node_id != null){
            start_recording(null);
         }

        setStatusLabel(nodes.size());
    }
}