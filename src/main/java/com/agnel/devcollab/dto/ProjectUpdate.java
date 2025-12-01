package com.agnel.devcollab.dto;

import com.agnel.devcollab.entity.Project;
import java.time.LocalDateTime;

public class ProjectUpdate {
    private Long projectId;
    private String projectName;
    private Project.Status status;
    private LocalDateTime pomodoroStart;
    private String action; // "MOVED", "TIMER_STARTED", "TIMER_STOPPED", "CREATED", "DELETED", "SUBTASK_ADDED"
    private String userName;
    private String userColor;

    public ProjectUpdate() {}

    public ProjectUpdate(Long projectId, String projectName, Project.Status status, 
                        LocalDateTime pomodoroStart, String action, String userName, String userColor) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.status = status;
        this.pomodoroStart = pomodoroStart;
        this.action = action;
        this.userName = userName;
        this.userColor = userColor;
    }

    // Getters and Setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Project.Status getStatus() { return status; }
    public void setStatus(Project.Status status) { this.status = status; }

    public LocalDateTime getPomodoroStart() { return pomodoroStart; }
    public void setPomodoroStart(LocalDateTime pomodoroStart) { this.pomodoroStart = pomodoroStart; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserColor() { return userColor; }
    public void setUserColor(String userColor) { this.userColor = userColor; }
}

