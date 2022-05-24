package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.MainActivity;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;

public class AccountFragment extends Fragment {

    // Shared Preferences keys
    private String user_first_name_preferences_key;
    private String user_last_name_preferences_key;
    private String user_email_preferences_key;
    // Shared Preferences attribute
    private SharedPreferences mPreferences;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Set attributes for shared preferences
        initSharedPrefAttribute(inflater);

        // Get account information from sharedPreferences
        String firstName = mPreferences.getString(user_first_name_preferences_key, "");
        String lastName = mPreferences.getString(user_last_name_preferences_key, "");
        String email = mPreferences.getString(user_email_preferences_key, "");

        // Update UI
        setAccountInfoUI(view, firstName, lastName, email);

        // Define listeners
        defineListeners(view);

        return view;
    }

    private void setAccountInfoUI(View view, String firstName, String lastName, String email) {
        // Firstname field
        TextView firstNameField = view.findViewById(R.id.account_first_name_value);
        firstNameField.setText(firstName);

        // Lastname field
        TextView lastNameField = view.findViewById(R.id.account_last_name_value);
        lastNameField.setText(lastName);

        // Email field
        TextView emailField = view.findViewById(R.id.account_email_value);
        emailField.setText(email);
    }

    private void defineListeners(View view) {
        // Delete account button listener
        view.findViewById(R.id.account_delete_button).setOnClickListener(targetView -> {
            MainActivity mainActivity = ((MainActivity)getActivity());
            if(mainActivity != null)
                mainActivity.revokeAccess();
        });
    }

    private void initSharedPrefAttribute(LayoutInflater inflater) {
        // Shared Preferences file name
        String sharedPrefFile = getString(R.string.shared_preferences_file);
        // Init shared preference attribute
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // Initialize shared preference keys
        user_first_name_preferences_key = getString(R.string.user_first_name_preferences_key);
        user_last_name_preferences_key = getString(R.string.user_last_name_preferences_key);
        user_email_preferences_key = getString(R.string.user_email_preferences_key);
    }

}