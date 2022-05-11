package it.unipi.ing.mobile.sleepmonitoring_watch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import it.unipi.ing.mobile.sleepmonitoring_watch.databinding.ActivityMainBinding;
import it.unipi.ing.mobile.sleepmonitoring_watch.sensors.SensorsManager;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;
    private SensorsManager sensorsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sensorsManager =new SensorsManager(this.getApplicationContext());

    }

    public void start_recording(View view){
        try {
            sensorsManager.registerListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}