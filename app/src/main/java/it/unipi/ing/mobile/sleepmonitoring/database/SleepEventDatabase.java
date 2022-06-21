package it.unipi.ing.mobile.sleepmonitoring.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import it.unipi.ing.mobile.sleepmonitoring.R;

public class SleepEventDatabase {
    private static SleepEventDatabase instance = null;
    private final SleepEventDB database;
    private static String timestamp_format;
    private static List<Thread> threads;

    static {
        threads = new ArrayList<>();
    }

    public static String getCurrentTimestamp(){
        DateFormat df = new SimpleDateFormat(
                timestamp_format,
                Locale.ITALY
        );
        return df.format(new Date());
    }

    public static synchronized SleepEventDatabase build(Context context){
        //keep = keep + 1;

        if (!threads.contains(Thread.currentThread())){
            Log.i("DATABASE",
                    "Thread " + Thread.currentThread() + " is using database"
            );
            threads.add(Thread.currentThread());
        }

        timestamp_format = context.getString(R.string.timestamp_format);
        if (instance == null || !instance.database.isOpen()){
            Log.i("DATABASE", "Database opened");
            instance = new SleepEventDatabase(context, null);
        }
        return instance;
    }

    public static SleepEventDatabase buildExample(Context context, String asset){
        timestamp_format = context.getString(R.string.timestamp_format);
        if (instance == null){
            instance = new SleepEventDatabase(context, asset);
        }
        return instance;
    }

    public static synchronized void close(){
        threads = threads.stream()
                .filter(thread -> (thread.isAlive() && !Thread.currentThread().equals(thread)))
                .collect(Collectors.toList());

        Log.i("DATABASE", "Remaining still " + threads.size() + " threads using database");
        if (threads.size() == 0 && instance != null && instance.database.isOpen()){
            Log.i("DATABASE", "Database closed");
            instance.database.close();
            instance = null;
        }
    }

    private SleepEventDatabase(Context context, String asset){

        if (asset != null){
            database =  Room.databaseBuilder(context, SleepEventDB.class,
                           context.getString(R.string.db_name)
                    )
                    .createFromAsset(asset)
                    .build();
        }
        else {
            database = Room.databaseBuilder(context, SleepEventDB.class,
                            context.getString(R.string.db_name)
                    )
                    .build();
        }
    }

    // SLEEP EVENT

    public List<SleepEvent> getEvents(){ // debug only
        return database.sleep_event_dao().getEvents();
    }

    public List<SleepEvent> getEventsByDate(String date){// debug only
        return database.sleep_event_dao().getEventsByDate(date);
    }

    public List<SleepEvent> getEventsBySession(Long id){
        return database.sleep_event_dao().getEventsBySession(id);
    }

    public void insertSleepEvents(SleepEvent... sleep_events){
        database.sleep_event_dao().insertSleepEvents(sleep_events);
    }

    // SLEEP SESSION

    public List<SleepSession> getSessions(){ // debug only
        return database.sleep_event_dao().getSessions();
    }

    public List<SleepSession> getSessionsByDate(String date){
        List<SleepSession> sessions = database.sleep_event_dao().getSessionsByDate(date);
        sessions.forEach(session -> {
            if(!session.getStart().substring(0,10).equals(date))
                session.setStart(date + " 00:00:00");
            if(!session.getStop().substring(0,10).equals(date))
                session.setStop(date + " 23:59:59");
        });

        return sessions;
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
        if (session == null)
            return null;
        List<SleepEvent> events = database.sleep_event_dao().getEventsBySession(session.getId());

        return new Report(session.getStop().substring(0,10), session.getStart(), session.getStop(), events);
    }

    public Report getReport(String date, Long session_id){
        SleepSession session = database.sleep_event_dao().getSessionById(session_id);
        if (session == null)
            return null;
        List<SleepEvent> events = database.sleep_event_dao().getEventsBySession(session_id);

        return new Report(date, session.getStart(), session.getStop(), events);
    }

    // DELETE HISTORY

    public void deleteBefore(String date){
        if (date == null){
            database.sleep_event_dao().deleteEventsBefore(getCurrentTimestamp().substring(0,10));
            database.sleep_event_dao().deleteSessionsBefore(getCurrentTimestamp().substring(0,10));
        }
        else {
            database.sleep_event_dao().deleteEventsBefore(date);
            database.sleep_event_dao().deleteSessionsBefore(date);
        }
    }
}
