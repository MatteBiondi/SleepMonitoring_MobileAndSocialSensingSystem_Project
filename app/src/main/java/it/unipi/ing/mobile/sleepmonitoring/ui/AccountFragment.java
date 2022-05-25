package it.unipi.ing.mobile.sleepmonitoring.ui;

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

import it.unipi.ing.mobile.sleepmonitoring.MainActivity;
import it.unipi.ing.mobile.sleepmonitoring.R;
import it.unipi.ing.mobile.sleepmonitoring.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    // Shared Preferences keys
    private String user_first_name_preferences_key;
    private String user_last_name_preferences_key;
    private String user_email_preferences_key;
    // Shared Preferences
    private SharedPreferences mPreferences;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set attributes for shared preferences
        initSharedPrefAttributes(inflater);

        // Get account information from sharedPreferences
        String firstName = mPreferences.getString(user_first_name_preferences_key, "");
        String lastName = mPreferences.getString(user_last_name_preferences_key, "");
        String email = mPreferences.getString(user_email_preferences_key, "");

        // Update UI
        setAccountInfoUI(firstName, lastName, email);

        // Define listeners
        defineButtonListener();

        return root;
    }

    private void setAccountInfoUI(String firstName, String lastName, String email) {
        // Firstname field
        TextView firstNameField = binding.accountFirstNameValue;
        firstNameField.setText(firstName);

        // Lastname field
        TextView lastNameField = binding.accountLastNameValue;
        lastNameField.setText(lastName);

        // Email field
        TextView emailField = binding.accountEmailValue;
        emailField.setText(email);
    }

    private void defineButtonListener() {
        // Delete account button listener
        binding.accountDeleteButton.setOnClickListener(targetView -> {
            MainActivity mainActivity = ((MainActivity)getActivity());
            if(mainActivity != null)
                //Revoke user access
                mainActivity.revokeAccess();
        });
    }

    private void initSharedPrefAttributes(LayoutInflater inflater) {
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