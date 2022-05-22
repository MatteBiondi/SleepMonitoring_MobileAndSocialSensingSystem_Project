package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Locale;

import kotlin.random.Random;

@Entity(tableName = "sleep_session")
public class SleepSession {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "start") @NonNull
    private String start;

    @ColumnInfo(name = "stop")
    private String stop;
    
    public SleepSession(@NonNull String start) {
        this.start = start;
        this.stop = null;
    }

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    @NonNull
    public String getStart() {
        return start;
    }

    public void setStart(@NonNull String start) {
        this.start = start;
    }

    @NonNull
    public String getStop() {
        return stop;
    }

    public void setStop(@NonNull String stop) {
        this.stop = stop;
    }

    @Override @NonNull
    public String toString(){
        return String.format(Locale.ITALIAN, "<%s: %s,%s>", id, start, stop);
    }
}
