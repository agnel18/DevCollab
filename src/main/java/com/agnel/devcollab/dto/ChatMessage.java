package com.agnel.devcollab.dto;

import java.time.LocalDateTime;

public class ChatMessage {
    private Long projectId;
    private String message;
    private String userName;
    private String userColor;
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(Long projectId, String message, String userName, String userColor, LocalDateTime timestamp) {
        this.projectId = projectId;
        this.message = message;
        this.userName = userName;
        this.userColor = userColor;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserColor() { return userColor; }
    public void setUserColor(String userColor) { this.userColor = userColor; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

