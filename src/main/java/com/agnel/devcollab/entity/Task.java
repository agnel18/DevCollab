package com.agnel.devcollab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtask> subtasks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.TODO;

    public enum Status {
        TODO, DOING, DONE
    }

    // Pomodoro timer support at Task level
    private LocalDateTime pomodoroStart;
    private long totalSecondsSpent = 0;
    
    // Pomodoro cycle settings (inherited from Project or customizable)
    private int pomodoroDuration = 25; // Work duration in minutes
    private int breakDuration = 5; // Break duration in minutes
    private boolean isBreak = false; // Track if current timer is a break
    
    // Track Pomodoro cycles
    private Integer estimatedPomodoros = 1;
    private Integer completedPomodoros = 0;
    private Integer currentCycle = 1;
    
    // Allow editing even when DONE
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { 
        this.status = status;
        if (status == Status.DONE && completedAt == null) {
            completedAt = LocalDateTime.now();
        } else if (status != Status.DONE) {
            completedAt = null; // Allow reopening tasks
        }
    }

    public LocalDateTime getPomodoroStart() { return pomodoroStart; }
    public void setPomodoroStart(LocalDateTime start) { this.pomodoroStart = start; }

    public long getTotalSecondsSpent() { return totalSecondsSpent; }
    public void setTotalSecondsSpent(long seconds) { this.totalSecondsSpent = seconds; }

    public int getPomodoroDuration() { return pomodoroDuration; }
    public void setPomodoroDuration(int duration) { this.pomodoroDuration = duration; }

    public int getBreakDuration() { return breakDuration; }
    public void setBreakDuration(int duration) { this.breakDuration = duration; }

    public boolean isBreak() { return isBreak; }
    public void setBreak(boolean isBreak) { this.isBreak = isBreak; }

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

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // === Helper Methods ===
    
    // Calculate total time from all subtasks
    public long getTotalSubtaskSeconds() {
        return subtasks.stream().mapToLong(Subtask::getTotalSecondsSpent).sum();
    }
    
    // Get combined time (task's own time + subtask times)
    public long getCombinedSecondsSpent() {
        return totalSecondsSpent + getTotalSubtaskSeconds();
    }
    
    // Helper method to get current timer target in seconds
    public long getCurrentTimerTarget() {
        return (isBreak ? breakDuration : pomodoroDuration) * 60L;
    }
    
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
    
    // Check if task can be edited (always true now - even DONE tasks are editable)
    public boolean isEditable() {
        return true; // Allow editing at any status
    }
}
