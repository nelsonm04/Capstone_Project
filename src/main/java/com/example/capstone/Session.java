package com.example.capstone;

import javafx.scene.image.Image;

public class Session {

    private static String username;
    private static String uid;
    private static String email;
    private static Image profilePicture;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Session.username = username;
    }

    public static String getUid() {
        return uid;
    }

    public static void setUid(String uid) {
        Session.uid = uid;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        Session.email = email;
    }

    public static Image getProfilePicture() {
        return profilePicture;
    }

    public static void setProfilePicture(Image profilePicture) {
        Session.profilePicture = profilePicture;
    }

    public static void clearSession() {
        username = null;
        uid = null;
        email = null;
        profilePicture = null;
    }
}
