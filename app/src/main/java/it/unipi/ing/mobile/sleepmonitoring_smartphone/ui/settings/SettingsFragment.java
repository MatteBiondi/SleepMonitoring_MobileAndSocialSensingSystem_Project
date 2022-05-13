package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.settings;

import static android.content.Context.MODE_PRIVATE;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private String sharedPrefFile;
    private String font_preferences_label;
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
        font_preferences_label = getString(R.string.font_preferences_key);
        theme_preferences_label = getString(R.string.theme_preferences_key);

        reloadValuesFromSharedPref(view);
        defineListeners(view);

        //todo onClickListener for button in setting fragment

        return view;
    }

    private void reloadValuesFromSharedPref(View view){
        RadioButton radioButton;
        RadioGroup fontRadioGroup = view.findViewById(R.id.font_radio_group);
        RadioGroup themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        //Font size
        String saved_font_size = mPreferences.getString(font_preferences_label, getString(R.string.medium_font_size));
        radioButton=fontRadioGroup.findViewWithTag(saved_font_size);
        radioButton.setChecked(true);
        //Theme
        String saved_theme = mPreferences.getString(theme_preferences_label, getString(R.string.light_theme));
        radioButton=themeRadioGroup.findViewWithTag(saved_theme);
        radioButton.setChecked(true);
    }

    private void defineListeners(View view){
        //Font size change
        RadioGroup fontRadioGroup = view.findViewById(R.id.font_radio_group);
        fontRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the RadioButton selected
            RadioButton checkedRadioButton = view.findViewById(checkedId);
            String text = checkedRadioButton.getText().toString();
            mPreferences.edit().putString(font_preferences_label, text).apply();

            //todo applica le modifiche

        });

        //Theme change
        RadioGroup themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId is the RadioButton selected
            RadioButton checkedRadioButton = view.findViewById(checkedId);
            String text = checkedRadioButton.getText().toString();
            mPreferences.edit().putString(theme_preferences_label, text).apply();
            //todo applica le modifiche
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            //todo in report il grafico va dark
        });
    }
}