package it.unipi.ing.mobile.sleepmonitoring.ui;

import static com.androidplot.xy.StepMode.INCREMENT_BY_VAL;
import static com.androidplot.xy.StepMode.SUBDIVIDE;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.TableOrder;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import it.unipi.ing.mobile.sleepmonitoring.R;
import it.unipi.ing.mobile.sleepmonitoring.database.Report;
import it.unipi.ing.mobile.sleepmonitoring.database.SleepEvent;
import it.unipi.ing.mobile.sleepmonitoring.database.SleepEventDatabase;
import it.unipi.ing.mobile.sleepmonitoring.database.SleepSession;
import it.unipi.ing.mobile.sleepmonitoring.databinding.FragmentReportBinding;

public class ReportFragment extends Fragment {
    FragmentReportBinding binding;
    // Tag attribute used in log
    private final String TAG = "ReportFragment";
    // Attribute for UI components
    private EditText date;
    private XYPlot plot;
    private Spinner spinner;
    // Attribute for MainActivity used to close app and run code on UI thread
    private Activity mainActivity;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentReportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Init class attributes
        initAttribute();

        // Define listeners for UI elements
        // Get context used as parameter
        defineDateFieldListener(inflater.getContext());
        defineSpinnerFieldListener(inflater.getContext());

        // Get args to check if the creation request come from home fragment button or not
        boolean fromHome = ReportFragmentArgs.fromBundle(getArguments()).getLastReport();
        if(fromHome){
            handleLastReportRequest(inflater.getContext());
        }

        return root;
    }

    private void defineDateFieldListener(Context context) {
        date.setOnFocusChangeListener((v, hasFocus) -> {
            // If we unfocus this element -> do nothing
            if(!hasFocus)
                return;

            // Clean UI from other elements
            mainActivity.runOnUiThread(()->spinner.setVisibility(View.GONE));
            mainActivity.runOnUiThread(()->plot.setVisibility(View.GONE));

            // Get current date
            final Calendar cldr = Calendar.getInstance();
            int currentDay = cldr.get(Calendar.DAY_OF_MONTH);
            int currentMonth = cldr.get(Calendar.MONTH);
            int currentYear = cldr.get(Calendar.YEAR);

            // Date picker dialog
            DatePickerDialog datepicker = new DatePickerDialog(getActivity(),
                    (dp, year, monthOfYear, dayOfMonth) -> {

                        // Selected date is written in date field using a specified format
                        String newDateValue =(String) DateFormat.format(ReportFragment.this.getString(R.string.report_date_format),
                                new GregorianCalendar(year, monthOfYear, dayOfMonth));

                        date.setText(newDateValue);

                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                // Request sessions for the selected date and populate spinner
                                SleepEventDatabase db = SleepEventDatabase.build(context);
                                populateSpinner(newDateValue, db);
                            }
                        }.start();
                    },
                    currentYear, currentMonth, currentDay);

            // Set constraint that user can't select date after today
            datepicker.getDatePicker().setMaxDate(new Date().getTime());
            // Show the date picker dialog
            datepicker.show();
            // Pass focus to the layout
            ConstraintLayout cl = (ConstraintLayout) date.getParent();
            cl.requestFocus();
            date.clearFocus();
        });
    }

    private void defineSpinnerFieldListener(Context context) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        // Hide plot element
                        mainActivity.runOnUiThread(()->plot.setVisibility(View.GONE));

                        //Take the selected item from the spinner
                        SleepSession selectedSession = (SleepSession) parent.getItemAtPosition(pos);

                        // Request events data for the received date
                        SleepEventDatabase db = SleepEventDatabase.build(context);

                        Report selectedReport = db.getReport(
                                date.getText().toString(),
                                selectedSession.getId()
                        );

                        if(selectedReport == null)
                            return;
                        List<SleepEvent> sleepEventList = getEventsFromReport(selectedReport);
                        if( sleepEventList == null)
                            return;

                        // Take start time and stop time for the events report
                        Date[] reportTimestamps = getReportTimestamps(selectedReport);
                        if(reportTimestamps[0] == null || reportTimestamps[1] == null)
                            // Error occurs
                            return;
                        Date startTimeDate = reportTimestamps[0];
                        Date stopTimeDate = reportTimestamps[1];

                        // Plot update
                        plotUpdate(sleepEventList, startTimeDate, stopTimeDate);
                    }
                }.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void handleLastReportRequest(Context context) {
        new Thread(){
            @Override
            public void run() {
                super.run();

                // Get movement events from last reported session
                SleepEventDatabase db = SleepEventDatabase.build(context);

                Report lastReport = db.getLastReport();
                if(lastReport == null) {
                    mainActivity.runOnUiThread(() -> Toast.makeText(
                            getContext(),
                            R.string.no_sessions_at_all,
                            Toast.LENGTH_SHORT
                    ).show());
                    return;
                }
                List<SleepEvent> sleepEventList = getEventsFromReport(lastReport);
                if(sleepEventList == null)
                    return;

                // Get start and stop timestamp
                Date[] reportTimestamps = getReportTimestamps(lastReport);
                if(reportTimestamps[0] == null || reportTimestamps[1] == null)
                    // Error occurs
                    return;
                Date lastReportStartDate = reportTimestamps[0];
                Date lastReportStopDate = reportTimestamps[1];

                // Extract selected date of the report
                String requestedDate = extractSelectedDate(lastReportStartDate);

                // Set the date of last report on the editText
                mainActivity.runOnUiThread(() -> date.setText(requestedDate));

                //Put session items in the spinner
                populateSpinner(requestedDate, db);

                // Select the last item
                mainActivity.runOnUiThread(()-> spinner.setSelection(spinner.getCount() - 1));

                // Update the plot
                plotUpdate(sleepEventList,lastReportStartDate, lastReportStopDate);

            }
        }.start();
    }

    private List<SleepEvent> getEventsFromReport(Report selectedReport) {
        List<SleepEvent> sleepEventList = selectedReport.getEvents();

        // If no movements are present -> Show toast and don't show plot
        if (sleepEventList.size() <= 0) {
            mainActivity.runOnUiThread(() -> Toast.makeText(
                    getContext(),
                    R.string.no_report_to_plot,
                    Toast.LENGTH_SHORT
            ).show());

            return null;
        }
        return sleepEventList;
    }

    private void populateSpinner(String requestedDate, SleepEventDatabase db) {
        // Get sessions of a requested date
        List<SleepSession> sessions = db.getSessionsByDate(requestedDate);
        // If there are no sessions in that date -> show a toast and don't create spinner
        if (sessions.size() <= 0) {
            mainActivity.runOnUiThread(() -> Toast.makeText(
                    getContext(),
                    R.string.no_sessions_in_date,
                    Toast.LENGTH_SHORT
            ).show());

            return;
        }
        ArrayAdapter<SleepSession> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                sessions
        );

        mainActivity.runOnUiThread(()->{
            spinner.setAdapter(adapter);
            spinner.setVisibility(View.VISIBLE);
        });
    }

    private String extractSelectedDate(Date date) {
        String requestedDate = "";
        try {
            // Identify the date of the last report
            requestedDate = DateFormat.format(getString(R.string.report_date_format), date).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestedDate;
    }

    private Date[] getReportTimestamps(Report report) {
        // Identify start and stop timestamp related to the selected session
        String reportStartString = report.getStartTimestamp();
        String reportStopString = report.getStopTimestamp();

        SimpleDateFormat timestampFormat = new SimpleDateFormat(
                getString(R.string.timestamp_format), Locale.getDefault());

        Date reportStartDate = null;
        Date reportStopDate = null;

        try {
            // Obtain timestamp in the specified format
            reportStartDate = timestampFormat.parse(reportStartString);
            reportStopDate = timestampFormat.parse(reportStopString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Date[]{reportStartDate,reportStopDate};
    }


    private void initAttribute() {
        // Get MainActivity instance
        mainActivity = getActivity();
        if (mainActivity == null) {
            // If it is not possible => error. Stop application
            Toast.makeText(getContext(), R.string.main_activity_error, Toast.LENGTH_SHORT).show();
            mainActivity.finishAndRemoveTask();
        }
        // UI elements
        plot=binding.reportPlot;
        date=binding.editTextDate;
        spinner=binding.reportSessionSpinner;
    }


    public void plotUpdate(List<SleepEvent> sleepEventList, Date startTime, Date stopTime){
        // If there are no data to plot
        if(sleepEventList.size() == 0){
            mainActivity.runOnUiThread(() -> Toast.makeText(
                    getContext(),
                    R.string.no_report_to_plot,
                    Toast.LENGTH_SHORT
            ).show());

            return;
        }

        // Data structures
        // Micro movements
        String microMovementsTitle = getString(R.string.micro_movements_title);
        MovementsList microML = new MovementsList(microMovementsTitle, Color.BLUE, 30f);
        // Macro movements
        String macroMovementsTitle = getString(R.string.macro_movements_title);
        MovementsList macroML = new MovementsList(macroMovementsTitle, Color.RED, 30f);
        // Rollover
        String rollMovementsTitle = getString(R.string.roll_movements_title);
        MovementsList rollML = new MovementsList(rollMovementsTitle, Color.GREEN, 30f);

        // Extract values from the list
        extractMovementFromEventsList(sleepEventList, microML, macroML, rollML);

        // Define graphic characteristics of the plot
        defineGraphicCharacteristics(startTime, stopTime);

        // Define how to manage domain axis values
        setManagingPolicyForDomainValues();

        // Update Plot and make it visible
        mainActivity.runOnUiThread(() -> {
            plot.clear();

            // Define series to be the plotted
            plot.addSeries(microML.fromListToSeries(), microML.getPointsFormat());
            plot.addSeries(macroML.fromListToSeries(), macroML.getPointsFormat());
            plot.addSeries(rollML.fromListToSeries(), rollML.getPointsFormat());

            plot.redraw();
            plot.setVisibility(View.VISIBLE);
        });
    }

    private void setManagingPolicyForDomainValues() {
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new NumberFormat() {
            @NonNull
            @Override
            public StringBuffer format(double epochTime,
                                       @NonNull StringBuffer stringBuffer,
                                       @NonNull FieldPosition fieldPosition) {
                /* The n values identified and placed in the domain axis do not correspond
                exactly to the time of the individual events. The vertical lines are equally
                distributed between the lower and upper bound */
                String timeFormatString = getString(R.string.report_time_format);
                SimpleDateFormat timeFormat = new SimpleDateFormat( timeFormatString,
                                                                    Locale.getDefault());
                Date selectedDateTime = new Date((long)epochTime);

                return new StringBuffer(timeFormat.format(selectedDateTime));
            }

            @NonNull
            @Override
            public StringBuffer format(long epochTime,
                                       @NonNull StringBuffer stringBuffer,
                                       @NonNull FieldPosition fieldPosition) {
                throw new UnsupportedOperationException("Not yet implemented.");
            }

            @Nullable
            @Override
            public Number parse(@NonNull String s, @NonNull ParsePosition parsePosition) {
                throw new UnsupportedOperationException("Not yet implemented.");
            }
        });
    }

    private void defineGraphicCharacteristics(Date startTime, Date stopTime) {
        // Organize the legend item as in a table with 1 column and 3 rows
        plot.getLegend().setTableModel(
                new DynamicTableModel(1, 3, TableOrder.ROW_MAJOR)
        );

        // Distance between horizontal lines and define fixed boundaries
        plot.setRangeStep(INCREMENT_BY_VAL, 1);
        plot.setRangeBoundaries(0,4, BoundaryMode.FIXED);

        // Subdivision of graph in vertical sections
        plot.setDomainStep(SUBDIVIDE, 10);

        // Define lower boundary and  upper boundary for plot domain
        if(startTime != null)
            plot.setDomainLowerBoundary((double)startTime.getTime(),BoundaryMode.FIXED);
        if(stopTime != null)
            plot.setDomainUpperBoundary((double)stopTime.getTime(),BoundaryMode.FIXED);
    }

    private void extractMovementFromEventsList(List<SleepEvent> sleepEventList,
                                               MovementsList micro,
                                               MovementsList macro,
                                               MovementsList roll) {
        // Prepare movement labels
        String microMovementLabel = getString(R.string.micro_movements_label);
        String macroMovementLabel = getString(R.string.macro_movements_label);
        String rolloverLabel = getString(R.string.roll_movements_label);

        // Put timestamp and data values in the corresponding list depending on the type of each action
        SimpleDateFormat timestampFormat = new SimpleDateFormat(
                getString(R.string.timestamp_format),
                Locale.getDefault()
        );

        for (SleepEvent event : sleepEventList){
            Date date;
            try {
                date = timestampFormat.parse(event.getTimestamp());
                if (date == null)
                    // Error-> Don't crash app. Discard data
                    continue;
                if(event.getEvent().equals(microMovementLabel)){
                    micro.addMovement(date.getTime(),1f);
                }else if(event.getEvent().equals(macroMovementLabel)){
                    macro.addMovement(date.getTime(),2f);
                }else if(event.getEvent().equals(rolloverLabel)){
                    roll.addMovement(date.getTime(),3f);
                }

                //else discard it
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

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

    // Static inner class to handle movements list and it's representation characteristics
    private static class MovementsList {
        private final List<Long> timestamp;
        private final List<Float> values;
        private final String title;

        private final LineAndPointFormatter microMovementsFormat;

        public MovementsList(String title, Integer color, Float size){
            timestamp = new ArrayList<>();
            values = new ArrayList<>();
            this.title = title;

            // Define characteristics of points
            microMovementsFormat = new LineAndPointFormatter(null, color, null, null);
            microMovementsFormat.getVertexPaint().setStrokeWidth(size);
        }

        public void addMovement(long time, float value) {
            // Add a movement event saving its timestamp and value
            timestamp.add(time);
            values.add(value);
        }

        public SimpleXYSeries fromListToSeries(){
            return new SimpleXYSeries(timestamp, values, title);
        }

        public LineAndPointFormatter getPointsFormat() {
            return  microMovementsFormat;
        }

    }
}