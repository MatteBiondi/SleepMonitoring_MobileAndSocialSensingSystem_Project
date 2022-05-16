package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SleepEventDao {
    @Query("SELECT * FROM sleep_event")
    List<SleepEvent> getAll();

    @Query("SELECT * FROM sleep_event WHERE date(timestamp) == :date")
    List<SleepEvent> getByDate(String date);

    @Insert
    void insert(SleepEvent sleep_event);

    @Delete
    void deleteAll();
}
