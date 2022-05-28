package it.unipi.ing.mobile.sleepmonitoring;

import static java.lang.String.valueOf;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

import it.unipi.ing.mobile.sleepmonitoring.bluetooth.Bluetooth;
import it.unipi.ing.mobile.sleepmonitoring.database.SleepEventDatabase;
import it.unipi.ing.mobile.sleepmonitoring.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Bluetooth bluetooth;

    private final String TAG = "SleepMonitoring_smartphone";

    private SharedPreferences mPreferences;


    private GoogleSignInClient client;
    private GoogleSignInAccount account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the shared preferences
        String sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // Restore Theme preferences
        // MUST be before super.onCreate() because of a bug that otherwise makes the app crash
        String selectedTheme = mPreferences.getString(
                getString(R.string.theme_preferences_key),
                "Light"
        );
        if(selectedTheme.equals("Light") || selectedTheme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_report, R.id.nav_account, R.id.nav_settings, R.id.nav_exit)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController,
                mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Sign-in with Google credentials
        signInWithGoogle();

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

        // Define listeners for exit item in the sidebar menu
        defineExitItemListener(navigationView);

    }

    private void defineExitItemListener(NavigationView navigationView) {
        // onClickListener for exit button in navBar
        MenuItem exitItem = navigationView.getMenu().findItem(R.id.nav_exit);
        exitItem.setOnMenuItemClickListener(menuItem -> {
            client.signOut().addOnCompleteListener(task -> {
                Toast.makeText(getApplicationContext(), R.string.signOut, Toast.LENGTH_SHORT).show();

                // Remove user info from shared preferences
                String user_first_name_key = getString(R.string.user_first_name_preferences_key);
                String user_last_name_key = getString(R.string.user_last_name_preferences_key);
                String user_email_key = getString(R.string.user_email_preferences_key);
                mPreferences.edit().remove(user_first_name_key).apply();
                mPreferences.edit().remove(user_last_name_key).apply();
                mPreferences.edit().remove(user_email_key).apply();

                // Close application
                finishAndRemoveTask();
            });

            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_content_main);
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
        SleepEventDatabase.close();
        Log.i(TAG,"onStop");
    }

    public void revokeAccess(){
        // Disconnect Google account from the application
        client.revokeAccess()
                .addOnCompleteListener(this, task -> {
                    Toast.makeText(
                            getApplicationContext(),
                            R.string.disconnection,
                            Toast.LENGTH_SHORT
                    ).show();

                    // Remove user info from shared preferences
                    mPreferences.edit().clear().apply();

                    // Remove saved history reports
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            SleepEventDatabase db = SleepEventDatabase.build(getApplicationContext());
                            // Null as argument means "Delete all"
                            db.deleteBefore(null);
                        }
                    }.start();

                    // Close application
                    finishAndRemoveTask();
                });
    }

    // Function that requires login action to the user using their Google credentials
    private void signInWithGoogle(){
        // Definition of the sign-in options and the required scopes (email in this case)
        // Sign-in is configured to require information about user's ID, basic personal information and email
        // ID e basic information are included in DEFAULT_SIGN_IN option
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Initialize the attribute of GoogleSignInClient class with the options just defined
        client = GoogleSignIn.getClient(this, signInOptions);

        /*
         Check for existing Google Signed-In account
         If the user is already signed-in -> the GoogleSignInAccount will be non-null.
        */
        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            // No already signed-in user -> Start Login
            requestSignIn();
        }
        else{
            // Read information about already signed-in user
            readUserInfo();
        }
    }

    private void requestSignIn(){
        // Check that the Google Play Services are installed and available to be used
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            // If not -> Error, show message and close the application
            Toast.makeText(getApplicationContext(),R.string.failed_google_login,Toast.LENGTH_LONG).show();
            finishAndRemoveTask();
        } else {
            // The sign-in Intent is launched and result is handled
            Intent i = client.getSignInIntent();
            ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Check if login success
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // If yes take user data from intent result and read the information
                            Intent data = result.getData();
                            account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
                            Toast.makeText(getApplicationContext(), R.string.signIn, Toast.LENGTH_SHORT).show();
                            readUserInfo();
                        }
                        else{
                            // If not -> Error, show a message and close the application
                            Toast.makeText(getApplicationContext(),R.string.rejected_login_or_permissions,Toast.LENGTH_LONG).show();
                            finishAndRemoveTask();
                        }
                    });
            activityResultLauncher.launch(i);
        }
    }

    private void readUserInfo(){
        // Set nav header information
        NavigationView navigationView = binding.navView;
        LinearLayout navHeaderView=(LinearLayout) navigationView.getHeaderView(0);

        if(account.getPhotoUrl() != null) {
            // User profile image
            // Download (from Internet) and set user image.
            // Download is performed any time, so that we can find any update of that image
            ImageView userImage = navHeaderView.findViewById(R.id.user_image);
            downloadImageFromInternet(userImage,account.getPhotoUrl().toString());
        }

        // Variables containing user information
        String accountGivenName = (account.getGivenName() == null) ? "" : account.getGivenName();
        String accountFamilyName = (account.getFamilyName() == null) ? "" : account.getFamilyName();
        String accountDisplayName = (account.getDisplayName() == null) ? "" : account.getDisplayName();
        String accountEmail = (account.getEmail() == null) ? "" : account.getEmail();

        // User name
        TextView userName = navHeaderView.findViewById(R.id.user_name);
        userName.setText(accountDisplayName);

        // User email
        TextView userEmail = navHeaderView.findViewById(R.id.user_email);
        userEmail.setText(accountEmail);

        //Save account information on shared preferences that will be used by AccountFragment
        String user_first_name_key = getString(R.string.user_first_name_preferences_key);
        String user_last_name_key = getString(R.string.user_last_name_preferences_key);
        String user_email_key = getString(R.string.user_email_preferences_key);
        mPreferences.edit().putString(user_first_name_key, accountGivenName).apply();
        mPreferences.edit().putString(user_last_name_key, accountFamilyName).apply();
        mPreferences.edit().putString(user_email_key, accountEmail).apply();

        Log.i(TAG, valueOf(mPreferences.getAll()));

    }

    private void showBluetoothAlert(){
        AlertDialog.Builder bluetooth_alert_builder = new AlertDialog.Builder(this)
                .setTitle(R.string.bluetooth_alert_title)
                .setMessage(R.string.bluetooth_alert_message)
                .setPositiveButton(
                        R.string.bluetooth_enable, (dialog, which) -> bluetooth.enableInterface(this))
                .setNegativeButton(R.string.bluetooth_ignore, (dialogInterface, i) -> {
                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    editor.putBoolean("bluetooth_alert", false);
                    editor.apply();
                })
                .setNeutralButton(R.string.bluetooth_neutral, null)
                .setIcon(R.drawable.ic_baseline_bluetooth_24);
        bluetooth_alert_builder.show();
    }

    // Android >= 12 bluetooth connection request run-time permission
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Bluetooth.BLUETOOTH_REQUEST_CODE) {
            // Checking whether user granted the permission or not
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Bluetooth permission granted");
                bluetooth.enableInterface(this);
            }
            else {
                Log.i(TAG,"BLE permission denied");
            }
        }
    }



    // Download user image from Internet
    private void downloadImageFromInternet(ImageView userImage, String imageURL){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Bitmap userImageBitmap;
                    InputStream in=new java.net.URL(imageURL).openStream();
                    userImageBitmap=BitmapFactory.decodeStream(in);
                    runOnUiThread(() -> {
                        if(userImageBitmap != null)
                            userImage.setImageBitmap(Bitmap.createScaledBitmap(userImageBitmap, 200, 200, false));
                    });
                } catch (Exception e) {
                    Log.e("Error Message", e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }
}