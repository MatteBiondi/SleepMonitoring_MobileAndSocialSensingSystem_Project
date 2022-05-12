package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.bluetooth.Bluetooth;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Bluetooth bluetooth;

    private final String TAG = "SleepMonitoring_smartphone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //todo mPreferences=getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_report, R.id.nav_account, R.id.nav_settings, R.id.nav_exit)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Bluetooth pop-up alert
        bluetooth = Bluetooth.build(this);

        SharedPreferences shared_preferences = getPreferences(MODE_PRIVATE);
        if(!bluetooth.checkInterface() && shared_preferences.getBoolean(
                "bluetooth_alert", true)){
            SharedPreferences.Editor editor = shared_preferences.edit();
            editor.putBoolean("bluetooth_alert", true);
            editor.apply();
            showBluetoothAlert();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"onResume");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG,"onStart");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG,"onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG,"onStop");
    }

    private void showBluetoothAlert(){
        AlertDialog.Builder bluetooth_alert_builder = new AlertDialog.Builder(this)
                .setTitle(R.string.bluetooth_alert_title)
                .setMessage(R.string.bluetooth_alert_message)
                .setPositiveButton(
                        R.string.bluetooth_enable, (dialog, which) -> bluetooth.enableInterface())
                .setNegativeButton(R.string.bluetooth_ignore, (dialogInterface, i) -> {
                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    editor.putBoolean("bluetooth_alert", false);
                    editor.apply();
                })
                .setNeutralButton(R.string.bluetooth_neutral, null)
                .setIcon(R.drawable.ic_baseline_bluetooth_24);
        AlertDialog bluetooth_alert = bluetooth_alert_builder.show();
    }

    // Android >= 12 bluetooth connection request run-time permission
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Bluetooth.BLUETOOTH_REQUEST_CODE) {
            // Checking whether user granted the permission or not
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Bluetooth permission granted");
                bluetooth.enableInterface();
            }
            else {
                Log.i(TAG,"BLE permission denied");
            }
        }
    }
}