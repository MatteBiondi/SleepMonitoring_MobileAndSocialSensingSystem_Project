package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.account;

import static android.content.Context.MODE_PRIVATE;

import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.MainActivity;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;

public class AccountFragment extends Fragment {

    private AccountViewModel mViewModel;

    private String sharedPrefFile ;
    private String user_first_name_preferences_key;
    private String user_last_name_preferences_key;
    private String user_email_preferences_key;
    private SharedPreferences mPreferences;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Set attributes for shared preferences
        sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        user_first_name_preferences_key = getString(R.string.user_first_name_preferences_key);
        user_last_name_preferences_key = getString(R.string.user_last_name_preferences_key);
        user_email_preferences_key = getString(R.string.user_email_preferences_key);

        // Get account information from sharedPreferences
        String firstName = mPreferences.getString(user_first_name_preferences_key, "");
        String lastName = mPreferences.getString(user_last_name_preferences_key, "");
        String email = mPreferences.getString(user_email_preferences_key, "");

        // Update UI
        TextView firstNameField = view.findViewById(R.id.account_first_name_value);
        firstNameField.setText(firstName);

        TextView lastNameField = view.findViewById(R.id.account_last_name_value);
        lastNameField.setText(lastName);

        TextView emailField = view.findViewById(R.id.account_email_value);
        emailField.setText(email);

        // Delete account button listener
        view.findViewById(R.id.account_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View targetView) {
                MainActivity mainActivity = ((MainActivity)getActivity());
                if(mainActivity != null)
                    mainActivity.revokeAccess();
            }
        });

        return view;
    }

}