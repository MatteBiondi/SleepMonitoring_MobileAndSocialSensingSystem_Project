package it.unipi.ing.mobile.sleepmonitoring_smartphone;

import static java.lang.String.valueOf;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

import it.unipi.ing.mobile.sleepmonitoring_smartphone.bluetooth.Bluetooth;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Bluetooth bluetooth;

    private final String TAG = "SleepMonitoring_smartphone";

    private String sharedPrefFile;
    private SharedPreferences mPreferences;


    private GoogleSignInClient client;
    private GoogleSignInOptions signInOptions;
    private GoogleSignInAccount account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the shared preferences
        sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        //Restore Theme preferences
        String selectedTheme = mPreferences.getString(getString(R.string.theme_preferences_key),"Light");
        if(selectedTheme.equals("Light") || selectedTheme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

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
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Signin with Google credentials
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

        // onClickListener for exit button in navBar
        MenuItem exitItem = (MenuItem) navigationView.getMenu().findItem(R.id.nav_exit);
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

                finishAndRemoveTask();
            });

            return true;
        });

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

    // Disconnect Google account from the application
    public void revokeAccess(){
        client.revokeAccess()
                .addOnCompleteListener(this, task -> {
                    Toast.makeText(getApplicationContext(), R.string.disconnection, Toast.LENGTH_SHORT).show();
                    // Remove user info from shared preferences
                    mPreferences.edit().clear().apply();

                    //todo rimuovi ogni altro dato delle label, history etc

                    finishAndRemoveTask();
                });
    }

    //Funzione che richiede il login all'utente tramite le proprie credenziali Google
    private void signInWithGoogle(){
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.

        // Opzioni di sign-in e scopes richiesti per l'accesso a Google Drive
        // Configuro il sign-in per richiedere informazioni sull'ID utente, sull'email,
        // informazioni base dell'utente. ID e informazioni base sono incluse in DEFAULT_SIGN_IN
        signInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        //Ora costruisco un oggetto di classe GoogleSignInClient con le opzioni sopra specificate
        client = GoogleSignIn.getClient(this, signInOptions);

        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            requestSignIn();
        }
        else{
            readUserInfo();
        }

        //todo check se puoi fare qualcosa durante il toast e le string in file xml non utili
    }

    private void requestSignIn(){
        //Verifico che i servizi Google Play siano installati e disponibili per l'utilizzo
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            Toast.makeText(getApplicationContext(),R.string.failed_google_login,Toast.LENGTH_LONG).show();
            finishAndRemoveTask();
        } else {
            //todo
            //Il risultato del sign-in Intent è gestito in onActivityResult
            Intent i = client.getSignInIntent();
            ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
                            Toast.makeText(getApplicationContext(), R.string.signIn, Toast.LENGTH_SHORT).show();
                            readUserInfo();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),R.string.rejected_login_or_permissions,Toast.LENGTH_LONG).show();
                            finishAndRemoveTask();
                        }
                    });
            activityResultLauncher.launch(i);
        }
    }

    private void readUserInfo(){

        //todo è necessario farlo dalle info in account o posso prendere quelle in shared pref?

        // Set nav header information
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        LinearLayout navHeaderView=(LinearLayout) navigationView.getHeaderView(0);

        if(account.getPhotoUrl() != null) {
            // User image
            //Download and set user image. Download any time, so that we can find any update of it
            ImageView userImage = navHeaderView.findViewById(R.id.user_image);
            // Image link from internet
            new DownloadImageFromInternet(userImage).execute(account.getPhotoUrl().toString());
        }

        // User name
        TextView userName = navHeaderView.findViewById(R.id.user_name);
        userName.setText(account.getDisplayName());

        // User email
        TextView userEmail = navHeaderView.findViewById(R.id.user_email);
        userEmail.setText(account.getEmail());

        //Save account information on shared preferences for account fragment
        String user_first_name_key = getString(R.string.user_first_name_preferences_key);
        String user_last_name_key = getString(R.string.user_last_name_preferences_key);
        String user_email_key = getString(R.string.user_email_preferences_key);
        mPreferences.edit().putString(user_first_name_key, account.getGivenName()).apply();
        mPreferences.edit().putString(user_last_name_key, account.getFamilyName()).apply();
        mPreferences.edit().putString(user_email_key, account.getEmail()).apply();

        Log.i(TAG, valueOf(mPreferences.getAll()));

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



    // Inner class for async task that is used to download user image from Internet
    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {

        private final ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap userImageBitmap = null;
            try {
                String imageURL=urls[0];
                InputStream in=new java.net.URL(imageURL).openStream();
                userImageBitmap=BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return userImageBitmap;
        }
        protected void onPostExecute(Bitmap result) {
            if(result != null)
                imageView.setImageBitmap(Bitmap.createScaledBitmap(result, 200, 200, false));
        }
    }
}