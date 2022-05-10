package it.unipi.ing.mobile.sleepmonitoring_watch;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import it.unipi.ing.mobile.sleepmonitoring_watch.databinding.ActivityMainBinding;
import it.unipi.ing.mobile.sleepmonitoring_watch.sensors.SensorsManager;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;
    private Object SensorsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        try {
            SensorsManager =new SensorsManager(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}