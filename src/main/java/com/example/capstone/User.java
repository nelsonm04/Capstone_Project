package com.example.capstone;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String uid;
    private String email;
    private String username;
    private long createdAt;

    public User(String uid, String email, String username) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("username", username);
        map.put("created_at", createdAt);
        return map;
    }
}
