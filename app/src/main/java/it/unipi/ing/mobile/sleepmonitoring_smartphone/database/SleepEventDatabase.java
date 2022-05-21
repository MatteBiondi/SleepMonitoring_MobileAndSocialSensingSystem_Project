package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import android.content.Context;
import androidx.room.Room;
import java.util.List;

public class SleepEventDatabase {
    private static SleepEventDatabase instance = null;
    private final SleepEventDB database;

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
            database =  Room.databaseBuilder(context, SleepEventDB.class, "sleep_event_db")
                    .createFromAsset(asset)
                    .build();
        }
        else {
            database = Room.databaseBuilder(context, SleepEventDB.class, "sleep_event_db")
                    .build();
        }
    }

    public List<SleepEvent> getAll(){
        return database.sleep_event_dao().getAll();
    }

    public List<SleepEvent> getByDate(String date){
        return database.sleep_event_dao().getByDate(date);
    }

    public List<SleepEvent> getLastReport(){
        return database.sleep_event_dao().getLastReport();
    }

    public void insertSleepEvents(SleepEvent... sleep_events){
        database.sleep_event_dao().insertSleepEvents(sleep_events);
    }

    public void deleteBefore(String date){
        database.sleep_event_dao().deleteBefore(date);
    }

}
