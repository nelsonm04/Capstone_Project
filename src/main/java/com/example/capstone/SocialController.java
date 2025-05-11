package com.example.capstone;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Controller for managing the social interface of the application. Handles user interactions related
 * to friend requests, friend list, and displaying events for friends.
 */
public class SocialController implements Initializable {

    @FXML private Button mainButton, socialButton, eventButton, settingButton;
    @FXML private Label monthYear, usernameDisplay;
    @FXML private ImageView profilePicture;
    @FXML private ComboBox<String> userDropdown;
    @FXML private Button sendRequestButton;
    @FXML private ListView<String> friendsListView;
    @FXML private VBox pendingRequestsVBox;
    private Firestore db;
    private String currentUid;

    /**
     * Initializes the social interface by setting up the navigation buttons, user information,
     * and loading users, friends, and pending requests.
     * @param location The URL location.
     * @param resources The resources bundle.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = FirestoreClient.getFirestore();
        currentUid = Session.getUid();

        // Navigation buttons setup
        mainButton.setOnAction(e -> handleScreenSwitch("MainScreen"));
        socialButton.setOnAction(e -> handleScreenSwitch("socialScreen"));
        eventButton.setOnAction(e -> handleScreenSwitch("eventScreen"));
        settingButton.setOnAction(e -> handleScreenSwitch("settingScreen"));

        // User info display
        if (Session.getUsername() != null) usernameDisplay.setText(Session.getUsername());
        if (Session.getProfilePicture() != null) {
            profilePicture.setImage(Session.getProfilePicture());
            Circle clip = new Circle(40, 40, 40);
            profilePicture.setClip(clip);
        }
        monthYear.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

        // Load data
        loadAllUsers();
        loadFriends();
        loadPendingRequests();

        sendRequestButton.setOnAction(e -> sendFriendRequest());
    }

    /**
     * Handles user sign out. Clears the session and loads the sign-in screen.
     * @param actionEvent The action event triggered by the sign-out button.
     */
    @FXML
    private Button signOutButton;
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
     * Switches between screens based on the provided screen name.
     * @param screenName The name of the screen to switch to.
     */
    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/capstone/" + screenName + ".fxml")
            );
            Parent root = loader.load();

            // Only re-run data loads for social screen
            if ("socialScreen".equals(screenName)) {
                SocialController sc = loader.getController();
                sc.loadAllUsers();
                sc.loadFriends();
                sc.loadPendingRequests();
            }

            Stage stage = (Stage) mainButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().clear();
            if ("MainScreen".equals(screenName)) {
                scene.getStylesheets()
                        .add(getClass().getResource("/Styles/mainscreen.css").toExternalForm());
            }
            scene.getStylesheets()
                    .add(getClass().getResource("/Styles/planet.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a list of all users from Firestore, excluding the current user and their friends.
     */
    public void loadAllUsers() {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("users").get();
            QuerySnapshot snapshot = future.get();
            List<QueryDocumentSnapshot> allUsers = snapshot.getDocuments();

            // Fetch friends of current user
            ApiFuture<QuerySnapshot> friendsFuture = db
                    .collection("users")
                    .document(currentUid)
                    .collection("friends")
                    .get();
            QuerySnapshot friendsSnap = friendsFuture.get();
            List<QueryDocumentSnapshot> friendDocs = friendsSnap.getDocuments();

            Set<String> friendsSet = new HashSet<>();
            for (QueryDocumentSnapshot d : friendDocs) {
                friendsSet.add(d.getString("username"));
            }

            // Filter users
            List<String> dropdownNames = new ArrayList<>();
            for (QueryDocumentSnapshot userDoc : allUsers) {
                String uid = userDoc.getId();
                String name = userDoc.getString("username");
                if (!uid.equals(currentUid) && !friendsSet.contains(name)) {
                    dropdownNames.add(name);
                }
            }

            Platform.runLater(() -> userDropdown.getItems().setAll(dropdownNames));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the list of friends for the current user and displays them in the friends list.
     */
    public void loadFriends() {
        db.collection("users").document(currentUid)
                .collection("friends").get()
                .addListener(() -> {
                    try {
                        List<String> friends = new ArrayList<>();
                        for (DocumentSnapshot d : db.collection("users")
                                .document(currentUid)
                                .collection("friends")
                                .get().get().getDocuments()) {
                            friends.add(d.getString("username"));
                        }

                        Platform.runLater(() -> {
                            friendsListView.getItems().setAll(friends);

                            // 1) Setup the Unfriend button per cell
                            friendsListView.setCellFactory(lv -> new ListCell<>() {
                                private final Button btn = new Button("Unfriend");
                                {
                                    btn.setOnAction(e -> removeFriend(getItem()));
                                }

                                @Override
                                protected void updateItem(String name, boolean empty) {
                                    super.updateItem(name, empty);
                                    if (empty || name == null) {
                                        setText(null);
                                        setGraphic(null);
                                    } else {
                                        setText(name);
                                        setGraphic(btn);
                                    }
                                }
                            });

                            // 2) Attach click listener to show friend events
                            friendsListView.setOnMouseClicked(evt -> {
                                String selected = friendsListView.getSelectionModel().getSelectedItem();
                                if (selected != null) {
                                    showFriendEvents(selected);
                                }
                            });
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Runnable::run);
    }

    /**
     * Displays upcoming events for the specified friend in a dialog.
     * @param friendUsername The username of the friend whose events are to be displayed.
     */
    private void showFriendEvents(String friendUsername) {
        // Find their UID
        String friendUid;
        try {
            friendUid = db.collection("users")
                    .whereEqualTo("username", friendUsername)
                    .get().get()
                    .getDocuments().get(0).getId();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Query upcoming events
        LocalDate today = LocalDate.now();
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .document(friendUid)
                .collection("events")
                .whereGreaterThanOrEqualTo("date", today.toString())
                .orderBy("date")
                .limit(5)     // Show next 5 events
                .get();

        List<String> eventLines = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                String title = doc.getString("title");
                String date = doc.getString("date");
                String time = doc.getString("time");
                eventLines.add(date + "  •  " + time + " → " + title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            if (eventLines.isEmpty()) {
                Alert info = new Alert(Alert.AlertType.INFORMATION,
                        friendUsername + " has no upcoming events."
                );
                DialogPane dp = info.getDialogPane();
                dp.getStylesheets().add(
                        getClass().getResource("/Styles/dialog.css").toExternalForm()
                );
                dp.getStyleClass().add("dialog-pane");
                info.initOwner(mainButton.getScene().getWindow());
                info.showAndWait();
            } else {
                ListView<String> listView = new ListView<>();
                listView.getItems().setAll(eventLines);
                listView.getStyleClass().add("event-list");
                listView.setMaxWidth(Region.USE_PREF_SIZE);
                listView.setPrefSize(350, Math.min(eventLines.size() * 40, 200));

                VBox content = new VBox(10, listView);
                content.setAlignment(Pos.CENTER);
                content.setPadding(new Insets(10));

                Dialog<Void> dlg = new Dialog<>();
                dlg.setTitle(friendUsername + "’s Next Events");
                DialogPane dp = dlg.getDialogPane();

                dp.getStylesheets().add(
                        getClass().getResource("/Styles/dialog.css").toExternalForm()
                );
                dp.getStyleClass().add("dialog-pane");
                dp.setContent(content);
                dp.getButtonTypes().setAll(ButtonType.CLOSE);
                dlg.initOwner(mainButton.getScene().getWindow());
                dlg.showAndWait();
            }
        });
    }

    /**
     * Sends a friend request to the selected user from the dropdown.
     */
    private void sendFriendRequest() {
        String target = userDropdown.getValue();
        if (target == null) return;

        try {
            // Look up UID
            QuerySnapshot qs = db.collection("users")
                    .whereEqualTo("username", target)
                    .get().get();
            if (!qs.isEmpty()) {
                String toUid = qs.getDocuments().get(0).getId();

                Map<String, Object> req = Map.of(
                        "fromUid", currentUid,
                        "fromUsername", Session.getUsername()
                );

                // Send the friend request
                db.collection("users")
                        .document(toUid)
                        .collection("friendRequests")
                        .add(req);

                loadAllUsers();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a friend from the current user's friend list.
     * @param friendName The name of the friend to be removed.
     */
    private void removeFriend(String friendName) {
        try {
            ApiFuture<QuerySnapshot> queryFuture = db.collection("users")
                    .whereEqualTo("username", friendName)
                    .get();
            String friendUid = queryFuture.get().getDocuments().get(0).getId();

            // Remove from current user's friend list
            db.collection("users")
                    .document(currentUid)
                    .collection("friends")
                    .document(friendUid).delete();

            // Remove from friend's friend list
            db.collection("users")
                    .document(friendUid)
                    .collection("friends")
                    .document(currentUid).delete();

            loadFriends();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads any pending friend requests for the current user and displays them.
     */
    private void loadPendingRequests() {
        db.collection("users")
                .document(currentUid)
                .collection("friendRequests")
                .get()
                .addListener(() -> {
                    try {
                        List<String> requests = new ArrayList<>();
                        for (DocumentSnapshot doc : db.collection("users")
                                .document(currentUid)
                                .collection("friendRequests")
                                .get().get().getDocuments()) {
                            requests.add(doc.getString("fromUsername"));
                        }

                        Platform.runLater(() -> {
                            pendingRequestsVBox.getChildren().clear();
                            for (String request : requests) {
                                HBox requestBox = new HBox(10);
                                Label requestLabel = new Label(request);
                                Button acceptButton = new Button("Accept");
                                Button declineButton = new Button("Decline");

                                acceptButton.setOnAction(e -> acceptRequest(request));
                                declineButton.setOnAction(e -> declineRequest(request));

                                requestBox.getChildren().addAll(requestLabel, acceptButton, declineButton);
                                pendingRequestsVBox.getChildren().add(requestBox);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Runnable::run);
    }

    /**
     * Accepts a pending friend request and adds the friend to both user's friend lists.
     * @param fromUsername The username of the person whose request is being accepted.
     */
    private void acceptRequest(String fromUsername) {
        try {
            ApiFuture<QuerySnapshot> queryFuture = db.collection("users")
                    .whereEqualTo("username", fromUsername)
                    .get();
            String fromUid = queryFuture.get().getDocuments().get(0).getId();

            // Add to both users' friend lists
            db.collection("users")
                    .document(currentUid)
                    .collection("friends")
                    .document(fromUid)
                    .set(Map.of("username", fromUsername));
            db.collection("users")
                    .document(fromUid)
                    .collection("friends")
                    .document(currentUid)
                    .set(Map.of("username", Session.getUsername()));

            // Remove the request
            db.collection("users")
                    .document(currentUid)
                    .collection("friendRequests")
                    .document(fromUid).delete();

            loadFriends();
            loadPendingRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Declines a pending friend request.
     * @param fromUsername The username of the person whose request is being declined.
     */
    private void declineRequest(String fromUsername) {
        try {
            ApiFuture<QuerySnapshot> queryFuture = db.collection("users")
                    .whereEqualTo("username", fromUsername)
                    .get();
            String fromUid = queryFuture.get().getDocuments().get(0).getId();

            // Remove the request
            db.collection("users")
                    .document(currentUid)
                    .collection("friendRequests")
                    .document(fromUid).delete();

            loadPendingRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
