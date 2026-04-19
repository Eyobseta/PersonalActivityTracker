#!/bin/bash
# Set paths
JAR="dist/PersonalActivityTracker.jar"
MAIN_CLASS="com.tracker.ActivityTrackerApp"
OUTPUT_DIR="installer"
MODULE_PATH="/usr/share/openjfx/lib"  # Change to your JavaFX lib path
JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))

# Create custom runtime with jlink
$JAVA_HOME/bin/jlink --module-path "$MODULE_PATH:$JAVA_HOME/jmods" \
  --add-modules javafx.controls,javafx.fxml,java.base \
  --output "$OUTPUT_DIR/runtime"

# Package the app
$JAVA_HOME/bin/jpackage \
  --type app-image \
  --name "PersonalActivityTracker" \
  --input dist \
  --main-jar PersonalActivityTracker.jar \
  --main-class $MAIN_CLASS \
  --runtime-image "$OUTPUT_DIR/runtime" \
  --icon src/com/tracker/icon.png \
  --dest "$OUTPUT_DIR"