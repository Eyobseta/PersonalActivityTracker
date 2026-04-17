package com.tracker.controller;

import com.tracker.model.ActivityRecord;
import com.tracker.storage.FileStorage;
import com.tracker.analytics.AnalyticsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML private TextField activityNameField;
    @FXML private TextField durationField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<ActivityRecord> tableView;
    @FXML private TableColumn<ActivityRecord, String> colName;
    @FXML private TableColumn<ActivityRecord, Integer> colDuration;
    @FXML private TableColumn<ActivityRecord, LocalDate> colDate;

    private ObservableList<ActivityRecord> data = FXCollections.observableArrayList();
    private FileStorage storage = new FileStorage();
    private AnalyticsService analytics = new AnalyticsService();

    @FXML
    public void initialize() {
        // Set up table columns
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tableView.setItems(data);

        // Load existing data from file
        loadFromFile();
    }

    @FXML
    private void handleAdd() {
        String name = activityNameField.getText().trim();
        String durText = durationField.getText().trim();
        LocalDate date = datePicker.getValue();

        if (name.isEmpty() || durText.isEmpty() || date == null) {
            showAlert("Input Error", "Please fill all fields and select a date.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durText);
            if (duration <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Invalid Duration", "Duration must be a positive integer (minutes).");
            return;
        }

        ActivityRecord newRecord = new ActivityRecord(name, duration, date);
        data.add(newRecord);
        // Append to file immediately (no overwrite)
        try {
            storage.appendRecord(newRecord);
        } catch (IOException e) {
            showAlert("File Error", "Could not save record: " + e.getMessage());
            data.remove(newRecord); // rollback
            return;
        }
        clearInputs();
    }

    @FXML
    private void handleSave() {
        // Save all current table data to file (overwrites)
        try {
            storage.saveAll(data);
            showAlert("Success", "All records saved to file.");
        } catch (IOException e) {
            showAlert("Save Error", "Could not save data: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoad() {
        loadFromFile();
    }

    @FXML
    private void handleAnalyze() {
        List<ActivityRecord> records = data;
        if (records.isEmpty()) {
            showAlert("No Data", "Add some activities before analyzing.");
            return;
        }

        // Compute statistics
        Map<String, Integer> totalTime = analytics.totalTimePerActivity(records);
        String mostFrequent = analytics.mostFrequentActivity(records);
        Map<LocalDate, Integer> daily = analytics.dailySummary(records);
        Map<String, Integer> weekly = analytics.weeklySummary(records);

        // Build a nice analysis window
        StringBuilder sb = new StringBuilder();
        sb.append("📊 ACTIVITY SUMMARY\n\n");
        sb.append("🏆 Most frequent activity: ").append(mostFrequent).append("\n\n");
        sb.append("⏱️ Total time per activity:\n");
        totalTime.forEach((act, mins) -> sb.append("   • ").append(act).append(": ").append(mins).append(" min\n"));
        sb.append("\n📅 Daily summary (minutes):\n");
        daily.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
                sb.append("   • ").append(e.getKey()).append(" → ").append(e.getValue()).append(" min\n"));
        sb.append("\n🗓️ Weekly summary (minutes):\n");
        weekly.forEach((week, mins) -> sb.append("   • ").append(week).append(" → ").append(mins).append(" min\n"));

        showAlert("Analytics Dashboard", sb.toString());
    }

    private void loadFromFile() {
        List<ActivityRecord> loaded = storage.loadAll();
        data.clear();
        data.addAll(loaded);
        showAlert("Load Complete", "Loaded " + loaded.size() + " records from file.");
    }

    private void clearInputs() {
        activityNameField.clear();
        durationField.clear();
        datePicker.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}