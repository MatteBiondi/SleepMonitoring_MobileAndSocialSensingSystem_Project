package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SleepEvent.class}, version = 1, exportSchema = false)
public abstract class SleepEventDB extends RoomDatabase {
    public abstract SleepEventDao sleep_event_dao();
}
