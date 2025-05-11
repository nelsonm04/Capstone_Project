package com.example.capstone;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Settings screen. Manages user actions such as changing username,
 * switching screens, updating avatars, and deleting accounts.
 */
public class SettingsController implements Initializable {

    @FXML private ImageView profileImageView, profilePicture;
    @FXML private TextField changeUserName;
    @FXML private Text deleteAccount;
    @FXML private Text changeAvatarText;
    @FXML private Button eventButton, mainButton, settingButton, socialButton, signOutButton;
    @FXML private Label monthYear, usernameDisplay, weatherLabel;

    private static User currentUser;

    /**
     * Sets the current user context for the controller.
     * @param user the currently logged-in user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Initializes the settings screen components and session data.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set button navigation
        mainButton.setOnAction(e -> handleScreenSwitch("MainScreen"));
        settingButton.setOnAction(e -> handleScreenSwitch("settingScreen"));
        socialButton.setOnAction(e -> handleScreenSwitch("socialScreen"));
        eventButton.setOnAction(e -> handleScreenSwitch("eventScreen"));

        // Load session username
        if (Session.getUsername() != null) {
            usernameDisplay.setText(Session.getUsername());
        }

        // Load session profile picture
        if (Session.getProfilePicture() != null) {
            Image sessionImage = Session.getProfilePicture();
            profilePicture.setImage(sessionImage);
            profileImageView.setImage(sessionImage);
            profilePicture.setClip(new Circle(40, 40, 40));
            profileImageView.setClip(new Circle(135, 135, 135));
        }

        changeAvatarText.setOnMouseClicked(this::handleChangeAvatar);

        // Display current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        monthYear.setText(LocalDate.now().format(formatter));

        // Get user from main screen context
        currentUser = MainScreen.getCurrentUser();
    }

    /**
     * Handles user sign-out by clearing session and loading the sign-in screen.
     */
    public void handleSignOut(javafx.event.ActionEvent actionEvent) {
        Session.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signOutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Switches between different application screens while preserving window settings.
     * @param screenName the name of the FXML screen to load
     */
    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/" + screenName + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) mainButton.getScene().getWindow();

            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();
            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(root);
            scene.getStylesheets().clear();
            if (screenName.equals("MainScreen")) {
                scene.getStylesheets().add(getClass().getResource("/Styles/mainscreen.css").toExternalForm());
            }
            scene.getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());

            stage.setScene(scene);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(isMaximized);
            stage.setFullScreen(isFullScreen);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prompts user to choose an image file and updates the profile picture.
     * @param event the mouse click event
     */
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
            Session.setProfilePicture(newImage);

            profilePicture.setClip(new Circle(40, 40, 40));
            profileImageView.setClip(new Circle(135, 135, 135));

            System.out.println("Profile picture updated and saved to session!");
        }
    }

    /**
     * Handles deletion of the user's account from Firestore and FirebaseAuth.
     * @param event the mouse click event
     */
    @FXML
    private void deleteUserAccount(MouseEvent event) {
        if (currentUser == null) {
            System.out.println("No user is currently signed in.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Account Deletion");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String uid = currentUser.getUid();
            Firestore db = FirestoreClient.getFirestore();
            db.collection("users").document(uid).delete().addListener(() -> {
                System.out.println("Firestore user document deleted.");
                try {
                    FirebaseAuth.getInstance().deleteUser(uid);
                    System.out.println("User deleted from Firebase Authentication.");
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Account deleted successfully.");
                        goToSignInScreen();
                    });
                } catch (FirebaseAuthException e) {
                    System.err.println("Failed to delete user from Firebase Auth: " + e.getMessage());
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Auth deletion failed."));
                }
            }, Runnable::run);
        }
    }

    /**
     * Redirects the user to the sign-in screen.
     */
    private void goToSignInScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) deleteAccount.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the current user's username in Firestore and UI.
     */
    @FXML
    private void changeUsernameOnAction() {
        String newUsername = changeUserName.getText().trim();

        if (newUsername.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Username cannot be empty.");
            return;
        }

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "No user is currently signed in.");
            return;
        }

        usernameDisplay.setText(newUsername);
        Session.setUsername(newUsername);

        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(currentUser.getUid())
                .update("username", newUsername)
                .addListener(() -> {
                    System.out.println("Username successfully updated in Firestore.");
                    Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Username updated!"));
                }, Runnable::run);
    }

    /**
     * Shows an alert with custom message and styling.
     * @param type the type of the alert (information, error, etc.)
     * @param message the message to display
     */
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Username Change");
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());
        alert.showAndWait();
    }
}
