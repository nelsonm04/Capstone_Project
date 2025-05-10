module com.example.capstone {
    requires javafx.controls;
    requires javafx.fxml;



    requires firebase.admin;
    requires com.google.auth;
    requires com.google.auth.oauth2;
    requires google.cloud.firestore;
    requires google.cloud.core;
    requires com.google.api.apicommon;
    requires com.google.gson;
    requires java.desktop;


    opens com.example.capstone to javafx.fxml;
    exports com.example.capstone;
}