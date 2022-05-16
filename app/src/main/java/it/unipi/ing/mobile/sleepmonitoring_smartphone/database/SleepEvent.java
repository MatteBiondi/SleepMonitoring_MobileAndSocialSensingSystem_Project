package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_event")
public class SleepEvent {

    @PrimaryKey @NonNull
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
}
