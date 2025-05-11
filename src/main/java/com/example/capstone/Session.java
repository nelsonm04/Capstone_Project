package com.example.capstone;

import javafx.scene.image.Image;

/**
 * A static utility class to store and manage the current user's session data.
 */
public class Session {

    /** The username of the current user. */
    private static String username;

    /** The unique identifier (UID) of the current user. */
    private static String uid;

    /** The email address of the current user. */
    private static String email;

    /** The profile picture of the current user. */
    private static Image profilePicture;

    /**
     * Returns the current user's username.
     *
     * @return the username
     */
    public static String getUsername() {
        return username;
    }

    /**
     * Sets the current user's username.
     *
     * @param username the username to set
     */
    public static void setUsername(String username) {
        Session.username = username;
    }

    /**
     * Returns the current user's UID.
     *
     * @return the UID
     */
    public static String getUid() {
        return uid;
    }

    /**
     * Sets the current user's UID.
     *
     * @param uid the UID to set
     */
    public static void setUid(String uid) {
        Session.uid = uid;
    }

    /**
     * Returns the current user's email address.
     *
     * @return the email address
     */
    public static String getEmail() {
        return email;
    }

    /**
     * Sets the current user's email address.
     *
     * @param email the email to set
     */
    public static void setEmail(String email) {
        Session.email = email;
    }

    /**
     * Returns the current user's profile picture.
     *
     * @return the profile picture as a {@link Image}
     */
    public static Image getProfilePicture() {
        return profilePicture;
    }

    /**
     * Sets the current user's profile picture.
     *
     * @param profilePicture the profile picture to set
     */
    public static void setProfilePicture(Image profilePicture) {
        Session.profilePicture = profilePicture;
    }

    /**
     * Clears all stored session data for the current user.
     */
    public static void clearSession() {
        username = null;
        uid = null;
        email = null;
        profilePicture = null;
    }
}
