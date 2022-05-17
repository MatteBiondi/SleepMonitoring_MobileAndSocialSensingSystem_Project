package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import android.content.Context;
import androidx.room.Room;
import java.util.List;

public class SleepEventDatabase {
    private static SleepEventDatabase instance = null;
    private final SleepEventDB database;

    public static SleepEventDatabase build(Context context){
        if (instance == null){
            instance = new SleepEventDatabase(context);
        }
        return instance;
    }

    private SleepEventDatabase(Context context){
        database = Room.databaseBuilder(context, SleepEventDB.class, "sleep_event_db").build();
    }

    public List<SleepEvent> getAll(){
        return database.sleep_event_dao().getAll();
    }

    public List<SleepEvent> getByDate(String date){
        return database.sleep_event_dao().getByDate(date);
    }

    public void insertSleepEvents(SleepEvent... sleep_events){
        database.sleep_event_dao().insertSleepEvents(sleep_events);
    }

    public void deleteBefore(String date){
        database.sleep_event_dao().deleteBefore(date);
    }

}
