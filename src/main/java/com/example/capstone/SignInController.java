package com.example.capstone;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.remoteconfig.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SignInController {

    @FXML
    private TextField userEmail;

    @FXML
    private PasswordField userPassword;

    @FXML
    private Button userLogIn;

    @FXML
    private Button userSignUp;

    private FirebaseAuth auth = FirebaseAuth.getInstance(); // Get Firebase authentication instance


    @FXML
    void userSignIn(ActionEvent event) {
        signInUser();
    }
    @FXML
    void handleSignUp(ActionEvent event) throws IOException {
        CapstoneApplication.setRoot("SignUp");
    }

    private void signInUser() {
        String email = userEmail.getText();
        String password = userPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter a valid email address");
            return;
        }
        try{

            String apiKey = "AIzaSyDVmD5jdc-iO7sOZ0Bn5F7-ZEoLkk17bVM\n";

            String firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

            String jsonInput = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", email, password);

            java.net.URL url = new URL(firebaseAuthUrl);

            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            java.io.OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                // ✅ Success - login
                showAlert("Success", "Login successful!");
                CapstoneApplication.setRoot("MainScreen");
            } else {
                // ❌ Invalid credentials
                showAlert("Error", "Invalid email or password.");
            }

            conn.disconnect();
        } catch (Exception e) {
            showAlert("Error", "Failed to sign in: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
