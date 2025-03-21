package com.example.capstone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;

public class CapstoneApplication extends Application {

    public static Scene scene;
    public static Firestore fstore;
    public static FirebaseAuth fauth;

    @Override
    public void start(Stage stage) throws IOException {
        initializeFirebase();  // ✅ Ensure Firebase is initialized before loading the UI

        scene = new Scene(loadFXML("SignIn"), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Capstone Project");
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CapstoneApplication.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private void initializeFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {  // ✅ Prevents duplicate Firebase initialization
            try {
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/Firebase/capstoneKey.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                fstore = FirestoreClient.getFirestore();
                fauth = FirebaseAuth.getInstance();

                System.out.println("✅ Firebase Initialized Successfully");

            } catch (IOException e) {
                System.err.println("❌ Failed to initialize Firebase: " + e.getMessage());
            }
        } else {
            fstore = FirestoreClient.getFirestore();
            fauth = FirebaseAuth.getInstance();
            System.out.println("ℹ️ Firebase was already initialized.");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
