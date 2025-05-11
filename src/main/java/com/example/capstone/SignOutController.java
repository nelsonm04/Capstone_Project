package com.example.capstone;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * The controller for the sign-out functionality in the Capstone application.
 * This class handles the user sign-out process, including clearing the session
 * data and navigating the user back to the SignIn screen.
 */
public class SignOutController {

    @FXML
    private Button signOutButton;

    /**
     * Handles the action when the user clicks the "Sign Out" button.
     * Initiates the user sign-out process.
     *
     * @param event The action event triggered by the button click.
     */
    @FXML
    void handleSignOut(ActionEvent event) {
        signOutUser();
    }

    /**
     * Signs the user out by clearing session data and redirecting to the SignIn screen.
     * This method clears any session-related information and reloads the SignIn screen.
     */
    private void signOutUser() {
        // Clear session data
        Session.clearSession();

        // Redirect to SignIn screen
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/SignIn.fxml"));
            Parent root = loader.load();

            // Get the current window and set a new scene for the SignIn screen
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            Scene scene = new Scene(root);

            // Apply custom styles to the scene
            scene.getStylesheets().add(getClass().getResource("/styles/planet.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            // Print stack trace if an error occurs while loading the SignIn screen
            e.printStackTrace();
        }
    }
}
