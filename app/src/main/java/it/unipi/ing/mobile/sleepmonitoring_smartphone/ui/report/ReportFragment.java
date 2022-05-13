package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.report;

import static android.content.Context.MODE_PRIVATE;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;

public class ReportFragment extends Fragment {

    private ReportViewModel mViewModel;
    private final String TAG = "ReportFragment";
    private EditText date;
    private String sharedPrefFile;
    private SharedPreferences mPreferences;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        sharedPrefFile = getString(R.string.shared_preferences_file);
        mPreferences=inflater.getContext().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);


        date=view.findViewById(R.id.editText_date);
        date.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus)
                return;
            final Calendar cldr = Calendar.getInstance();
            //Get current date
            int currentDay = cldr.get(Calendar.DAY_OF_MONTH);
            int currentMonth = cldr.get(Calendar.MONTH);
            int currentYear = cldr.get(Calendar.YEAR);
            // date picker dialog
            DatePickerDialog datepicker = new DatePickerDialog(getActivity(),
                    (dp, year, monthOfYear, dayOfMonth) -> date.setText(
                            DateFormat.format("dd/MM/yyyy",
                                    new GregorianCalendar(year,monthOfYear,dayOfMonth))),
                    currentYear, currentMonth, currentDay);
            datepicker.show();
            date.clearFocus();
        });

        // Get args to check if the request of creation come from home fragment button or not
        boolean fromHome = ReportFragmentArgs.fromBundle(getArguments()).getLastReport();
        if(fromHome){
            // Set the current date on the editText and update the plot
            date.setText(DateFormat.format("dd/MM/yyyy", new Date()));

            //todo update the plot
        }

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG,"start");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"resume");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG,"pause");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG,"stop");
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        Log.i(TAG,"destroyview");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"destroy");
    }


}