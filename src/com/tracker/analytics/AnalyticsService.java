package com.tracker.analytics;

import com.tracker.model.ActivityRecord;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {

    public Map<String, Integer> totalTimePerActivity(List<ActivityRecord> records) {
        Map<String, Integer> map = new HashMap<>();
        for (ActivityRecord rec : records) {
            map.put(rec.getName(), map.getOrDefault(rec.getName(), 0) + rec.getDuration());
        }
        return map;
    }

    public String mostFrequentActivity(List<ActivityRecord> records) {
        if (records.isEmpty()) return "No data";
        return records.stream()
                .collect(Collectors.groupingBy(ActivityRecord::getName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    public Map<LocalDate, Integer> dailySummary(List<ActivityRecord> records) {
        Map<LocalDate, Integer> daily = new HashMap<>();
        for (ActivityRecord rec : records) {
            daily.put(rec.getDate(), daily.getOrDefault(rec.getDate(), 0) + rec.getDuration());
        }
        return daily;
    }

    public Map<String, Integer> weeklySummary(List<ActivityRecord> records) {
        Map<String, Integer> weekly = new HashMap<>();
        for (ActivityRecord rec : records) {
            LocalDate weekStart = rec.getDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            String key = weekStart.toString() + " to " + weekStart.plusDays(6);
            weekly.put(key, weekly.getOrDefault(key, 0) + rec.getDuration());
        }
        return weekly;
    }
}