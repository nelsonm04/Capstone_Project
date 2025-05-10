package com.example.capstone;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

public class SocialController {

    @FXML
    private ListView<String> friendsListView;

    @FXML
    private Button addFriendButton;

    @FXML
    public void initialize() {
        // Example data
        friendsListView.getItems().addAll("Alice", "Bob", "Charlie");

        // Handle adding friends
        addFriendButton.setOnAction(event -> addFriend("New Friend"));
    }

    private void addFriend(String friendName) {
        friendsListView.getItems().add(friendName);
    }
}
