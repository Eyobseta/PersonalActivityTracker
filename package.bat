@echo off
set JAR=dist\PersonalActivityTracker.jar
set MAIN_CLASS=com.tracker.ActivityTrackerApp
set OUTPUT_DIR=installer
set MODULE_PATH=C:\path\to\javafx-sdk\lib
set JAVA_HOME=C:\Program Files\Java\jdk-21

%JAVA_HOME%\bin\jlink --module-path "%MODULE_PATH%;%JAVA_HOME%\jmods" --add-modules javafx.controls,javafx.fxml,java.base --output "%OUTPUT_DIR%\runtime"

%JAVA_HOME%\bin\jpackage --type app-image --name "PersonalActivityTracker" --input dist --main-jar PersonalActivityTracker.jar --main-class %MAIN_CLASS% --runtime-image "%OUTPUT_DIR%\runtime" --icon src\com\tracker\icon.ico --dest "%OUTPUT_DIR%"