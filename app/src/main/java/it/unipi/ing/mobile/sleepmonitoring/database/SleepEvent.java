package it.unipi.ing.mobile.sleepmonitoring.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Class representing the sleep_event entity of SQLiteDB used to store the events tracked
 * by sleep monitoring
 */
@Entity(tableName = "sleep_event")
public class SleepEvent {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @NonNull
    private String timestamp;

    @ColumnInfo(name = "event") @NonNull
    private String event;

    public SleepEvent(@NonNull String timestamp, @NonNull String event){
        this.event = event;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@NonNull String timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    public String getEvent() {
        return event;
    }

    public void setEvent(@NonNull String event) {
        this.event = event;
    }

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    @NonNull @Override
    public String toString(){
        return String.format("<%s,%s>", timestamp, event);
    }
}
