package com.example.capstone;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

/**
 * The controller for the Sign-Up functionality in the Capstone application.
 * This class handles user account creation by validating inputs, interacting
 * with Firebase Authentication, and saving user data to Firestore.
 */
public class SignUpController {

    @FXML
    private Button backButton;

    @FXML
    private TextField signUpEmail;

    @FXML
    private PasswordField signUpPassword;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordMatch;

    @FXML
    private TextField userUsername;

    /**
     * Initializes the SignUpController. Adds a listener to the confirm password field
     * to check if the passwords match.
     */
    @FXML
    public void initialize() {
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordMatch();
        });
    }

    /**
     * Checks if the password and confirm password fields match and updates the
     * label with the corresponding message.
     */
    private void checkPasswordMatch() {
        if (signUpPassword.getText().equals(confirmPasswordField.getText())) {
            passwordMatch.setText("Passwords Match");
            passwordMatch.setStyle("-fx-text-fill: green;");
        } else {
            passwordMatch.setText("Passwords Don't Match");
            passwordMatch.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Navigates the user back to the SignIn screen.
     *
     * @throws IOException If an error occurs while loading the SignIn screen.
     */
    @FXML
    private void handleBack() throws IOException {
        CapstoneApplication.setRoot("SignIn");
    }

    /**
     * Handles the Sign-Up button click event. Validates user input, creates a new user
     * in Firebase Authentication, and saves user data to Firestore.
     *
     * @param event The action event triggered by the button click.
     */
    @FXML
    void handleSignUp(ActionEvent event) {
        String email = signUpEmail.getText();
        String password = signUpPassword.getText();
        String confirmPassword = confirmPasswordField.getText();
        String username = userUsername.getText();

        // Validate input fields
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            showAlert("Error", "All fields must be filled.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        try {
            // Create user in Firebase Authentication
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false)
                    .setDisplayName(username)
                    .setDisabled(false);

            UserRecord userRecord = CapstoneApplication.fauth.createUser(request);
            showAlert("Success", "Account created successfully.");

            // Create a new User object
            User newUser = new User(userRecord.getUid(), email, username);

            // Save user data to Firestore
            saveUserToDatabase(newUser);

            // Redirect to SignIn screen
            CapstoneApplication.setRoot("SignIn");

        } catch (FirebaseAuthException e) {
            showAlert("Error", "Failed to create account: " + e.getMessage());
        } catch (IOException e) {
            showAlert("Error", "Failed to return to login screen: " + e.getMessage());
        }
    }

    /**
     * Saves the newly created user to Firestore database.
     *
     * @param user The User object containing the user's information.
     */
    private void saveUserToDatabase(User user) {
        try {
            Firestore db = CapstoneApplication.fstore;
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.set(user.toMap());
        } catch (Exception e) {
            showAlert("Error", "Failed to save user to database: " + e.getMessage());
        }
    }

    /**
     * Displays an alert dialog with a given title and message.
     *
     * @param title   The title of the alert.
     * @param message The message content of the alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());
        alert.showAndWait();
    }
}
