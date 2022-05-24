package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.WearableListener;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    public final String TAG = "HomeFragment";
    public boolean welcomeMessageUpdated;
    private FragmentHomeBinding binding;

    private String sharedPrefFile ;
    private String user_first_name_preferences_key;
    private SharedPreferences mPreferences;
    private StatusReceiver status_receiver;
    private final String WATCH_CAPABILITY = "it.unipi.ing.mobile.sleepmonitoring.watch";
    private final String RUNNING_INTENT = "it.unipi.ing.mobile.sleepmonitoring_smartphone.RUNNING";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        welcomeMessageUpdated=false;

        // Set attributes for shared preferences
        sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        user_first_name_preferences_key = getString(R.string.user_first_name_preferences_key);

        //Update welcome message
        updateWelcomeMessage();

        // Set home fragment button listener for click event
        Button seeLastReport = binding.homeLastReportButton;
        seeLastReport.setOnClickListener(view -> {
            HomeFragmentDirections.ActionNavHomeToNavReport action =HomeFragmentDirections.actionNavHomeToNavReport();
            action.setLastReport(true);
            Navigation.findNavController(view).navigate(action);
        });

        //todo serve?
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Define status receiver to update UI
        status_receiver = new StatusReceiver();
        Activity activity = getActivity();

        if(activity != null){
            activity.registerReceiver(
                    status_receiver,
                    new IntentFilter(RUNNING_INTENT)
            );

            // Check capabilities node
            Log.i(TAG, "Searching nodes ...");
            Wearable.getCapabilityClient(activity.getApplicationContext()).getCapability(
                    WATCH_CAPABILITY,
                    CapabilityClient.FILTER_REACHABLE
            ).addOnCompleteListener(task -> {
                try {
                    Set<Node> nodes = task.getResult().getNodes();
                    HomeFragment.this.checkNearbyNodes(nodes);
                }
                catch (Exception e){
                    Toast.makeText(getContext(),R.string.no_wearable_api,Toast.LENGTH_LONG).show();
                    Activity thisActivity = getActivity();
                    if(thisActivity != null) thisActivity.finishAndRemoveTask();
                }

            });

            // Register capabilities listener
            Wearable.getCapabilityClient(activity.getApplicationContext())
                    .addListener(status_receiver, WATCH_CAPABILITY);

        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Activity activity = getActivity();

        // Remove status receiver and capabilities listener
        if(activity != null){
            activity.unregisterReceiver(status_receiver);
            Wearable.getCapabilityClient(activity.getApplicationContext())
                    .removeListener(status_receiver, WATCH_CAPABILITY);
        }
        binding = null;
    }

    private void updateUIStatus(Status status){
        binding.homeStatusValue.setText(status.toString());
        binding.homeStatusValue.setTextColor(status.getColor());
    }

    private void checkNearbyNodes(Set<Node> nodes){
        if (nodes.size() == 0){
            updateUIStatus(Status.DISCONNECTED);
        }
        else {
            for (Node node : nodes) { // Usually there is just one node
                Log.i(TAG, node.toString());
                if (node.isNearby()){
                    updateUIStatus( WearableListener.isRunning() ? Status.RUNNING : Status.CONNECTED );
                }
                else{
                    updateUIStatus(Status.DISCONNECTED);
                }
            }
        }
    }

    class StatusReceiver extends BroadcastReceiver implements CapabilityClient.OnCapabilityChangedListener{
        @Override
        public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
            checkNearbyNodes(capabilityInfo.getNodes());
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            updateUIStatus( WearableListener.isRunning() ? Status.RUNNING : Status.CONNECTED );
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

    public boolean updateWelcomeMessage(){
        // Get account information from sharedPreferences
        String firstName = mPreferences.getString(user_first_name_preferences_key, "");
        String welcomeMessage = getString(R.string.home_welcome_label)+" "+firstName;

        TextView textView = binding.homeWelcomeLabel;
        textView.setText(welcomeMessage);

        return !firstName.equals("");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"onResume + "+welcomeMessageUpdated);

        if(!welcomeMessageUpdated) {
            // Update welcome message
            welcomeMessageUpdated=updateWelcomeMessage();
        }
    }

}