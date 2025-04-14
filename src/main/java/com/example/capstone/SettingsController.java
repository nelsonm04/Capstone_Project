package com.example.capstone;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private ImageView changeAvatar;

    @FXML
    private TextField changeUserName;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        // Switches the screen when clicked on
        mainButton.setOnAction(e -> handleScreenSwitch("MainScreen"));
        settingButton.setOnAction(e -> handleScreenSwitch("settingScreen"));
        socialButton.setOnAction(e -> handleScreenSwitch("socialScreen"));
        eventButton.setOnAction(e -> handleScreenSwitch("eventScreen"));

    }

    private void handleScreenSwitch(String screenName) {
        //Switching screens through buttons
        try {
            CapstoneApplication.setRoot(screenName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
