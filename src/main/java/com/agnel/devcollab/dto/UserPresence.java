package com.agnel.devcollab.dto;

public class UserPresence {
    private String userId;
    private String userName;
    private String userColor;
    private boolean connected;

    public UserPresence() {}

    public UserPresence(String userId, String userName, String userColor, boolean connected) {
        this.userId = userId;
        this.userName = userName;
        this.userColor = userColor;
        this.connected = connected;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserColor() { return userColor; }
    public void setUserColor(String userColor) { this.userColor = userColor; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }
}

