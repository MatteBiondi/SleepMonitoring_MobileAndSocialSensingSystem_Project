package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Class defining the SQLiteDB used to store data collected by the sleep monitoring app
 */
@Database(entities = {SleepEvent.class, SleepSession.class}, version = 1, exportSchema = false)
public abstract class SleepEventDB extends RoomDatabase {
    public abstract SleepEventDao sleep_event_dao();
}
