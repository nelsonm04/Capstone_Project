package com.example.capstone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SignOutController {

    @FXML
    private Button signOutButton;

    @FXML
    void handleSignOut(ActionEvent event) {
        signOutUser();
    }

    private void signOutUser() {
        // Clear session data
        Session.clearSession();

        // Redirect to SignIn screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/SignIn.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signOutButton.getScene().getWindow();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("/styles/planet.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
