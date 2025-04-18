package com.example.capstone;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainScreen implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set to current system month and year
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue(); // 1–12
        currentYear = now.getYear();

        updateCalendar(); // Load current month
        addEventButton.setOnAction(e -> openAddEventDialog());

        // Switches the screen when clicked on
        mainButton.setOnAction(e -> handleScreenSwitch("MainScreen"));
        settingButton.setOnAction(e -> handleScreenSwitch("settingScreen"));
        socialButton.setOnAction(e -> handleScreenSwitch("socialScreen"));
        eventButton.setOnAction(e -> handleScreenSwitch("eventScreen"));

    }

    private void handleScreenSwitch(String screenName) {
        try {
            CapstoneApplication.setRoot(screenName);
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
                vbox.setMouseTransparent(true); // So StackPane still gets clicks
                cell.getChildren().add(vbox);

                // ✅ Make cell clickable
                cell.setOnMouseClicked(e -> showEventDialog(date, cell));
            }

            calendarGrid.add(cell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
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


    private void showEventDialog(LocalDate date, StackPane cell) {
        Dialog<EventData> dialog = new Dialog<>();
        dialog.setTitle("New Event for " + date.toString());

        // Form inputs
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
            Label eventLabel = new Label(eventData.time + " - " + eventData.name);
            eventLabel.setStyle("-fx-font-size: 10; -fx-text-fill: blue; -fx-cursor: hand;");

            // 🔥 Add click-to-delete
            eventLabel.setOnMouseClicked(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Event");
                alert.setHeaderText("Delete this event?");
                alert.setContentText(eventLabel.getText());

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        VBox parent = (VBox) eventLabel.getParent();
                        parent.getChildren().remove(eventLabel);
                    }
                });
            });

            VBox eventBox = new VBox();
            eventBox.setSpacing(2);
//            eventBox.setMouseTransparent(true);

            List<Node> childrenCopy = new ArrayList<>(cell.getChildren());
            eventBox.getChildren().addAll(childrenCopy);
            eventBox.getChildren().add(eventLabel);

            cell.getChildren().setAll(eventBox);
        });
    }



    private static class EventDataWithDate {
        String name;
        String time;
        LocalDate date;

        EventDataWithDate(String name, String time, LocalDate date) {
            this.name = name;
            this.time = time;
            this.date = date;
        }
    }

    private void openAddEventDialog() {
        Dialog<EventDataWithDate> dialog = new Dialog<>();
        dialog.setTitle("Add New Event");

        Label nameLabel = new Label("Event:");
        TextField nameField = new TextField();

        Label timeLabel = new Label("Time:");
        TextField timeField = new TextField();
        timeField.setPromptText("e.g. 3:00 PM");

        Label dateLabel = new Label("Date:");
        DatePicker datePicker = new DatePicker();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(timeLabel, 0, 1);
        grid.add(timeField, 1, 1);
        grid.add(dateLabel, 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Add", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL
        );

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return new EventDataWithDate(
                        nameField.getText(),
                        timeField.getText(),
                        datePicker.getValue()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result.name != null && result.time != null && result.date != null) {
                loadCalendar(result.date.getYear(), result.date.getMonthValue());

                for (javafx.scene.Node node : calendarGrid.getChildren()) {
                    Integer col = GridPane.getColumnIndex(node);
                    Integer row = GridPane.getRowIndex(node);
                    if (row == null || row == 0 || col == null) continue;

                    int index = (row - 1) * 7 + col;
                    List<LocalDate> dates = CalendarUtils.getMonthDates(currentYear, currentMonth);
                    if (index >= 0 && index < dates.size()) {
                        LocalDate cellDate = dates.get(index);
                        if (result.date.equals(cellDate) && node instanceof StackPane) {

                            Label eventLabel = new Label(result.time + " - " + result.name);
                            eventLabel.setStyle("-fx-font-size: 10; -fx-text-fill: blue; -fx-cursor: hand;");

                            // 🔥 Delete on click
                            eventLabel.setOnMouseClicked(e -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Delete Event");
                                alert.setHeaderText("Delete this event?");
                                alert.setContentText(eventLabel.getText());

                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        VBox parent = (VBox) eventLabel.getParent();
                                        parent.getChildren().remove(eventLabel);
                                    }
                                });
                            });

                            VBox eventBox = new VBox();
                            eventBox.setSpacing(2);
                            eventBox.setMouseTransparent(true);

                            List<Node> originalChildren = new ArrayList<>(((StackPane) node).getChildren());
                            eventBox.getChildren().addAll(originalChildren);
                            eventBox.getChildren().add(eventLabel);

                            ((StackPane) node).getChildren().setAll(eventBox);
                            break;
                        }
                    }
                }
            }
        });
    }


    @FXML
    public void initialize() {
        String weather = WeatherAPI.getWeather("New York");
        weatherLabel.setText(weather);
    }



}
