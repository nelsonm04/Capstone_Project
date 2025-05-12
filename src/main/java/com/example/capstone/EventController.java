package com.example.capstone;

import com.google.cloud.firestore.Firestore;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Event screen in the Capstone JavaFX application.
 * Handles loading and displaying upcoming and archived events,
 * user navigation, and session-based profile display.
 */
public class EventController implements Initializable {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button eventButton;

    @FXML
    private Button mainButton;

    @FXML
    private Label monthYear;

    @FXML
    private Button settingButton;

    @FXML
    private Button socialButton;

    @FXML
    private Label usernameDisplay, forecastLabel;

    @FXML
    private Label weatherLabel;

    @FXML
    private ImageView profilePicture;

    @FXML
    private VBox eventListVBox, rightPanelVBox;

    @FXML
    private Button signOutButton;

    /**
     * Initializes the controller. Sets up screen navigation buttons,
     * user profile display, and loads upcoming and archived events.
     *
     * @param location  location of the FXML file
     * @param resources resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
            Circle sidebarClip = new Circle(40, 40, 40);
            profilePicture.setClip(sidebarClip);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        monthYear.setText(LocalDate.now().format(formatter));
        monthYear.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        loadUpcomingEvents();
        loadArchivedEvents();
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
     * Switches to the specified screen and preserves current window settings.
     *
     * @param screenName the name of the FXML screen to switch to
     */
    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/" + screenName + ".fxml"));
            Parent root = loader.load();

            if ("socialScreen".equals(screenName)) {
                SocialController sc = loader.getController();
                sc.loadAllUsers();
                sc.loadFriends();
                sc.loadPendingRequests();
            }

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

    /**
     * Loads and displays upcoming events, including recurring ones, from Firestore.
     * Groups them into VBoxes for better display.
     */
    private void loadUpcomingEvents() {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();

        eventListVBox.getChildren().removeIf(node -> node instanceof HBox);

        try {
            var future = db.collection("users")
                    .document(uid)
                    .collection("events")
                    .orderBy("date")
                    .orderBy("time")
                    .get();

            var querySnapshot = future.get();

            List<EventData> events = new ArrayList<>();
            LocalDate today = LocalDate.now();
            LocalDate windowEnd = today.plusWeeks(4);

            for (var doc : querySnapshot.getDocuments()) {
                String title = doc.getString("title");
                String time = doc.getString("time");
                String dateStr = doc.getString("date");
                String repeat = doc.contains("repeat") ? doc.getString("repeat") : "None";
                String repeatEndStr = doc.contains("repeatEnd") ? doc.getString("repeatEnd") : null;

                if (title == null || time == null || dateStr == null) continue;

                LocalDate baseDate = LocalDate.parse(dateStr);
                LocalDate repeatEnd = repeatEndStr != null ? LocalDate.parse(repeatEndStr) : windowEnd;

                if (!baseDate.isBefore(today) && !baseDate.isAfter(windowEnd)) {
                    events.add(new EventData(title, time, baseDate));
                }

                if (!repeat.equalsIgnoreCase("None")) {
                    LocalDate next = baseDate;
                    while ((next = getNextOccurrence(next, repeat)) != null && !next.isAfter(repeatEnd)) {
                        if (!next.isBefore(today) && !next.isAfter(windowEnd)) {
                            events.add(new EventData(title, time, next));
                        }
                    }
                }
            }

            events.sort(Comparator.comparing(e -> e.date));

            int groupSize = 5;
            VBox currentGroup = new VBox(10);
            currentGroup.setStyle("-fx-padding: 10;");
            int count = 0;

            for (EventData event : events) {
                HBox eventBox = new HBox(10);
                eventBox.setStyle("-fx-padding: 10; -fx-background-color: #2a2a2a; -fx-background-radius: 8;");
                eventBox.setMinHeight(40);

                Label dateLabel = new Label(event.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                Label timeLabel = new Label(event.time);
                Label titleLabel = new Label(event.title);

                dateLabel.setMinWidth(120);
                timeLabel.setMinWidth(80);
                titleLabel.setMinWidth(200);

                for (Label label : List.of(dateLabel, timeLabel, titleLabel)) {
                    label.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
                }

                eventBox.getChildren().addAll(dateLabel, timeLabel, titleLabel);
                currentGroup.getChildren().add(eventBox);
                count++;

                if (count == groupSize) {
                    eventListVBox.getChildren().add(currentGroup);
                    currentGroup = new VBox(10);
                    currentGroup.setStyle("-fx-padding: 10;");
                    count = 0;
                }
            }

            if (!currentGroup.getChildren().isEmpty()) {
                eventListVBox.getChildren().add(currentGroup);
            }

        } catch (Exception e) {
            System.out.println("Failed to load events: " + e.getMessage());
        }
    }

    /**
     * Signs the user out and redirects to the Sign In screen.
     *
     * @param actionEvent the event triggered by clicking the Sign Out button
     */
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

    /**
     * Loads archived (past) events from Firestore and displays them in the right panel.
     */
    private void loadArchivedEvents() {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();

        VBox pastEventGroup = new VBox(10);
        pastEventGroup.setStyle("-fx-padding: 10;");

        try {
            var future = db.collection("users")
                    .document(uid)
                    .collection("archived_events")
                    .orderBy("date", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .get();

            var querySnapshot = future.get();

            for (var doc : querySnapshot.getDocuments()) {
                String title = doc.getString("title");
                String time = doc.getString("time");
                String dateStr = doc.getString("date");

                if (title == null || time == null || dateStr == null) continue;

                LocalDate date = LocalDate.parse(dateStr);

                HBox eventBox = new HBox(10);
                eventBox.setStyle("-fx-padding: 10; -fx-background-color: #333; -fx-background-radius: 8;");

                Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                Label timeLabel = new Label(time);
                Label titleLabel = new Label(title);

                for (Label label : List.of(dateLabel, timeLabel, titleLabel)) {
                    label.setStyle("-fx-font-size: 14px; -fx-text-fill: #ccc;");
                }

                eventBox.getChildren().addAll(dateLabel, timeLabel, titleLabel);
                pastEventGroup.getChildren().add(eventBox);
            }

            rightPanelVBox.getChildren().clear();
            Label header = new Label("📅 Past Events");
            header.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            rightPanelVBox.getChildren().addAll(header, pastEventGroup);

        } catch (Exception e) {
            System.out.println("Failed to load archived events: " + e.getMessage());
        }
    }

    /**
     * Calculates the next occurrence date of a repeating event.
     *
     * @param current    the current date
     * @param repeatType the repeat interval (e.g. "DAILY", "WEEKLY", etc.)
     * @return the next date of occurrence, or null if invalid repeat type
     */
    private LocalDate getNextOccurrence(LocalDate current, String repeatType) {
        return switch (repeatType.toUpperCase()) {
            case "DAILY" -> current.plusDays(1);
            case "WEEKLY" -> current.plusWeeks(1);
            case "MONTHLY" -> current.plusMonths(1);
            case "YEARLY" -> current.plusYears(1);
            default -> null;
        };
    }

    /**
     * Simple model class for storing event data (title, time, and date).
     */
    private static class EventData {
        String title;
        String time;
        LocalDate date;

        /**
         * Constructs a new EventData instance.
         *
         * @param title the event title
         * @param time  the event time
         * @param date  the event date
         */
        EventData(String title, String time, LocalDate date) {
            this.title = title;
            this.time = time;
            this.date = date;
        }
    }
}
