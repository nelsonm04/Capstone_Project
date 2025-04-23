package com.example.capstone;

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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

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
    private Label usernameDisplay;

    @FXML
    private Label weatherLabel;

    @FXML
    private ImageView profilePicture;


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



    }

    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenName + ".fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) mainButton.getScene().getWindow(); // or any button
            Scene scene = new Scene(root);

            // 🛠 Attach CSS
            scene.getStylesheets().add(getClass().getResource("/styles/planet.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
