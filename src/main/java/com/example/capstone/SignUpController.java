package com.example.capstone;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.auth.FirebaseAuth;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

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
    public void initialize() {
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordMatch();
        });
    }

    private void checkPasswordMatch() {
        if (signUpPassword.getText().equals(confirmPasswordField.getText())) {
            passwordMatch.setText("Password Match");
        }else{
            passwordMatch.setText("Password Don't Match");
        }
    }

    @FXML
    private void handleBack() throws IOException{
        CapstoneApplication.setRoot("SignIn");
    }



    @FXML
    void handleSignUp(ActionEvent event) {
        String email = signUpEmail.getText();
        String password = signUpPassword.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "Email and password cannot be empty.");
            return;
        }

        try {
            // Create new user in Firebase Authentication
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setEmailVerified(false)
                    .setDisplayName(email.split("@")[0])
                    .setDisabled(false);

            UserRecord userRecord = CapstoneApplication.fauth.createUser(request);
            showAlert("Success", "Account created successfully: " + userRecord.getUid());

        } catch (FirebaseAuthException e) {
            showAlert("Error", "Failed to create account: " + e.getMessage());
        }
    }

    private void saveUserToDatabase(String uid, String email) {
        try{
            Firestore db = FirestoreOptions.getDefaultInstance().getService();

            Map<String, Object> user = new HashMap<>();
            user.put("uid", uid);
            user.put("email", email);
            user.put("Create_at", System.currentTimeMillis());

            DocumentReference docRef = db.collection("users").document(uid);
            docRef.set(user);
            showAlert("Success", "User created successfully");
        } catch (Exception e){
            showAlert("Error", "Failed to create user: " + e.getMessage());
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
