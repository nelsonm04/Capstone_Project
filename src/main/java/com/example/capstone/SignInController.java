package com.example.capstone;

import com.google.firebase.auth.UserRecord;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
            showAlert("Error", "Please enter a valid email address and password.");
            return;
        }
        try {
            String apiKey = "AIzaSyDVmD5jdc-iO7sOZ0Bn5F7-ZEoLkk17bVM"; // (Reminder: protect API key if you go public)

            String firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

            String jsonInput = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", email, password);

            URL url = new URL(firebaseAuthUrl);
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

                // Step 1: Get UID from FirebaseAuth
                UserRecord userRecord = CapstoneApplication.fauth.getUserByEmail(email);
                String uid = userRecord.getUid();

                // Step 2: Get Username from Firestore
                String username = getUsernameFromFirestore(uid);

                if (username != null) {
                    // After getting username and uid
                    User user = new User(uid,email,username);
                    MainScreen.setCurrentUser(user);
                    Session.setUsername(username);
                    Session.setUid(uid);
                    Session.setEmail(email);  // you already know email from login

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
                    Parent root = loader.load();

                    MainScreen controller = loader.getController();
                    controller.setUsername(username);

                    Stage stage = (Stage) userLogIn.getScene().getWindow();
                    Scene scene = new Scene(root);



                    scene.getStylesheets().add(getClass().getResource("/styles/planet.css").toExternalForm());
                    scene.getStylesheets().add(getClass().getResource("/styles/mainscreen.css").toExternalForm());

                    stage.setScene(scene);
                    stage.show();

                } else {
                    showAlert("Error", "Username not found.");
                }

            } else {
                showAlert("Error", "Invalid email or password.");
            }

            conn.disconnect();
        } catch (Exception e) {
            showAlert("Error", "Failed to sign in: " + e.getMessage());
        }
    }

    private String getUsernameFromFirestore(String uid) {
        try {
            var docRef = CapstoneApplication.fstore.collection("users").document(uid);
            var snapshot = docRef.get().get();  // Blocking (for now) — easier

            if (snapshot.exists()) {
                return snapshot.getString("username");
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch username: " + e.getMessage());
            return null;
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
