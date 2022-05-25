package it.unipi.ing.mobile.sleepmonitoring.database;

import java.util.List;
import java.util.stream.Collectors;

public class Report {
    private final String start_timestamp;
    private final String stop_timestamp;
    private final List<SleepEvent> events;

    /**
     * Default constructor
     */
    public Report(String start_timestamp, String stop_timestamp, List<SleepEvent> events){
        this.start_timestamp = start_timestamp;
        this.stop_timestamp = stop_timestamp;
        this.events = events;
    }

    /**
     * The constructor takes only the part of sessions related to a reference date as argument, the
     * timestamp and event list are truncated if necessary
     */
    public Report(String reference_date, String start_timestamp, String stop_timestamp, List<SleepEvent> events) {
        // If the session starts before reference date the start timestamp is set at midnight
        this.start_timestamp =
                (reference_date.equals(start_timestamp.substring(0,10))) ? start_timestamp : reference_date + " 00:00:00";
        // If the the session ends after the reference date the end timestamp is truncated at the end of day
        this.stop_timestamp =
                (reference_date.equals(stop_timestamp.substring(0,10))) ? stop_timestamp : reference_date + " 23:59:59";

        // Filter the events by timestamp
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
