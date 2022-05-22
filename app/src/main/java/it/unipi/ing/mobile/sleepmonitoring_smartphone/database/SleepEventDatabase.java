package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import android.content.Context;

import androidx.room.Room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SleepEventDatabase {
    private static SleepEventDatabase instance = null;
    private final SleepEventDB database;
    private static final String DB_NAME  = "sleep_event_db.db";

    private String getCurrentTimestamp(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALY);
        return df.format(new Date());
    }

    public static SleepEventDatabase build(Context context){
        if (instance == null){
            instance = new SleepEventDatabase(context, null);
        }
        return instance;
    }

    public static SleepEventDatabase buildExample(Context context, String asset){
        if (instance == null){
            instance = new SleepEventDatabase(context, asset);
        }
        return instance;
    }

    private SleepEventDatabase(Context context, String asset){
        if (asset != null){
            database =  Room.databaseBuilder(context, SleepEventDB.class, DB_NAME)
                    .createFromAsset(asset)
                    .build();
        }
        else {
            database = Room.databaseBuilder(context, SleepEventDB.class, DB_NAME)
                    .build();
        }
    }

    // SLEEP EVENT

    public List<SleepEvent> getEvents(){ // TODO: debug only, remove
        return database.sleep_event_dao().getEvents();
    }

    public List<SleepEvent> getEventsByDate(String date){// TODO: remove
        return database.sleep_event_dao().getEventsByDate(date);
    }

    public List<SleepEvent> getEventsBySession(Long id){
        return database.sleep_event_dao().getEventsBySession(id);
    }

    public void insertSleepEvents(SleepEvent... sleep_events){
        database.sleep_event_dao().insertSleepEvents(sleep_events);
    }

    // SLEEP SESSION

    public List<SleepSession> getSessions(){ // TODO: debug only, remove
        return database.sleep_event_dao().getSessions();
    }

    public List<SleepSession> getSessionsByDate(String date){
        return database.sleep_event_dao().getSessionsByDate(date);
    }

    public void startSession(){
        database.sleep_event_dao().startSession(new SleepSession(getCurrentTimestamp()));
    }

    public void stopSession(){
        database.sleep_event_dao().stopSession(getCurrentTimestamp());
    }

    // REPORT

    public Report getLastReport(){
        SleepSession session = database.sleep_event_dao().getLastSession();
        List<SleepEvent> events = database.sleep_event_dao().getEventsBySession(session.getId());
        List<SleepEvent> filtered_events =
                events.stream()
                        .filter(sleepEvent -> sleepEvent.getTimestamp().substring(0,10).equals(session.getStop()))
                        .collect(Collectors.toList());

        return new Report(session.getStart(), session.getStop(), filtered_events);
    }

    public Report getReport(String date, Long session_id){
        SleepSession session = database.sleep_event_dao().getSessionById(session_id);
        List<SleepEvent> events = database.sleep_event_dao().getEventsBySession(session_id);
        List<SleepEvent> filtered_events =
                events.stream()
                        .filter(sleepEvent -> sleepEvent.getTimestamp().substring(0,10).equals(date))
                        .collect(Collectors.toList());

        return new Report(session.getStart(), session.getStop(), filtered_events);
    }

    // DELETE HISTORY

    public void deleteBefore(String date){ // TODO compatibility
        if (date == null){
            database.sleep_event_dao().deleteEventsBefore(getCurrentTimestamp());
            database.sleep_event_dao().deleteSessionsBefore(getCurrentTimestamp());
        }
        else {
            database.sleep_event_dao().deleteEventsBefore(date);
            database.sleep_event_dao().deleteSessionsBefore(date);
        }
    }
}
