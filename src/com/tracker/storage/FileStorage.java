package com.tracker.storage;

import com.tracker.model.ActivityRecord;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileStorage {
    private static final String DATA_DIR = "data";
    private static final String FILE_NAME = "activities.csv";
    private final Path filePath;

    public FileStorage() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
        filePath = Paths.get(DATA_DIR, FILE_NAME);
    }

    public List<ActivityRecord> loadAll() {
        if (!Files.exists(filePath)) return new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            return reader.lines()
                    .map(ActivityRecord::fromCSV)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveAll(List<ActivityRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (ActivityRecord rec : records) {
                writer.write(rec.toCSV());
                writer.newLine();
            }
        }
    }

    public void appendRecord(ActivityRecord record) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(record.toCSV());
            writer.newLine();
        }
    }
}