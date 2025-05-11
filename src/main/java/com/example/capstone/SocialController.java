package com.example.capstone;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        db = FirestoreClient.getFirestore();
        currentUid = Session.getUid();

        // nav buttons
        mainButton.setOnAction(e -> handleScreenSwitch("MainScreen"));
        socialButton.setOnAction(e -> handleScreenSwitch("socialScreen"));
        eventButton.setOnAction(e -> handleScreenSwitch("eventScreen"));
        settingButton.setOnAction(e -> handleScreenSwitch("settingScreen"));

        // user info
        if (Session.getUsername() != null)      usernameDisplay.setText(Session.getUsername());
        if (Session.getProfilePicture() != null) {
            profilePicture.setImage(Session.getProfilePicture());
            Circle clip = new Circle(40,40,40);
            profilePicture.setClip(clip);
        }
        monthYear.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

        loadAllUsers();
        loadFriends();
        loadPendingRequests();
        sendRequestButton.setOnAction(e -> sendFriendRequest());
    }

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

    private void handleScreenSwitch(String screenName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/capstone/"+screenName+".fxml")
            );
            Parent root = loader.load();

            // ONLY re‐run SocialController’s data loads:
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

    public void loadAllUsers() {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("users").get();
            QuerySnapshot snapshot = future.get();

            List<QueryDocumentSnapshot> allUsers = snapshot.getDocuments();

            // fetch friends the same way:
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

            // now filter
            List<String> dropdownNames = new ArrayList<>();
            for (QueryDocumentSnapshot userDoc : allUsers) {
                String uid   = userDoc.getId();
                String name  = userDoc.getString("username");
                if (!uid.equals(currentUid) && !friendsSet.contains(name)) {
                    dropdownNames.add(name);
                }
            }

            Platform.runLater(() -> userDropdown.getItems().setAll(dropdownNames));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    public void loadFriends() {
        db.collection("users").document(currentUid)
                .collection("friends").get()
                .addListener(() -> {
                    try {
                        List<String> friends = new ArrayList<>();
                        for (DocumentSnapshot d : db.collection("users")
                                .document(currentUid)
                                .collection("friends")
                                .get().get().getDocuments())
                            friends.add(d.getString("username"));

                        Platform.runLater(() -> {
                            friendsListView.getItems().setAll(friends);
                            friendsListView.setCellFactory(lv -> new ListCell<>() {
                                private final Button btn = new Button("Unfriend");
                                {
                                    btn.setOnAction(e -> removeFriend(getItem()));
                                }
                                @Override
                                protected void updateItem(String name, boolean empty) {
                                    super.updateItem(name, empty);
                                    if (empty || name == null) {
                                        setText(null); setGraphic(null);
                                    } else {
                                        setText(name); setGraphic(btn);
                                    }
                                }
                            });
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }, Runnable::run);
    }

    private void sendFriendRequest() {
        String target = userDropdown.getValue();
        if (target == null) return;

        try {
            // look up UID
            QuerySnapshot qs = db.collection("users")
                    .whereEqualTo("username", target)
                    .get().get();
            if (!qs.isEmpty()) {
                String toUid = qs.getDocuments().get(0).getId();

                Map<String,Object> req = Map.of(
                        "fromUid",    currentUid,
                        "fromUsername", Session.getUsername()
                );

                // send it
                db.collection("users")
                        .document(toUid)
                        .collection("friendRequests")
                        .add(req);

                Platform.runLater(() -> {
                    loadPendingRequests();
                    CustomAlert alert = new CustomAlert(
                            Alert.AlertType.INFORMATION,
                            "Friend request sent to \"" + target + "\"!"
                    );
                    alert.showAndWait();

                    HBox row = new HBox(8);
                    Label lbl = new Label(target + " (sent)");
                    row.getChildren().add(lbl);
                    pendingRequestsVBox.getChildren().add(row);

                    userDropdown.getSelectionModel().clearSelection();
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    public void loadPendingRequests() {
        pendingRequestsVBox.getChildren().clear();
        db.collection("users").document(currentUid)
                .collection("friendRequests")
                .get().addListener(() -> {
                    try {
                        for (DocumentSnapshot d : db.collection("users")
                                .document(currentUid)
                                .collection("friendRequests")
                                .get().get().getDocuments()) {
                            String fromU = d.getString("fromUsername");
                            String fromUid = d.getString("fromUid");
                            HBox row = new HBox(8);
                            Label lbl = new Label(fromU);
                            Button acc = new Button("Accept");
                            Button dec = new Button("Decline");
                            acc.setOnAction(e -> {
                                // add each other
                                db.collection("users")
                                        .document(currentUid)
                                        .collection("friends")
                                        .document(fromUid)
                                        .set(Map.of("username", fromU));
                                db.collection("users")
                                        .document(fromUid)
                                        .collection("friends")
                                        .document(currentUid)
                                        .set(Map.of("username", Session.getUsername()));
                                d.getReference().delete();
                                Platform.runLater(() -> {
                                    loadFriends();
                                    loadPendingRequests();
                                });
                            });
                            dec.setOnAction(e -> {
                                d.getReference().delete();
                                Platform.runLater(this::loadPendingRequests);
                            });
                            row.getChildren().addAll(lbl, acc, dec);
                            Platform.runLater(() -> pendingRequestsVBox.getChildren().add(row));
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }, Runnable::run);
    }

    private void removeFriend(String friendName) {
        try {
            String friendUid = db.collection("users")
                    .whereEqualTo("username", friendName)
                    .get().get().getDocuments().get(0).getId();
            // remove both sides
            db.collection("users")
                    .document(currentUid)
                    .collection("friends")
                    .document(friendUid)
                    .delete();
            db.collection("users")
                    .document(friendUid)
                    .collection("friends")
                    .document(currentUid)
                    .delete();
            Platform.runLater(this::loadFriends);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static class CustomAlert extends Alert {
        CustomAlert(AlertType type, String content) {
            super(type);
            setTitle("Friend Request");
            setHeaderText(null);
            setContentText(content);

            DialogPane dp = getDialogPane();
            dp.getStylesheets().add(
                    getClass().getResource("/Styles/planet.css").toExternalForm()
            );
            dp.getStyleClass().add("dialog-pane");
        }
    }
}
