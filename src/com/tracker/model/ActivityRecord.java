package com.tracker.model;

import java.time.LocalDate;

public class ActivityRecord {
    private String name;
    private int duration;      // minutes
    private LocalDate date;

    public ActivityRecord(String name, int duration, LocalDate date) {
        this.name = name;
        this.duration = duration;
        this.date = date;
    }

    public String getName() { return name; }
    public int getDuration() { return duration; }
    public LocalDate getDate() { return date; }

    // For CSV storage: name,duration,date (yyyy-MM-dd)
    public String toCSV() {
        return name + "," + duration + "," + date.toString();
    }

    public static ActivityRecord fromCSV(String line) {
        String[] parts = line.split(",");
        if (parts.length != 3) return null;
        try {
            String name = parts[0];
            int duration = Integer.parseInt(parts[1]);
            LocalDate date = LocalDate.parse(parts[2]);
            return new ActivityRecord(name, duration, date);
        } catch (Exception e) {
            return null;
        }
    }
}