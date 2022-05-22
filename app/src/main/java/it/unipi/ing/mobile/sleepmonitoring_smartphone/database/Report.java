package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import java.util.List;

public class Report {
    private final String start_timestamp;
    private final String stop_timestamp;
    private final List<SleepEvent> events;

    public Report(String start_timestamp, String stop_timestamp, List<SleepEvent> events){
        this.start_timestamp = start_timestamp;
        this.stop_timestamp = stop_timestamp;
        this.events = events;
    }

    public String getStartTimestamp(){
        return start_timestamp;
    }

    public String getStopTimestamp(){
        return stop_timestamp;
    }

    public List<SleepEvent> getEvents(){
        return events;
    }
}
