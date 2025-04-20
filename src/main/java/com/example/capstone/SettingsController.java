package com.example.capstone;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private ImageView profileImageView, profilePicture;

    @FXML
    private TextField changeUserName;

    @FXML
    private Text changeAvatarText;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Screen switchers
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
            profileImageView.setImage(sessionImage);

            // 🔥 Clip both to circles
            Circle sidebarClip = new Circle(40, 40, 40); // sidebar
            profilePicture.setClip(sidebarClip);

            Circle centerClip = new Circle(135, 135, 135); // center big image
            profileImageView.setClip(centerClip);
        }

        // Setup clicking "Change Avatar"
        changeAvatarText.setOnMouseClicked(this::handleChangeAvatar);

        // Weather feature commented out
        // String weather = WeatherAPI.getWeather("New York");
        // weatherLabel.setText(weather);
    }


    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenName + ".fxml"));
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

    private void loadProfilePicture() {
        try {
            // Load default profile picture from resources
            Image defaultImage = new Image(getClass().getResource("/Images/avatar.png").toExternalForm());
            profilePicture.setImage(defaultImage);
            profileImageView.setImage(defaultImage);

            // Make images round
            Circle sidebarClip = new Circle(40, 40, 40); // Small one
            profilePicture.setClip(sidebarClip);

            Circle centerClip = new Circle(135, 135, 135); // Big one
            profileImageView.setClip(centerClip);
        } catch (Exception e) {
            System.out.println("❌ Failed to load default avatar: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangeAvatar(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Image newImage = new Image(selectedFile.toURI().toString());

            profilePicture.setImage(newImage);
            profileImageView.setImage(newImage);

            // 🔥 Save to session
            Session.setProfilePicture(newImage);

            // Re-clip
            Circle sidebarClip = new Circle(40, 40, 40);
            profilePicture.setClip(sidebarClip);

            Circle centerClip = new Circle(135, 135, 135);
            profileImageView.setClip(centerClip);

            System.out.println("✅ Profile picture updated and saved to session!");
        }
    }

}
