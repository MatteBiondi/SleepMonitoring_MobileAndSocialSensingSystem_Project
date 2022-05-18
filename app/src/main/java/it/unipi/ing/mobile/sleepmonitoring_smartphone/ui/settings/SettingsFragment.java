package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.database.SleepEventDatabase;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private String sharedPrefFile;
    private String theme_preferences_label;


    private SharedPreferences mPreferences;
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        theme_preferences_label = getString(R.string.theme_preferences_key);

        reloadValuesFromSharedPref(view);
        defineListeners(view);

        return view;
    }

    private void reloadValuesFromSharedPref(View view){
        RadioButton radioButton;
        RadioGroup themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        //Theme
        String saved_theme = mPreferences.getString(theme_preferences_label, getString(R.string.light_theme));
        radioButton=themeRadioGroup.findViewWithTag(saved_theme);
        radioButton.setChecked(true);
    }

    private void defineListeners(View view){
        //Theme change
        RadioGroup themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the RadioButton selected
            RadioButton checkedRadioButton = view.findViewById(checkedId);
            String text = checkedRadioButton.getText().toString();
            mPreferences.edit().putString(theme_preferences_label, text).apply();
            if(text.equals("Light") || text.equals("light")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        // Delete hisotry
        Button deleteHistory = view.findViewById(R.id.delete_report_history_button);
        deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                View popupView = View.inflate(getContext(), R.layout.popup_delate_history, null);


                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);


                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                // Blur effect on the background
                getView().setAlpha((float) 0.2);

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        getView().setAlpha((float) 1);
                    }
                });

                // dismiss the popup window when button clicked
                Button startDelete = popupView.findViewById(R.id.start_delete_button);
                startDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // take the selected items from radio group
                        RadioGroup historyRadioGroup = view.getRootView().findViewById(R.id.history_radio_group);
                        RadioButton checked = view.getRootView().findViewById(historyRadioGroup.getCheckedRadioButtonId());


                        //Get current date
                        final Calendar cldr = Calendar.getInstance();
                        int day = cldr.get(Calendar.DAY_OF_MONTH);
                        int month = cldr.get(Calendar.MONTH);
                        int year = cldr.get(Calendar.YEAR);

                        // Create the selected date before which user want to delete reports
                        String date;

                        switch (checked.getText().toString()){
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
                        Toast.makeText(getActivity().getApplicationContext(), R.string.deleted_report_confirmation, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}