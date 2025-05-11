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

/**
 * The main JavaFX application class for the Capstone Project.
 * <p>
 * This class initializes Firebase, loads the initial FXML layout,
 * and sets up the primary scene for the application.
 * </p>
 */
public class CapstoneApplication extends Application {

    /** The main application scene. */
    public static Scene scene;

    /** Firestore database instance for Firebase interaction. */
    public static Firestore fstore;

    /** Firebase authentication instance. */
    public static FirebaseAuth fauth;

    /**
     * The entry point of the JavaFX application.
     * Initializes Firebase and sets up the initial UI defined in the "SignIn.fxml" file.
     *
     * @param stage the primary stage for this application
     * @throws IOException if the FXML file or Firebase configuration file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        initializeFirebase();  // Ensure Firebase is initialized before loading the UI

        scene = new Scene(loadFXML("SignIn"), 640, 480);
        scene.getStylesheets().add(getClass().getResource("/styles/planet.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Capstone Project");
        stage.show();
    }

    /**
     * Loads the specified FXML layout file from the resources.
     *
     * @param fxml the name of the FXML file (without extension)
     * @return the root {@link Parent} node of the loaded FXML
     * @throws IOException if the FXML file cannot be found or loaded
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CapstoneApplication.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Replaces the current scene root with a new one from the specified FXML file.
     *
     * @param fxml the name of the FXML file (without extension)
     * @throws IOException if the FXML file cannot be found or loaded
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Initializes Firebase using service account credentials.
     * If Firebase is already initialized, it simply retrieves the existing Firestore and Auth instances.
     */
    private void initializeFirebase() {
        if (FirebaseApp.getApps().isEmpty()) {  // Prevents duplicate Firebase initialization
            try {
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/Firebase/capstoneKey.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                fstore = FirestoreClient.getFirestore();
                fauth = FirebaseAuth.getInstance();

                System.out.println("Firebase Initialized Successfully");

            } catch (IOException e) {
                System.err.println("Failed to initialize Firebase: " + e.getMessage());
            }
        } else {
            fstore = FirestoreClient.getFirestore();
            fauth = FirebaseAuth.getInstance();
            System.out.println("Firebase was already initialized.");
        }
    }

    /**
     * The main method, launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
