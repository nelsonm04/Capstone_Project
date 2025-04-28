package com.example.capstone;

import com.google.cloud.firestore.Firestore;
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EventController implements Initializable {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button eventButton;

    @FXML
    private Button mainButton;

    @FXML
    private Label monthYear; // ID on top left

    @FXML
    private Button settingButton;

    @FXML
    private Button socialButton;

    @FXML
    private Label usernameDisplay;

    @FXML
    private Label weatherLabel;

    @FXML
    private ImageView profilePicture;

    @FXML
    private VBox eventListVBox;


    public void initialize(URL location, ResourceBundle resources) {
        // Switches the screen when clicked on
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        monthYear.setText(LocalDate.now().format(formatter));
        monthYear.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");


        loadUpcomingEvents();

    }

    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenName + ".fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) mainButton.getScene().getWindow(); // or any button
            Scene scene = new Scene(root);

            if (screenName.equals("MainScreen")) {
                scene.getStylesheets().add(getClass().getResource("/Styles/mainscreen.css").toExternalForm());
            }
            scene.getStylesheets().add(getClass().getResource("/styles/planet.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class EventData {
        String title;
        String time;
        LocalDate date;

        EventData(String title, String time, LocalDate date) {
            this.title = title;
            this.time = time;
            this.date = date;
        }
    }

    private void loadUpcomingEvents() {
        Firestore db = CapstoneApplication.fstore;
        String uid = Session.getUid();

        eventListVBox.getChildren().clear(); // Clear any old ones first

        try {
            var future = db.collection("users")
                    .document(uid)
                    .collection("events")
                    .orderBy("date")
                    .orderBy("time")
                    .get();

            var querySnapshot = future.get();

            List<EventData> events = new ArrayList<>();
            for (var doc : querySnapshot.getDocuments()) {
                String title = doc.getString("title");
                String time = doc.getString("time");
                String dateStr = doc.getString("date");

                if (title != null && time != null && dateStr != null) {
                    events.add(new EventData(title, time, LocalDate.parse(dateStr)));
                }
            }

            // Now display the sorted list
            for (EventData event : events) {
                Label eventLabel = new Label(event.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        + " | " + event.time + " | " + event.title);
                eventLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
                eventListVBox.getChildren().add(eventLabel);
            }

        } catch (Exception e) {
            System.out.println("Failed to load events: " + e.getMessage());
        }

    }


}
