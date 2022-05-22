package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SleepEventDao {

    // SLEEP EVENT

    @Query("SELECT * FROM sleep_event ORDER BY timestamp ASC")
    List<SleepEvent> getEvents();

    @Query("SELECT * FROM sleep_event WHERE date(timestamp) == :date ORDER BY timestamp ASC")
    List<SleepEvent> getEventsByDate(String date);

        @Query("SELECT * FROM sleep_event WHERE timestamp >= (SELECT start FROM sleep_session WHERE id =:id) " +
                "AND timestamp <= (SELECT stop FROM sleep_session WHERE id =:id) ORDER BY timestamp ASC")
    List<SleepEvent> getEventsBySession(Long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSleepEvents(SleepEvent... sleep_events);

    @Query("DELETE FROM sleep_event where date(timestamp) <= :date")
    void deleteEventsBefore(String date);


    // SLEEP SESSIONS

    @Query("SELECT * FROM sleep_session")
    List<SleepSession> getSessions();

    @Query("SELECT DISTINCT (id), start, stop FROM sleep_session WHERE date(start) == :date or date(stop) == :date ORDER BY stop")
    List<SleepSession> getSessionsByDate(String date);

    @Query("SELECT * FROM sleep_session WHERE id = :id")
    SleepSession getSessionById(Long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void startSession(SleepSession sleep_session);

    @Query("UPDATE sleep_session SET stop = :timestamp WHERE id = (SELECT MAX(id) from sleep_session)")
    void stopSession(String timestamp);

    @Query("SELECT * FROM sleep_session WHERE id = (SELECT MAX(id) from sleep_session)")
    SleepSession getLastSession();

    @Query("DELETE FROM sleep_session WHERE start < date(:timestamp)")
    void deleteSessionsBefore(String timestamp);
}
