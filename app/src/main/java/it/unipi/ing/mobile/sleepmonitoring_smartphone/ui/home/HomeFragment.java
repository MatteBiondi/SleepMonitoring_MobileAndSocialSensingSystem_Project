package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.WearableListener;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.WorkerService;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.bluetooth.Bluetooth;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.databinding.FragmentHomeBinding;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.home.HomeFragmentDirections.ActionNavHomeToNavReport;

public class HomeFragment extends Fragment {
    public final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    private String sharedPrefFile ;
    private String user_first_name_preferences_key;
    private SharedPreferences mPreferences;
    private StatusReceiver status_receiver;
    private final String WATCH_CAPABILITY = "it.unipi.ing.mobile.sleepmonitoring.watch";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set attributes for shared preferences
        sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        user_first_name_preferences_key = getString(R.string.user_first_name_preferences_key);

        // Get account information from sharedPreferences
        String firstName = mPreferences.getString(user_first_name_preferences_key, "");

        final TextView textView = binding.homeWelcomeLabel;
        textView.append(" "+firstName);


        // Set home fragment button listener for click event
        Button seeLastReport = binding.homeLastReportButton;
        seeLastReport.setOnClickListener(view -> {
            ActionNavHomeToNavReport action =HomeFragmentDirections.actionNavHomeToNavReport();
            action.setLastReport(true);
            Navigation.findNavController(view).navigate(action);
        });

        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Define status receiver to update UI
        status_receiver = new StatusReceiver();
        Activity activity = getActivity();
        if(activity != null){
            activity.registerReceiver(
                    status_receiver,
                    new IntentFilter("it.unipi.ing.mobile.sleepmonitoring_smartphone.RUNNING")
            );

            Wearable.getCapabilityClient(activity.getApplicationContext())
                    .addListener(status_receiver, WATCH_CAPABILITY);

            // Init status label
            // Get capabilities node
            Task<CapabilityInfo> capability_task = (
                    Wearable.getCapabilityClient(activity.getApplicationContext()).getCapability(
                            WATCH_CAPABILITY, CapabilityClient.FILTER_REACHABLE));

            capability_task.addOnCompleteListener(task -> {
                Log.i(TAG, "Searching nodes ...");
                Set<Node> nodes = capability_task.getResult().getNodes();

                // Is possible to pair only one smartphone to the watch,
                //      the nodes should contain one node or zero
                if (nodes.size() == 0){
                    updateStatus(Status.DISCONNECTED);
                }
                else {
                    for (Node node : nodes) { // Usually there is just one node
                        Log.i(TAG, node.toString());
                        if (node.isNearby()){
                            updateStatus( WearableListener.isRunning() ? Status.RUNNING : Status.CONNECTED );
                        }
                    }
                }
            });
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Activity activity = getActivity();

        // Remove status receiver
        if(activity != null){
            activity.unregisterReceiver(status_receiver);
            Wearable.getCapabilityClient(activity.getApplicationContext())
                    .removeListener(status_receiver, WATCH_CAPABILITY);
        }
        binding = null;
    }

    private void updateStatus(Status status){
        binding.homeStatusValue.setText(status.toString());
        binding.homeStatusValue.setTextColor(status.getColor());
    }

    class StatusReceiver extends BroadcastReceiver implements CapabilityClient.OnCapabilityChangedListener{
        @Override
        public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
            Set<Node> nodes = capabilityInfo.getNodes();
            Log.i(TAG, "Available nodes: " + nodes.size());
            if (nodes.size() == 0){
                updateStatus(Status.DISCONNECTED);
            }
            else {
                for (Node node : nodes) { // Usually there is just one node
                    Log.i(TAG, node.toString());
                    if (node.isNearby()){
                        updateStatus( WearableListener.isRunning() ? Status.RUNNING:Status.CONNECTED );
                    }
                }
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "BRD RECEIVER");
            updateStatus( WearableListener.isRunning() ? Status.RUNNING : Status.CONNECTED );
        }
    }

    enum Status{
        CONNECTED("Connected", Color.GREEN),
        DISCONNECTED("Disconnected", Color.RED),
        RUNNING("Running", Color.BLUE);

        private final String status;
        private final int color;

        Status(String status, int color) {
            this.status = status;
            this.color = color;
        }

        @NonNull
        @Override
        public String toString(){
            return this.status.toUpperCase();
        }

        public int getColor(){
            return this.color;
        }
    }
}