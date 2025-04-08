package com.example.capstone;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainScreen implements Initializable {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYear;

    @FXML
    private Button prevMonthButton, nextMonthButton;

    private int currentMonth;
    private int currentYear;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set to current system month and year
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue(); // 1–12
        currentYear = now.getYear();

        updateCalendar(); // Load current month
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
                cell.getChildren().add(dateLabel);
            }

            calendarGrid.add(cell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }
}
