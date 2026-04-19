package com.tracker.controller;

import com.tracker.model.ActivityRecord;
import com.tracker.storage.FileStorage;
import com.tracker.analytics.AnalyticsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
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
    @FXML private Label statusLabel;

    private ObservableList<ActivityRecord> data = FXCollections.observableArrayList();
    private FileStorage storage = new FileStorage();
    private AnalyticsService analytics = new AnalyticsService();

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tableView.setItems(data);
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
        try {
            storage.appendRecord(newRecord);
            statusLabel.setText("✅ Added: " + name + " (" + duration + " min)");
        } catch (IOException e) {
            showAlert("File Error", "Could not save record: " + e.getMessage());
            data.remove(newRecord);
            statusLabel.setText("❌ Failed to add activity");
            return;
        }
        clearInputs();
    }

    @FXML
    private void handleSave() {
        try {
            storage.saveAll(data);
            statusLabel.setText("💾 All records saved to file.");
            showAlert("Success", "All records saved to file.");
        } catch (IOException e) {
            showAlert("Save Error", "Could not save data: " + e.getMessage());
            statusLabel.setText("❌ Save failed");
        }
    }

    @FXML
    private void handleLoad() {
        loadFromFile();
        statusLabel.setText("📂 Loaded data from file.");
    }

    @FXML
    private void handleDelete() {
        ActivityRecord selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an activity to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete '" + selected.getName() + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm deletion");
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            data.remove(selected);
            try {
                storage.saveAll(data); // sync file after deletion
                statusLabel.setText("🗑️ Deleted: " + selected.getName());
            } catch (IOException e) {
                showAlert("Error", "Could not update file after deletion.");
                statusLabel.setText("❌ Delete failed");
            }
        }
    }

    @FXML
    private void handleAnalyze() {
        if (data.isEmpty()) {
            showAlert("No Data", "Add some activities before analyzing.");
            return;
        }
        showAnalyticsDashboard();
    }

    private void loadFromFile() {
        List<ActivityRecord> loaded = storage.loadAll();
        data.clear();
        data.addAll(loaded);
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

    // ------------------- MODERN ANALYTICS DASHBOARD -------------------
    private void showAnalyticsDashboard() {
        Stage analyticsStage = new Stage();
        analyticsStage.setTitle("📊 Activity Analytics Dashboard");
        analyticsStage.setMinWidth(750);
        analyticsStage.setMinHeight(550);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Overview Tab
        Tab overviewTab = new Tab("📈 Overview");
        VBox overviewBox = new VBox(15);
        overviewBox.setStyle("-fx-padding: 20; -fx-background-color: #f4f7fc;");

        String mostFreq = analytics.mostFrequentActivity(data);
        Label mostFreqLabel = new Label("🏆 Most Frequent Activity: " + mostFreq);
        mostFreqLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label totalActivitiesLabel = new Label("📋 Total Activities Logged: " + data.size());
        totalActivitiesLabel.setStyle("-fx-font-size: 14px;");

        // Bar chart for total time per activity
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Minutes");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Total Time per Activity");
        barChart.setAnimated(true);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> totalTimeMap = analytics.totalTimePerActivity(data);
        for (Map.Entry<String, Integer> entry : totalTimeMap.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChart.getData().add(series);

        overviewBox.getChildren().addAll(mostFreqLabel, totalActivitiesLabel, barChart);
        overviewTab.setContent(overviewBox);

        // Daily Summary Tab
        Tab dailyTab = new Tab("📅 Daily Summary");
        TableView<Map.Entry<LocalDate, Integer>> dailyTable = new TableView<>();
        TableColumn<Map.Entry<LocalDate, Integer>, String> dateCol = new TableColumn<>("Date");
        TableColumn<Map.Entry<LocalDate, Integer>, Integer> minsCol = new TableColumn<>("Minutes");
        dateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getKey().toString()));
        minsCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getValue()).asObject());
        dailyTable.getColumns().addAll(dateCol, minsCol);
        ObservableList<Map.Entry<LocalDate, Integer>> dailyItems = FXCollections.observableArrayList(analytics.dailySummary(data).entrySet());
        dailyItems.sort((a,b) -> a.getKey().compareTo(b.getKey()));
        dailyTable.setItems(dailyItems);
        dailyTab.setContent(dailyTable);

        // Weekly Summary Tab
        Tab weeklyTab = new Tab("🗓️ Weekly Summary");
        TableView<Map.Entry<String, Integer>> weeklyTable = new TableView<>();
        TableColumn<Map.Entry<String, Integer>, String> weekCol = new TableColumn<>("Week (Mon-Sun)");
        TableColumn<Map.Entry<String, Integer>, Integer> weekMinsCol = new TableColumn<>("Total Minutes");
        weekCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getKey()));
        weekMinsCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getValue()).asObject());
        weeklyTable.getColumns().addAll(weekCol, weekMinsCol);
        ObservableList<Map.Entry<String, Integer>> weeklyItems = FXCollections.observableArrayList(analytics.weeklySummary(data).entrySet());
        weeklyItems.sort((a,b) -> a.getKey().compareTo(b.getKey()));
        weeklyTable.setItems(weeklyItems);
        weeklyTab.setContent(weeklyTable);

        tabPane.getTabs().addAll(overviewTab, dailyTab, weeklyTab);
        Scene scene = new Scene(tabPane);
        scene.getStylesheets().add(getClass().getResource("/com/tracker/style.css").toExternalForm());
        analyticsStage.setScene(scene);
        analyticsStage.show();
    }
}