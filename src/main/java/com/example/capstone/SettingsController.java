package com.example.capstone;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
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

public class  SettingsController implements Initializable {

    @FXML
    private ImageView profileImageView, profilePicture;

    @FXML
    private TextField changeUserName;

    @FXML
    private Text deleteAccount;

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

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        monthYear.setText(LocalDate.now().format(formatter));

        currentUser = MainScreen.getCurrentUser();

    }


    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screenName + ".fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) mainButton.getScene().getWindow();
            Scene scene = new Scene(root);
            if (screenName.equals("MainScreen")) {
                scene.getStylesheets().add(getClass().getResource("/Styles/mainscreen.css").toExternalForm());
            }

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
            System.out.println(" Failed to load default avatar: " + e.getMessage());
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

            // Save to session
            Session.setProfilePicture(newImage);

            // Re-clip
            Circle sidebarClip = new Circle(40, 40, 40);
            profilePicture.setClip(sidebarClip);

            Circle centerClip = new Circle(135, 135, 135);
            profileImageView.setClip(centerClip);

            System.out.println("Profile picture updated and saved to session!");
        }
    }

    @FXML
    private void deleteUserAccount(MouseEvent event) {
        if (currentUser == null) {
            System.out.println("No user is currently signed in.");
            return;
        }

        // Confirmation popup
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Account Deletion");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String uid = currentUser.getUid();
            Firestore db = FirestoreClient.getFirestore();
            db.collection("users").document(uid).delete().addListener(() -> {
                System.out.println(" Firestore user document deleted.");
            try {
                FirebaseAuth.getInstance().deleteUser(uid);
                System.out.println("User deleted from Firebase Authentication.");
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Account deleted successfully.");
                    goToSignInScreen();
                });

            }  catch (FirebaseAuthException e) {
                System.err.println(" Failed to delete user from Firebase Auth: " + e.getMessage());
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Auth deletion failed."));
            }

            }, Runnable::run); // Executes on JavaFX thread

        }
    }

    private void goToSignInScreen(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/capstone/SignIn.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) deleteAccount.getScene().getWindow();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("/Styles/planet.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

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

        // Update UI
        usernameDisplay.setText(newUsername);
        Session.setUsername(newUsername);  // Persist in session

        // Update Firestore
        Firestore db = FirestoreClient.getFirestore();
        db.collection("users").document(currentUser.getUid())
                .update("username", newUsername)
                .addListener(() -> {
                    System.out.println("Username successfully updated in Firestore.");
                    Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Username updated!"));
                }, Runnable::run);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Username Change");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




}
