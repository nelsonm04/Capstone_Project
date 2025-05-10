package com.example.capstone;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

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

    @FXML
    public void initialize() {
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordMatch();
        });
    }

    private void checkPasswordMatch() {
        if (signUpPassword.getText().equals(confirmPasswordField.getText())) {
            passwordMatch.setText("Passwords Match");
            passwordMatch.setStyle("-fx-text-fill: green;");
        } else {
            passwordMatch.setText("Passwords Don't Match");
            passwordMatch.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleBack() throws IOException {
        CapstoneApplication.setRoot("SignIn");
    }

    @FXML
    void handleSignUp(ActionEvent event) {
        String email = signUpEmail.getText();
        String password = signUpPassword.getText();
        String confirmPassword = confirmPasswordField.getText();
        String username = userUsername.getText();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            showAlert("Error", "All fields must be filled.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false)
                    .setDisplayName(username)
                    .setDisabled(false);

            UserRecord userRecord = CapstoneApplication.fauth.createUser(request);
            showAlert("Success", "Account created successfully: ");

            User newUser = new User(userRecord.getUid(), email, username);
            saveUserToDatabase(newUser);

            CapstoneApplication.setRoot("SignIn");

        } catch (FirebaseAuthException e) {
            showAlert("Error", "Failed to create account: " + e.getMessage());
        } catch (IOException e) {
            showAlert("Error", "Failed to return to login screen: " + e.getMessage());
        }
    }

    private void saveUserToDatabase(User user) {
        try {
            Firestore db = CapstoneApplication.fstore;
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.set(user.toMap());
        } catch (Exception e) {
            showAlert("Error", "Failed to save user to database: " + e.getMessage());
        }
    }

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
