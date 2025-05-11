package com.example.capstone;

import com.google.firebase.auth.UserRecord;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

/**
 * The controller for the sign-in screen of the Capstone application.
 * It handles user login, user sign-up navigation, and shooting star animations.
 *
 * It connects to Firebase for user authentication and retrieves additional
 * user details from Firestore.
 */
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
    private Circle shootingStar1;  // Shooting Star 1

    @FXML
    private Circle shootingStar2;  // Shooting Star 2

    /**
     * Handles the action when the user clicks the "Log In" button.
     * Initiates the sign-in process.
     *
     * @param event The action event triggered by the button click.
     */
    @FXML
    void userSignIn(ActionEvent event) {
        signInUser();
    }

    /**
     * Navigates the user to the sign-up screen when they click the "Sign Up" button.
     *
     * @param event The action event triggered by the button click.
     * @throws IOException If the FXMLLoader encounters an error loading the sign-up screen.
     */
    @FXML
    void handleSignUp(ActionEvent event) throws IOException {
        CapstoneApplication.setRoot("SignUp");
    }

    /**
     * Signs in the user by validating their credentials and interacting with Firebase authentication.
     * If sign-in is successful, retrieves the user's information from Firestore and navigates to the main screen.
     * Displays an alert in case of errors.
     */
    private void signInUser() {
        String email = userEmail.getText();
        String password = userPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter a valid email address and password.");
            return;
        }

        try {
            String apiKey = "AIzaSyDVmD5jdc-iO7sOZ0Bn5F7-ZEoLkk17bVM";  // API Key for Firebase Authentication

            // Firebase Authentication URL for sign-in
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
                // Fetch UID and username after successful sign-in
                UserRecord userRecord = CapstoneApplication.fauth.getUserByEmail(email);
                String uid = userRecord.getUid();
                String username = getUsernameFromFirestore(uid);

                if (username != null) {
                    // Set the current user and session information
                    User user = new User(uid, email, username);
                    MainScreen.setCurrentUser(user);
                    Session.setUsername(username);
                    Session.setUid(uid);
                    Session.setEmail(email);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScreen.fxml"));
                    Parent root = loader.load();

                    MainScreen controller = loader.getController();
                    controller.setUsername(username);

                    // Switch to the main screen
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

    /**
     * Retrieves the username from Firestore using the user's UID.
     *
     * @param uid The UID of the user.
     * @return The username of the user, or null if not found.
     */
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

    /**
     * Displays an alert with a specified title and message.
     *
     * @param title   The title of the alert.
     * @param message The content of the alert.
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

    /**
     * Initializes the scene by animating shooting stars.
     */
    @FXML
    public void initialize() {
        animateShootingStar(shootingStar1, 5, 1000);
        animateShootingStar(shootingStar2, 7, 1200);
    }

    /**
     * Animates a shooting star (circle) element to move across the screen in a looping animation.
     *
     * @param star          The Circle object representing the shooting star.
     * @param durationSeconds The duration of the animation in seconds.
     * @param distance      The distance the star moves in pixels.
     */
    private void animateShootingStar(Circle star, int durationSeconds, int distance) {
        TranslateTransition transition = new TranslateTransition();
        transition.setDuration(Duration.seconds(durationSeconds));
        transition.setNode(star);
        transition.setByX(-distance); // Move left by "distance" amount
        transition.setByY(distance);  // Move down by "distance" amount
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.setAutoReverse(true);  // Loop the animation back and forth
        transition.play();
    }
}
