module PersonalActivityTracker {
    requires javafx.controls;
    requires javafx.fxml;
    
    exports com.tracker;
    exports com.tracker.controller;
    exports com.tracker.model;
    exports com.tracker.storage;
    exports com.tracker.analytics;
    
    opens com.tracker to javafx.fxml;
    opens com.tracker.controller to javafx.fxml;
}