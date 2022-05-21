package it.unipi.ing.mobile.sleepmonitoring_smartphone.ui.report;

import static android.content.Context.MODE_PRIVATE;

import static com.androidplot.xy.StepMode.INCREMENT_BY_VAL;
import static com.androidplot.xy.StepMode.SUBDIVIDE;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.TableOrder;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

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

import it.unipi.ing.mobile.sleepmonitoring_smartphone.R;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.database.SleepEvent;
import it.unipi.ing.mobile.sleepmonitoring_smartphone.database.SleepEventDatabase;

public class ReportFragment extends Fragment {

    private ReportViewModel mViewModel;
    private final String TAG = "ReportFragment";
    private EditText date;
    private XYPlot plot;
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


        plot=view.findViewById(R.id.report_plot);
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
                    (dp, year, monthOfYear, dayOfMonth) -> {
                        date.setText(
                                DateFormat.format(ReportFragment.this.getString(R.string.report_date_format),
                                        new GregorianCalendar(year, monthOfYear, dayOfMonth)));

                        // PLot update
                        plotUpdate(date.getText().toString());
                    },
                    currentYear, currentMonth, currentDay);
            datepicker.show();
            date.clearFocus();
        });

        // Get args to check if the request of creation come from home fragment button or not
        boolean fromHome = ReportFragmentArgs.fromBundle(getArguments()).getLastReport();
        //todo da sistemare e provare
        if(fromHome){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    SleepEventDatabase db = SleepEventDatabase.build(getContext());
                    List<SleepEvent> sleepEventList = db.getLastReport();
                    if (sleepEventList.size() <= 0)
                            return;
                    Activity mainActivity = getActivity();
                    if (mainActivity != null) {
                        mainActivity.runOnUiThread(() -> {
                            String extractedData = sleepEventList.get(0).getTimestamp();
                            SimpleDateFormat timestampFormat = new SimpleDateFormat(getString(R.string.timestamp_format), Locale.getDefault());
                            try {
                                Date dateLastReport = timestampFormat.parse(extractedData);
                                String requestedDate = DateFormat.format(getString(R.string.report_date_format), dateLastReport).toString();
                                // Set the current date on the editText and update the plot
                                date.setText(requestedDate);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    //Todo qui o dopo start?
                    // update the plot
                    plotUpdate(date.getText().toString());
                }
            }.start();
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

    public void plotUpdate(String date){
        new Thread(){
            @Override
            public void run() {
                super.run();
                plot.setVisibility(View.INVISIBLE);
                // Request data for the received date
                SleepEventDatabase db = SleepEventDatabase.build(getContext());
                List<SleepEvent> sleepEventList = db.getByDate(date);

                // If there are no data to plot
                if(sleepEventList.size() == 0){
                    Activity mainActivity = getActivity();
                    if (mainActivity != null) {
                        mainActivity.runOnUiThread(() -> {
                            Toast.makeText(getContext(),R.string.no_report_to_plot, Toast.LENGTH_SHORT).show();
                        });
                    }
                    return;
                }

                // Organize the legend item as in a table with 1 column and 3 rows
                plot.getLegend().setTableModel(new DynamicTableModel(1, 3, TableOrder.ROW_MAJOR));

                // Prepare movement label
                String microMovementLabel = getString(R.string.micro_movements_label);
                String macroMovementLabel = getString(R.string.macro_movements_label);
                String rolloverLabel = getString(R.string.roll_movements_label);

                // Data structure
                // Micro movements
                List<Long> miTimestamp = new ArrayList<>();
                List<Float> miValues = new ArrayList<>();
                // Macro movements
                List<Long> maTimestamp = new ArrayList<>();
                List<Float> maValues = new ArrayList<>();
                // Rollover
                List<Long> rTimestamp = new ArrayList<>();
                List<Float> rValues = new ArrayList<>();

                // Extract values from the list
                // Put timestamp and data values in the corresponding list depending on the type of action
                SimpleDateFormat timestampFormat = new SimpleDateFormat(getString(R.string.timestamp_format), Locale.getDefault());
                for (SleepEvent event : sleepEventList){
                    Date date;
                    try {
                        date = timestampFormat.parse(event.getTimestamp());
                        //todo assert: serve? c'è modo migliore?
                        assert date != null;
                        if(event.getEvent().equals(microMovementLabel)){
                            miTimestamp.add(date.getTime());
                            miValues.add(1f);
                        }else if(event.getEvent().equals(macroMovementLabel)){
                            maTimestamp.add(date.getTime());
                            maValues.add(2f);
                        }else if(event.getEvent().equals(rolloverLabel)){
                            rTimestamp.add(date.getTime());
                            rValues.add(3f);
                        }

                        //else discard it
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Distance between horizontal lines
                plot.setRangeStep(INCREMENT_BY_VAL, 1);
                plot.setRangeBoundaries(0,4, BoundaryMode.FIXED);

                // Subdivision of graph in vertical sections
                plot.setDomainStep(SUBDIVIDE, 10);
                // todo decidere plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 1800000);
                // Take the first time of the report and put it as lower boundary
                // Take the first time of the report and put it as upper boundary
                //todo plot.setDomainBoundaries(,,BoundaryMode.FIXED);

                plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new NumberFormat() {
                    @NonNull
                    @Override
                    public StringBuffer format(double epochTime, @NonNull StringBuffer stringBuffer, @NonNull FieldPosition fieldPosition) {
                        //Qui sono identificati gli n valori posti nel dominio, non corrispondono esattamente al tempo dell'evento
                        //Sono equidistribuiti fra il primo e l'ultimo valore
                        String timeFormatString = getString(R.string.report_time_format);
                        SimpleDateFormat timeFormat = new SimpleDateFormat(timeFormatString, Locale.getDefault());
                        Date selectedDateTime = new Date((long)epochTime);

                        return new StringBuffer(timeFormat.format(selectedDateTime));
                    }

                    @NonNull
                    @Override
                    public StringBuffer format(long epochTime, @NonNull StringBuffer stringBuffer, @NonNull FieldPosition fieldPosition) {
                        throw new UnsupportedOperationException("Not yet implemented.");
                    }

                    @Nullable
                    @Override
                    public Number parse(@NonNull String s, @NonNull ParsePosition parsePosition) {
                        throw new UnsupportedOperationException("Not yet implemented.");
                    }
                });

                // Update plot
                //MicroMovements
                String microMovementsTitle = getString(R.string.micro_movements_title);
                XYSeries microMovementsSeries = new SimpleXYSeries(miTimestamp, miValues, microMovementsTitle);
                //MacroMovements
                String macroMovementsTitle = getString(R.string.macro_movements_title);
                XYSeries macroMovementsSeries = new SimpleXYSeries(maTimestamp, maValues, macroMovementsTitle);
                //RolloverMovements
                String rollMovementsTitle = getString(R.string.roll_movements_title);
                XYSeries rollMovementsSeries = new SimpleXYSeries(rTimestamp, rValues, rollMovementsTitle);

                LineAndPointFormatter microMovementsFormat = new LineAndPointFormatter(null, Color.BLUE, null, null);
                microMovementsFormat.getVertexPaint().setStrokeWidth(30f);
                LineAndPointFormatter macroMovementsFormat = new LineAndPointFormatter(null, Color.RED, null, null);
                macroMovementsFormat.getVertexPaint().setStrokeWidth(30f);
                LineAndPointFormatter rollMovementsFormat = new LineAndPointFormatter(null, Color.GREEN, null, null);
                rollMovementsFormat.getVertexPaint().setStrokeWidth(30f);


                // Update Plot UI
                Activity mainActivity = getActivity();
                if (mainActivity != null) {
                    mainActivity.runOnUiThread(() -> {
                        plot.clear();

                        plot.setVisibility(View.VISIBLE);
                        plot.addSeries(microMovementsSeries, microMovementsFormat);
                        plot.addSeries(macroMovementsSeries, macroMovementsFormat);
                        plot.addSeries(rollMovementsSeries, rollMovementsFormat);

                        plot.redraw();
                    });
                }
            }
        }.start();
    }
}