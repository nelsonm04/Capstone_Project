package com.example.capstone;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user in the system. The User class stores details about the user,
 * including their unique identifier, email, username, and the timestamp of when the user was created.
 * Provides methods for accessing user data and converting the user object into a map format.
 */
public class User {

    private String uid;
    private String email;
    private String username;
    private long createdAt;

    /**
     * Constructs a new User object with the specified UID, email, and username.
     * The createdAt field is automatically set to the current system time.
     *
     * @param uid The unique identifier of the user.
     * @param email The email address of the user.
     * @param username The username of the user.
     */
    public User(String uid, String email, String username) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Gets the unique identifier (UID) of the user.
     *
     * @return The UID of the user.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the email address of the user.
     *
     * @return The email address of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the username of the user.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the timestamp of when the user was created.
     *
     * @return The creation timestamp of the user.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Converts the User object into a Map<String, Object> representation.
     * This is useful for storing the user in a database or passing user data between systems.
     *
     * @return A map containing the user's data (uid, email, username, created_at).
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("username", username);
        map.put("created_at", createdAt);
        return map;
    }
}
