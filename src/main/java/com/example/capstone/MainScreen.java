package com.example.capstone;

import com.google.cloud.firestore.Firestore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class MainScreen implements Initializable {

    @FXML
    private Label usernameDisplay;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYear;

    @FXML
    private Button prevMonthButton, nextMonthButton, addEventButton, mainButton, settingButton, socialButton, eventButton;

    private int currentMonth;
    private int currentYear;

    @FXML
    private Label weatherLabel;

    public void setUsername(String username) {
        usernameDisplay.setText(username);
    }

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

        // try {
        //     String weather = WeatherAPI.getWeather("New York");
        //     weatherLabel.setText(weather);
        // } catch (Exception ignored) {}
    }
    private void openAddEventDialog() {
        LocalDate today = LocalDate.now();
        StackPane fakeCell = new StackPane();  // 🔥 Just create a fake empty cell
        showEventDialog(today, fakeCell);
    }


    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/" + screenName + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/planet.css").toExternalForm());
            stage.setScene(scene);
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
            cell.setPrefSize(100, 100);

            if (date != null) {
                Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
                VBox vbox = new VBox(dateLabel);
                vbox.setSpacing(5);
                vbox.setMouseTransparent(true);
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

                LocalDate eventDate = LocalDate.parse(dateStr);

                Platform.runLater(() -> addEventToCalendarCell(eventDate, time + " - " + title));
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to load events: " + e.getMessage());
        }
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

                    Label eventLabel = new Label(eventText);
                    eventLabel.setStyle("-fx-font-size: 10; -fx-text-fill: white; -fx-cursor: hand;");
                    vbox.getChildren().add(eventLabel);
                    break;
                }
            }
        }
    }

    private void showEventDialog(LocalDate date, StackPane cell) {
        Dialog<EventData> dialog = new Dialog<>();
        dialog.setTitle("New Event for " + date.toString());

        Label nameLabel = new Label("Event:");
        TextField nameField = new TextField();

        Label timeLabel = new Label("Time:");
        TextField timeField = new TextField();
        timeField.setPromptText("e.g. 2:30 PM");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(timeLabel, 0, 1);
        grid.add(timeField, 1, 1);

        dialog.getDialogPane().setContent(grid);
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
                saveEventToFirestore(eventData.name, eventData.time, date);
                updateCalendar();
            }
        });
    }

    private void saveEventToFirestore(String title, String time, LocalDate date) {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", title);
        eventData.put("time", time);
        eventData.put("date", date.toString());
        eventData.put("createdAt", System.currentTimeMillis());

        try {
            db.collection("users")
                    .document(uid)
                    .collection("events")
                    .add(eventData)
                    .get();
            System.out.println("✅ Event saved!");
        } catch (Exception e) {
            System.out.println("❌ Failed to save event: " + e.getMessage());
        }
    }

    private static class EventData {
        String name;
        String time;

        EventData(String name, String time) {
            this.name = name;
            this.time = time;
        }
    }
}