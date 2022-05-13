package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.databinding.FragmentHomeBinding;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.home.HomeFragmentDirections.ActionNavHomeToNavReport;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private String sharedPrefFile ;
    private String user_first_name_preferences_key;
    private SharedPreferences mPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set attributes for shared preferences
        sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        user_first_name_preferences_key = getString(R.string.user_first_name_preferences_key);

        // Get account information from sharedPreferences
        String firstName = mPreferences.getString(user_first_name_preferences_key, "");

        final TextView textView = binding.homeWelcomeLabel;
        textView.append(" "+firstName);


        // Set home fragment button listener for click event
        Button seeLastReport = binding.homeLastReportButton;
        seeLastReport.setOnClickListener(view -> {
            ActionNavHomeToNavReport action =HomeFragmentDirections.actionNavHomeToNavReport();
            action.setLastReport(true);
            Navigation.findNavController(view).navigate(action);
        });

        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}