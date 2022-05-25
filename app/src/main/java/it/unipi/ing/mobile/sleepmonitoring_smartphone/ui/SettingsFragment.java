package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.database.SleepEventDatabase;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    // Shared Preferences keys
    private String theme_preferences_key;
    // Shared Preferences
    private SharedPreferences mPreferences;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set attributes for shared preferences
        initSharedPrefAttributes(inflater);

        // Reload precedence value of the theme from preview application setting change
        reloadValuesFromSharedPref();

        // Define listeners for UI element
        defineThemeRadioGroupListeners();
        defineButtonListeners();

        return root;
    }

    private void initSharedPrefAttributes(LayoutInflater inflater) {
        // Shared Preferences file name
        String sharedPrefFile = getString(R.string.shared_preferences_file);
        // Init shared preference attribute
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        // Initialize shared preference keys
        theme_preferences_key = getString(R.string.theme_preferences_key);
    }

    private void reloadValuesFromSharedPref(){
        RadioButton radioButton;
        RadioGroup themeRadioGroup = binding.themeRadioGroup;
        // Get saved theme
        String saved_theme = mPreferences.getString(theme_preferences_key, getString(R.string.light_theme));
        // Checked the corresponding button
        radioButton=themeRadioGroup.findViewWithTag(saved_theme);
        radioButton.setChecked(true);
    }

    private void defineThemeRadioGroupListeners(){
        // on theme change
        RadioGroup themeRadioGroup = binding.themeRadioGroup;
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the selected RadioButton
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            String text = checkedRadioButton.getText().toString();
            // Save selected theme in the shared preferences
            mPreferences.edit().putString(theme_preferences_key, text).apply();
            // Change theme
            if(text.equals("Light") || text.equals("light")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }

    private void defineButtonListeners() {
        // Delete history
        Button deleteHistory = binding.deleteReportHistoryButton;
        deleteHistory.setOnClickListener(view -> {
            // Inflate the layout of the popup window
            View popupView = View.inflate(getContext(), R.layout.popup_delate_history, null);

            // Create the popup window
            // Taps outside the popup also dismiss it -> Focusable = true
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            // Blur effect on the background
            View v = getView();
            if(v != null)
                v.setAlpha((float) 0.2);

            // Define popup window's listeners
            definePopupWindowListeners(popupView, popupWindow);

        });
    }

    private void definePopupWindowListeners(View popupView, PopupWindow popupWindow) {
        // When the popup window is closed, remove blur effect
        popupWindow.setOnDismissListener(() ->{
            View view = getView();
            if(view != null)
                view.setAlpha((float) 1);
        });

        // Dismiss the popup window when button clicked
        Button startDelete = popupView.findViewById(R.id.start_delete_button);
        startDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Take the selected items from radio group
                RadioGroup historyRadioGroup = view.getRootView().findViewById(R.id.history_radio_group);
                RadioButton checked = view.getRootView().findViewById(historyRadioGroup.getCheckedRadioButtonId());

                // Compute the selected date
                String selectedItem = checked.getText().toString();
                String date = computeSelectedDate(selectedItem);

                // Delete reports
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        SleepEventDatabase db = SleepEventDatabase.build(getContext());
                        db.deleteBefore(date);
                    }
                }.start();

                // Close the popup window
                popupWindow.dismiss();

                // Toast message for user as confirmation
                Toast.makeText(getActivity(), R.string.deleted_report_confirmation, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String computeSelectedDate(String checked) {
        // Create the selected date before which user want to delete reports
        String date;

        //Get current date
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);

        // Computed the selected date as a date string with specific format
        switch (checked){
            case "Today":
                date=DateFormat.format(getString(R.string.report_date_format),
                        new GregorianCalendar(year,month,day)).toString();
                break;
            case "Last month":
                month = (month == 0)? 11 : month-1;
                date=DateFormat.format(getString(R.string.report_date_format),
                        new GregorianCalendar(year,month,day)).toString();
                break;
            case "Last six month":
                month = (month < 6)? 11 + (month - 6) : month-6;
                date=DateFormat.format(getString(R.string.report_date_format),
                        new GregorianCalendar(year,month,day)).toString();
                break;
            default:
                year = year - 1;
                date=DateFormat.format(getString(R.string.report_date_format),
                        new GregorianCalendar(year,month,day)).toString();
        }

        return date;
    }
}