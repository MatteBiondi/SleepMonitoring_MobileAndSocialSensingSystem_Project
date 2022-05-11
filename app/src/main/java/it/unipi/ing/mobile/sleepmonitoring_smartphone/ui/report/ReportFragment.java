package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.report;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Calendar;

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;

public class ReportFragment extends Fragment {

    private ReportViewModel mViewModel;
    private final String TAG = "ReportFragment";
    private EditText date;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        date=view.findViewById(R.id.editText_date);
        date.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus)
                return;
            final Calendar cldr = Calendar.getInstance();
            //Get today date
            int currentDay = cldr.get(Calendar.DAY_OF_MONTH);
            int currentMonth = cldr.get(Calendar.MONTH);
            int currentYear = cldr.get(Calendar.YEAR);
            // date picker dialog
            DatePickerDialog datepicker = new DatePickerDialog(getActivity(),
                    (dp, year, monthOfYear, dayOfMonth) -> date.setText(
                            getString(R.string.report_date_format,
                            dayOfMonth,
                            monthOfYear + 1,
                            year)),
                    currentYear, currentMonth, currentDay);
            datepicker.show();
            date.clearFocus();
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
        // TODO: Use the ViewModel
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