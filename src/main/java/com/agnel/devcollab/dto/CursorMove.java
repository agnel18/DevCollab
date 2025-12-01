package com.agnel.devcollab.dto;

public class CursorMove {
    private String userId;
    private String userName;
    private String userColor;
    private double x;
    private double y;
    private Long projectId; // null if not hovering a project

    public CursorMove() {}

    public CursorMove(String userId, String userName, String userColor, double x, double y, Long projectId) {
        this.userId = userId;
        this.userName = userName;
        this.userColor = userColor;
        this.x = x;
        this.y = y;
        this.projectId = projectId;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserColor() { return userColor; }
    public void setUserColor(String userColor) { this.userColor = userColor; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}

