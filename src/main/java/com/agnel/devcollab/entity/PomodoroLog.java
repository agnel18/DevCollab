package com.agnel.devcollab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pomodoro_log")
public class PomodoroLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "subtask_id")
    private Subtask subtask;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    // Pomodoros used in this session (25min = 1 Pomodoro)
    @Column(nullable = false)
    private Integer pomodorosUsed = 0;
    
    // Was this Pomodoro completed or interrupted?
    private Boolean completed = false;
    
    // Optional: Interruption/distraction count
    private Integer distractions = 0;
    
    // User notes about this Pomodoro session
    @Column(length = 500)
    private String notes;
    
    // Break type: SHORT (5min), LONG (15-20min), NONE
    @Enumerated(EnumType.STRING)
    private BreakType breakType = BreakType.NONE;
    
    // Track which Pomodoro cycle this was (1-4, then long break)
    private Integer cycleNumber = 1;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum BreakType {
        NONE, SHORT, LONG
    }
    
    // Constructors
    public PomodoroLog() {}
    
    public PomodoroLog(Subtask subtask, User user) {
        this.subtask = subtask;
        this.user = user;
        this.startTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Subtask getSubtask() {
        return subtask;
    }
    
    public void setSubtask(Subtask subtask) {
        this.subtask = subtask;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getPomodorosUsed() {
        return pomodorosUsed;
    }
    
    public void setPomodorosUsed(Integer pomodorosUsed) {
        this.pomodorosUsed = pomodorosUsed;
    }
    
    public Boolean getCompleted() {
        return completed;
    }
    
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
    
    public boolean isCompleted() {
        return completed != null && completed;
    }
    
    public Integer getDistractions() {
        return distractions;
    }
    
    public void setDistractions(Integer distractions) {
        this.distractions = distractions;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public BreakType getBreakType() {
        return breakType;
    }
    
    public void setBreakType(BreakType breakType) {
        this.breakType = breakType;
    }
    
    public Integer getCycleNumber() {
        return cycleNumber;
    }
    
    public void setCycleNumber(Integer cycleNumber) {
        this.cycleNumber = cycleNumber;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public long getDurationMinutes() {
        if (endTime == null) return 0;
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    public double getEfficiencyScore() {
        if (endTime == null || !completed) return 0.0;
        long actualMinutes = getDurationMinutes();
        int expectedMinutes = pomodorosUsed * 25;
        if (expectedMinutes == 0) return 0.0;
        return Math.min(1.0, (double) actualMinutes / expectedMinutes);
    }
}
