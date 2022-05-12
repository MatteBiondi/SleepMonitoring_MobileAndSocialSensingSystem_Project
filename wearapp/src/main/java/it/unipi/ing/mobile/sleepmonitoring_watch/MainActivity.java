package it.unipi.ing.mobile.sleepmonitoring_watch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import it.unipi.ing.mobile.sleepmonitoring_watch.databinding.ActivityMainBinding;
import it.unipi.ing.mobile.sleepmonitoring_watch.sensors.SensorsManager;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;
    private SensorsManager sensorsManager;
    private ImageButton play_stop_button;
    private final String TAG = "MainActivity_LogTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        play_stop_button = findViewById(R.id.play_stop_button);

        sensorsManager =new SensorsManager(this.getApplicationContext());

    }

    public void start_recording(View view){
        try {
            Log.i(TAG,"startRecording");
            //todo register
            //sensorsManager.registerListeners();
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
            //sensorsManager.unregisterListeners();
            play_stop_button.setImageResource(R.drawable.ic_baseline_play_circle_filled);
            play_stop_button.setOnClickListener(this::start_recording);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}