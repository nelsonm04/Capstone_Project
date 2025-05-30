package com.example.capstone;

import com.google.cloud.firestore.Firestore;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Controller for the main screen of the application. Handles the display and interaction of the main user interface,
 * including calendar navigation, user profile, event creation, and handling user authentication.
 * Implements the {@link Initializable} interface for initializing UI components after they are loaded.
 */
public class MainScreen implements Initializable {

    @FXML
    private Label usernameDisplay;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYear;

    @FXML
    private ImageView profilePicture;

    @FXML
    private Button prevMonthButton, nextMonthButton, addEventButton, mainButton, settingButton, socialButton, eventButton;

    private int currentMonth;
    private int currentYear;
    private EventData lastDeletedEvent;
    private LocalDate lastDeletedDate;

    private Map<LocalDate, String> holidayMap = new HashMap<>();


    @FXML
    private Label weatherLabel;

    public void setUsername(String username) {
        usernameDisplay.setText(username);
    }

    private static User currentUser;
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    public static User getCurrentUser() {
        return currentUser;
    }

    User user = MainScreen.getCurrentUser();
    String uid = user.getUid();
    /**
     * Initializes the main screen, including setting the current date, configuring buttons,
     * and loading holidays for the current year.
     *
     * @param location The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue();
        currentYear = now.getYear();

        updateCalendar();
        addEventButton.setOnAction(e -> openAddEventDialog());

        mainButton.setOnAction(e -> handleScreenSwitch("MainScreen"));
        settingButton.setOnAction(e -> handleScreenSwitch("settingScreen"));
        socialButton.setOnAction(e -> handleScreenSwitch("socialScreen"));
        eventButton.setOnAction(e -> handleScreenSwitch("eventScreen"));

        if (Session.getUsername() != null) {
            usernameDisplay.setText(Session.getUsername());
        }

        if (Session.getProfilePicture() != null) {
            Image sessionImage = Session.getProfilePicture();

            profilePicture.setImage(sessionImage);

            Circle sidebarClip = new Circle(40, 40, 40); // sidebar
            profilePicture.setClip(sidebarClip);

        }
        new Thread(() -> {
            holidayMap = HolidayAPI.fetchUSHolidays(currentYear);
            Platform.runLater(this::updateCalendar);
        }).start();
        loadWeather("New York");

    }

    private void loadWeather(String city) {
        Task<String> t = new Task<>() {
            @Override
            protected String call() {
                return WeatherAPI.getWeatherFahrenheit("New York");
            }
        };
        t.setOnSucceeded(e -> weatherLabel.setText(t.getValue()));
        t.setOnFailed(e  -> weatherLabel.setText("Failed: " + t.getException().getMessage()));
        new Thread(t).start();

    }
    /**
     * Opens a dialog for adding a new event, allowing the user to input event details such as name,
     * time, date, repetition settings, and end date.
     */
    private void openAddEventDialog() {
        Dialog<EventDataWithDate> dialog = new Dialog<>();
        dialog.setTitle("New Event");

        // Form fields
        Label nameLabel = new Label("Event:");
        TextField nameField = new TextField();

        Label timeLabel = new Label("Time:");
        TextField timeField = new TextField();
        timeField.setPromptText("e.g. 2:30 PM");

        // Time formatting logic
        timeField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) return;
            String cleaned = newText.replaceAll("[^0-9APMapm:]", "").toUpperCase();
            if (cleaned.length() == 2 && !cleaned.contains(":")) {
                cleaned = cleaned.substring(0, 2) + ":";
            }
            if (!cleaned.matches("[0-9:APM]*")) {
                timeField.setText(oldText);
            } else {
                timeField.setText(cleaned);
            }
        });

        Label dateLabel = new Label("Date:");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Label repeatLabel = new Label("Repeat:");
        ComboBox<String> repeatBox = new ComboBox<>();
        repeatBox.getItems().addAll("None", "Daily", "Weekly", "Monthly", "Yearly");
        repeatBox.setValue("None");

        Label endDateLabel = new Label("Repeat Ends:");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Optional");

        // Grid layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(timeLabel, 0, 1);
        grid.add(timeField, 1, 1);
        grid.add(dateLabel, 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(repeatLabel, 0, 3);
        grid.add(repeatBox, 1, 3);
        grid.add(endDateLabel, 0, 4);
        grid.add(endDatePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new EventDataWithDate(
                        nameField.getText(),
                        timeField.getText(),
                        datePicker.getValue(),
                        repeatBox.getValue(),
                        endDatePicker.getValue() // ⬅ new field
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(eventData -> {
            if (eventData.name != null && eventData.date != null) {
                String timeInput = eventData.time == null ? "" : eventData.time.trim();

                if (!timeInput.isEmpty() && !isValidTime(timeInput)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid time format (e.g., 2:30PM) or leave it blank.");
                    alert.showAndWait();
                    return;
                }
                String timeToSave = timeInput.isEmpty() ? "N/A" : timeInput;
                saveEventToFirestore(eventData.name, timeToSave, eventData.date, eventData.repeat, eventData.endDate);
                updateCalendar();
            }
        });
    }
    @FXML
    private Button signOutButton;
    public void handleSignOut(javafx.event.ActionEvent actionEvent) {
        Session.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class EventDataWithDate {
        String name;
        String time;
        LocalDate date;
        String repeat;
        LocalDate endDate;

        EventDataWithDate(String name, String time, LocalDate date, String repeat,LocalDate endDate) {
            this.name = name;
            this.time = time;
            this.date = date;
            this.repeat = repeat;
            this.endDate = endDate;
        }
    }

    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/" + screenName + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainButton.getScene().getWindow();

            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();
            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(root);
            scene.getStylesheets().clear();

            if (screenName.equals("MainScreen")) {
                scene.getStylesheets().add(getClass().getResource("/Styles/mainscreen.css").toExternalForm());
            }
            scene.getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());

            stage.setScene(scene);

            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(isMaximized);
            stage.setFullScreen(isFullScreen);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCalendar() {
        loadCalendar(currentYear, currentMonth);
        updateMonthLabel();
    }

    private void updateMonthLabel() {
        String monthName = LocalDate.of(currentYear, currentMonth, 1)
                .getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthYear.setText(monthName + " " + currentYear);
    }

    @FXML
    private void goToNextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
        } else {
            currentMonth++;
        }
        updateCalendar();
    }

    @FXML
    private void goToPreviousMonth() {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
        } else {
            currentMonth--;
        }
        updateCalendar();
    }

    public void loadCalendar(int year, int month) {
        calendarGrid.getChildren().clear();

        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / 6);
            row.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(row);
        }

        List<LocalDate> dates = CalendarUtils.getMonthDates(year, month);
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            calendarGrid.add(dayLabel, i, 0);
        }

        int col = 0;
        int row = 1;

        for (LocalDate date : dates) {
            StackPane cell = new StackPane();
            cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setHgrow(cell, Priority.ALWAYS);
            GridPane.setVgrow(cell, Priority.ALWAYS);
            cell.setStyle("-fx-border-color: #888; -fx-border-width: 1px; -fx-border-style: solid;");


            if (date != null) {
                Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));


                VBox vbox = new VBox(dateLabel);
                vbox.setSpacing(5);
                vbox.setMouseTransparent(false);

                if (holidayMap != null && holidayMap.containsKey(date)) {
                    dateLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

                    Label holidayLabel = new Label(holidayMap.get(date));
                    holidayLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
                    holidayLabel.setWrapText(true);
                    vbox.getChildren().add(holidayLabel);
                }
                cell.getChildren().add(vbox);

                cell.setOnMouseClicked(e -> showEventDialog(date, cell));
            }
            calendarGrid.add(cell, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
        loadEventsFromFirestore(year, month);
    }

    private void loadEventsFromFirestore(int year, int month) {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        LocalDate today = LocalDate.now();

        try {
            var future = db.collection("users")
                    .document(uid)
                    .collection("events")
                    .whereGreaterThanOrEqualTo("date", firstDay.toString())
                    .whereLessThanOrEqualTo("date", lastDay.toString())
                    .get();

            var querySnapshot = future.get();

            for (var doc : querySnapshot.getDocuments()) {
                String title = doc.getString("title");
                String time = doc.getString("time");
                String dateStr = doc.getString("date");
                String repeat = doc.contains("repeat") ? doc.getString("repeat") : "None";
                String repeatEndStr = doc.contains("repeatEnd") ? doc.getString("repeatEnd") : null;

                if (title == null || time == null || dateStr == null) continue;

                LocalDate eventDate = LocalDate.parse(dateStr);

                if (eventDate.isBefore(today) && repeat.equalsIgnoreCase("None")) {
                    // Archive the document before deleting
                    Map<String, Object> archivedData = new HashMap<>(doc.getData());

                    db.collection("users")
                            .document(uid)
                            .collection("archived_events")
                            .add(archivedData)
                            .get(); // Optional: wait for completion

                    doc.getReference().delete();
                    continue;
                }

                LocalDate repeatEnd = null;
                if (repeatEndStr != null && !repeatEndStr.isEmpty()) {
                    repeatEnd = LocalDate.parse(repeatEndStr);
                }

                List<LocalDate> allDates = CalendarUtils.getMonthDates(year, month);

                for (LocalDate day : allDates) {
                    if (day == null) continue;

                    boolean matches = eventDate.equals(day) || isRecurringOnDate(eventDate, repeat, day);
                    boolean withinRepeatRange = repeatEnd == null || !day.isAfter(repeatEnd);

                    if (matches && withinRepeatRange && !day.isBefore(today)) {
                        LocalDate finalDay = day;
                        Platform.runLater(() ->
                                addEventToCalendarCell(finalDay, time + " - " + title)
                        );
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to load events: " + e.getMessage());
        }
    }



    private boolean isRecurringOnDate(LocalDate baseDate, String repeatType, LocalDate targetDate) {
        return switch (repeatType.toUpperCase()) {
            case "DAILY" -> !targetDate.isBefore(baseDate);
            case "WEEKLY" -> !targetDate.isBefore(baseDate)
                    && baseDate.getDayOfWeek() == targetDate.getDayOfWeek();
            case "MONTHLY" -> !targetDate.isBefore(baseDate)
                    && baseDate.getDayOfMonth() == targetDate.getDayOfMonth();
            case "YEARLY" -> baseDate.getDayOfMonth() == targetDate.getDayOfMonth()
                    && baseDate.getMonth() == targetDate.getMonth();
            default -> false;
        };
    }


    private void addEventToCalendarCell(LocalDate date, String eventText) {
        for (Node node : calendarGrid.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            if (row == null || row == 0 || col == null) continue;

            int index = (row - 1) * 7 + col;
            List<LocalDate> dates = CalendarUtils.getMonthDates(currentYear, currentMonth);
            if (index >= 0 && index < dates.size()) {
                LocalDate cellDate = dates.get(index);
                if (date.equals(cellDate) && node instanceof StackPane stackPane) {
                    VBox vbox;

                    if (!stackPane.getChildren().isEmpty() && stackPane.getChildren().get(0) instanceof VBox) {
                        vbox = (VBox) stackPane.getChildren().get(0);
                    } else {
                        vbox = new VBox();
                        vbox.getChildren().addAll(stackPane.getChildren());
                        stackPane.getChildren().setAll(vbox);
                    }
                    // Events Labels
                    String displayText = eventText.startsWith("N/A - ") ? eventText.substring(6) : eventText;
                    Label eventLabel = new Label(displayText);
                    eventLabel.setStyle("-fx-font-size: 14; -fx-text-fill: white; -fx-cursor: hand;");

                    eventLabel.setOnMouseClicked(e -> {
                        e.consume();
                        showEditOrDeleteDialog(eventText, date);
                    });
                    vbox.getChildren().add(eventLabel);
                    break;
                }
            }
        }
    }
    private void showEditOrDeleteDialog(String oldEventText, LocalDate date) {
        String[] parts = oldEventText.split(" - ", 2);
        if (parts.length != 2) return;

        String oldTime = parts[0].trim();
        String oldTitle = parts[1].trim();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Event");

        Label nameLabel = new Label("Event:");
        TextField nameField = new TextField(oldTitle);

        Label timeLabel = new Label("Time:");
        TextField timeField = new TextField(oldTime);
        timeField.setPromptText("e.g. 2:30PM");

        // Typing restriction (allow only digits, colon, A/P/M, no spaces)
        timeField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                return;
            }
            String cleaned = newText.replaceAll("[^0-9APMapm:]", "").toUpperCase();

            // If user typed 2 digits and no colon yet, auto-insert colon
            if (cleaned.length() == 2 && !cleaned.contains(":")) {
                cleaned = cleaned.substring(0, 2) + ":";
            }

            // Only allow 0-9, :, A, P, M
            if (!cleaned.matches("[0-9:APM]*")) {
                timeField.setText(oldText);
            } else {
                timeField.setText(cleaned);
            }
        });


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(timeLabel, 0, 1);
        grid.add(timeField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");


        // Two buttons: Save and Delete
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
        ButtonType cancelButtonType = ButtonType.CANCEL;

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> dialogButton);


        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                if (!nameField.getText().isEmpty() && !timeField.getText().isEmpty()) {
                    if (!timeField.getText().matches("^\\d{1,2}:\\d{2}(AM|PM)$")) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid time format (e.g., 2:30PM).");
                        alert.showAndWait();
                        return;
                    }
                    String[] timeParts = timeField.getText().replace("AM", "").replace("PM", "").split(":");
                    try {
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);
                        if (hour < 1 || hour > 12 || minute < 0 || minute > 59) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid time (1-12 hours and 00-59 minutes).");
                            alert.showAndWait();
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid time format.");
                        alert.showAndWait();
                        return;
                    }

                    updateEventInFirestore(oldTitle, oldTime, date, nameField.getText(), timeField.getText());
                    updateCalendar();
                }
            } else if (result == deleteButtonType) {
                deleteEventFromFirestore(oldTime + " - " + oldTitle, date);
                updateCalendar();
            }
            // Cancel does nothing
        });

    }

    private void updateEventInFirestore(String oldTitle, String oldTime, LocalDate date, String newTitle, String newTime) {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();

        try {
            var docs = db.collection("users")
                    .document(uid)
                    .collection("events")
                    .whereEqualTo("title", oldTitle)
                    .whereEqualTo("time", oldTime)
                    .whereEqualTo("date", date.toString())
                    .get()
                    .get()
                    .getDocuments();

            for (var doc : docs) {
                doc.getReference().update("title", newTitle, "time", newTime);
            }

            System.out.println("Event updated!");
        } catch (Exception e) {
            System.out.println("Failed to update event: " + e.getMessage());
        }
    }

    private void deleteEventFromFirestore(String eventText, LocalDate date) {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();

        String[] parts = eventText.split(" - ", 2);
        if (parts.length != 2) return;

        String time = parts[0].trim();
        String title = parts[1].trim();

        try {
            // Finds the event that matches the label
            var docs = db.collection("users")
                    .document(uid)
                    .collection("events")
                    .whereEqualTo("title", title)
                    .whereEqualTo("time", time)
                    .whereEqualTo("date", date.toString())
                    .get()
                    .get()
                    .getDocuments();

            if (!docs.isEmpty()) {
                lastDeletedEvent = new EventData(title, time);
                lastDeletedDate = date;

                docs.forEach(doc -> doc.getReference().delete());

                showUndoDialog();
            }

        } catch (Exception e) {
            System.out.println("Failed to delete: " + title);
            e.printStackTrace();
        }
    }
    private void showUndoDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Event Deleted");
        alert.setHeaderText(null);
        alert.setContentText("Event deleted. Click OK to undo.");

        ButtonType undoButton = new ButtonType("Undo");
        ButtonType closeButton = new ButtonType("Dismiss", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(undoButton, closeButton);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());

        alert.showAndWait().ifPresent(response -> {
            if (response == undoButton && lastDeletedEvent != null && lastDeletedDate != null) {
                saveEventToFirestore(lastDeletedEvent.name, lastDeletedEvent.time, lastDeletedDate, "None", null);

                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {}

                    Platform.runLater(() -> updateCalendar());
                }).start();
            }
            lastDeletedEvent = null;
            lastDeletedDate = null;
        });
    }


    private void showEventDialog(LocalDate date, StackPane cell) {
        Dialog<EventData> dialog = new Dialog<>();

        dialog.setTitle("New Event for " + date.toString());

        Label nameLabel = new Label("Event:");
        TextField nameField = new TextField();

        Label timeLabel = new Label("Time:");
        TextField timeField = new TextField();
        timeField.setPromptText("e.g. 2:30PM");

        timeField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                return;
            }
            String cleaned = newText.replaceAll("[^0-9APMapm:]", "").toUpperCase();

            // If user typed 2 digits and no colon yet, auto-insert colon
            if (cleaned.length() == 2 && !cleaned.contains(":")) {
                cleaned = cleaned.substring(0, 2) + ":";
            }

            // Only allow 0-9, :, A, P, M
            if (!cleaned.matches("[0-9:APM]*")) {
                timeField.setText(oldText);
            } else {
                timeField.setText(cleaned);
            }
        });


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(timeLabel, 0, 1);
        grid.add(timeField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new EventData(nameField.getText(), timeField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(eventData -> {
            if (eventData.name != null && eventData.time != null) {
                String timeInput = eventData.time.trim();

                if (!timeInput.isEmpty() && !isValidTime(timeInput)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter a valid time format (e.g., 2:30PM) or leave it blank.");
                    alert.showAndWait();
                    return;
                }

                String timeToSave = timeInput.isEmpty() ? "N/A" : timeInput;

                saveEventToFirestore(eventData.name, timeToSave, date, "None", null);
                updateCalendar();
            }
        });

    }

    private void saveEventToFirestore(String title, String time, LocalDate date, String repeat, LocalDate endDate) {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", title);
        eventData.put("time", time);
        eventData.put("date", date.toString());
        eventData.put("repeat", repeat);
        eventData.put("repeatEnd", endDate != null ? endDate.toString() : null);
        eventData.put("createdAt", System.currentTimeMillis());

        try {
            db.collection("users")
                    .document(uid)
                    .collection("events")
                    .add(eventData)
                    .get();
            System.out.println("Event saved!");
        } catch (Exception e) {
            System.out.println("Failed to save event: " + e.getMessage());
        }
    }


    private static class EventData {
        String name;
        String time;
        String repeat;

        EventData(String name, String time) {
            this.name = name;
            this.time = time;
            this.repeat = repeat;
        }
    }

    private boolean isValidTime(String time) {
        if (!time.matches("^\\d{1,2}:\\d{2}(AM|PM)$")) {
            return false; // wrong pattern
        }
        String[] parts = time.replace("AM", "").replace("PM", "").split(":");
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 1 || hour > 12 || minute < 0 || minute > 59) {
                return false; // invalid hour or minute
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}