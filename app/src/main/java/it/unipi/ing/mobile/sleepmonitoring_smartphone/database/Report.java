package it.unipi.ing.mobile.sleepmonitoring_smartphone.database;

import java.util.List;
import java.util.stream.Collectors;

public class Report {
    private final String start_timestamp;
    private final String stop_timestamp;
    private final List<SleepEvent> events;

    public Report(String start_timestamp, String stop_timestamp, List<SleepEvent> events){
        this.start_timestamp = start_timestamp;
        this.stop_timestamp = stop_timestamp;
        this.events = events;
    }

    public Report(String reference_date, String start_timestamp, String stop_timestamp, List<SleepEvent> events) {
        this.start_timestamp = (reference_date.equals(start_timestamp.substring(0,10))) ? start_timestamp : reference_date + " 00:00:00";
        this.stop_timestamp = (reference_date.equals(stop_timestamp.substring(0,10))) ? stop_timestamp : reference_date + " 23:59:59";
        this.events = events.stream()
                .filter(sleepEvent -> sleepEvent.getTimestamp().substring(0,10).equals(reference_date))
                .collect(Collectors.toList());
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
