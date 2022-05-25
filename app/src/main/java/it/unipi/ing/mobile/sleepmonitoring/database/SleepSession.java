package it.unipi.ing.mobile.sleepmonitoring.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class representing the sleep_session entity of SQLiteDB used to store the sessions tracked
 * by sleep monitoring
 */
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
        // The timestamp must be converted in a shorter format to be displayed on mobile app
        SimpleDateFormat full_sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ITALY
        );

        SimpleDateFormat short_sdf = new SimpleDateFormat(
               "HH:mm",
                Locale.ITALY
        );

        String start_short = "";
        String stop_short = "";

        try{
            Date start_full = full_sdf.parse(start);
            Date stop_full = full_sdf.parse(stop);
            if (start_full != null)
                start_short = short_sdf.format(start_full);
            if (stop_full != null)
                stop_short = short_sdf.format(stop_full);
        }
        catch (ParseException e){
            e.printStackTrace();

        }

        return String.format(Locale.ITALIAN, "%s - %s", start_short, stop_short);
    }
}
