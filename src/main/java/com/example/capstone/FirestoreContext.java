package com.example.capstone;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Initializes and provides access to a Firestore database instance using Firebase credentials.
 * This class sets up Firebase with service account credentials and returns a {@link Firestore} client.
 */
public class FirestoreContext {

    /**
     * Initializes Firebase using a service account JSON key file and returns a Firestore client instance.
     * <p>
     * This method will terminate the program if initialization fails due to an {@link IOException}.
     * </p>
     *
     * @return the initialized {@link Firestore} client
     */
    public Firestore firebase() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("C:\\Users\\Geone\\IdeaProjects\\Capstone\\src\\main\\resources\\Firebase\\capstoneKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1); // Critical failure: cannot proceed without Firebase
        }
        return FirestoreClient.getFirestore();
    }
}
