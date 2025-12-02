package com.agnel.devcollab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    private LocalDateTime pomodoroStart;
    private long totalSecondsSpent = 0; // Changed to seconds for accurate HH:MM:SS display
    
    // Pomodoro estimation (1-5 Pomodoros, default 1)
    @Column(nullable = false)
    private Integer estimatedPomodoros = 1;
    
    // Track completed Pomodoros for this subtask
    private Integer completedPomodoros = 0;
    
    // Track current Pomodoro cycle (1-4, resets after long break)
    private Integer currentCycle = 1;
    
    // Is subtask marked as completed?
    private Boolean completed = false;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public LocalDateTime getPomodoroStart() { return pomodoroStart; }
    public void setPomodoroStart(LocalDateTime start) { this.pomodoroStart = start; }

    public long getTotalSecondsSpent() { return totalSecondsSpent; }
    public void setTotalSecondsSpent(long seconds) { this.totalSecondsSpent = seconds; }
    
    public Integer getEstimatedPomodoros() { return estimatedPomodoros; }
    public void setEstimatedPomodoros(Integer estimatedPomodoros) { 
        this.estimatedPomodoros = estimatedPomodoros; 
    }
    
    public Integer getCompletedPomodoros() { return completedPomodoros; }
    public void setCompletedPomodoros(Integer completedPomodoros) { 
        this.completedPomodoros = completedPomodoros; 
    }
    
    public Integer getCurrentCycle() { return currentCycle; }
    public void setCurrentCycle(Integer currentCycle) { 
        this.currentCycle = currentCycle; 
    }
    
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { 
        this.completed = completed; 
    }
    
    // Convenience methods for backward compatibility
    public long getTotalMinutesSpent() { return totalSecondsSpent / 60; }
    public void setTotalMinutesSpent(long minutes) { this.totalSecondsSpent = minutes * 60; }
    
    // Helper methods
    public double getCompletionPercentage() {
        if (estimatedPomodoros == 0) return 0.0;
        return Math.min(100.0, (completedPomodoros * 100.0) / estimatedPomodoros);
    }
    
    public boolean needsLongBreak() {
        return currentCycle > 4;
    }
    
    public void incrementCycle() {
        if (currentCycle >= 4) {
            currentCycle = 1; // Reset after long break
        } else {
            currentCycle++;
        }
    }
    
    // Allow editing at any status
    public boolean isEditable() {
        return true; // Always editable, even when completed
    }
}