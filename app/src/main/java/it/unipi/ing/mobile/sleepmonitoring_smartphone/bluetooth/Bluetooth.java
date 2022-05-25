package it.unipi.ing.mobile.sleepmonitoring_smartphone.bluetooth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class Bluetooth {
    public static final String TAG = "BLUETOOTH";
    public static final Integer BLUETOOTH_REQUEST_CODE = 1000;

    private final ActivityResultLauncher<Intent> bluetooth_launcher;
    private final BluetoothAdapter bluetoothAdapter;
    private static Bluetooth instance = null;

    public static Bluetooth build(Activity activity){
        if(instance == null)
            instance =  new Bluetooth(activity);

        return instance;
    }

    private Bluetooth(Context context){

        // Get bluetooth adapter, if supported
        bluetoothAdapter = context.getSystemService(BluetoothManager.class).getAdapter();

        // Define intent launcher to request bluetooth
        bluetooth_launcher =
                ((AppCompatActivity)context).registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        uri -> bluetoothRequestCallback(uri.getResultCode())
                );
    }

    public boolean checkInterface() {
        if (bluetoothAdapter == null)
            return false;
        return bluetoothAdapter.isEnabled();
    }

    public void enableInterface(Activity activity){
        // Enable bluetooth adapter
        if (!bluetoothAdapter.isEnabled()) { // Bluetooth off
            if (permissionGranted(activity)) { // Request run-time permissions
                bluetooth_launcher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
            else {
                Log.i(TAG, "Missing BLE permissions");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            BLUETOOTH_REQUEST_CODE
                    );
            }
        }
    }

    private boolean permissionGranted(Context context){
        // BLUETOOTH_CONNECT run-time permission request for Android >= 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // Always true for Android < 12
        return true;
    }

    public void bluetoothRequestCallback(int resultCode){
        if (resultCode != Activity.RESULT_OK){
            Log.i(TAG, "Bluetooth disabled");
        }
    }
}