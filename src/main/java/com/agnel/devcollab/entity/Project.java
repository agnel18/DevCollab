package com.agnel.devcollab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnoreProperties("projects")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "column_id", nullable = false)
    @JsonIgnoreProperties("projects")
    private BoardColumn boardColumn;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("project")
    private List<Task> tasks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.TODO;

    public enum Status {
        TODO, DOING, DONE
    }

    private LocalDateTime pomodoroStart;
    private long totalSecondsSpent = 0; // Changed to seconds for accurate HH:MM:SS display
    private long pausedElapsedSeconds = 0; // Track elapsed seconds when paused for resume
    
    // Pomodoro cycle settings
    private int pomodoroDuration = 25; // Work duration in minutes (default 25)
    private int breakDuration = 5; // Break duration in minutes (default 5)
    private boolean isBreak = false; // Track if current timer is a break
    
    // Track completion and allow reopening
    private LocalDateTime completedAt;
    
    // Track Pomodoro cycles at project level
    private Integer estimatedPomodoros = 1;
    private Integer completedPomodoros = 0;
    private Integer currentCycle = 1;

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public BoardColumn getBoardColumn() { return boardColumn; }
    public void setBoardColumn(BoardColumn boardColumn) { this.boardColumn = boardColumn; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { 
        this.status = status;
        if (status == Status.DONE && completedAt == null) {
            completedAt = LocalDateTime.now();
        } else if (status != Status.DONE) {
            completedAt = null; // Allow reopening projects
        }
    }

    public LocalDateTime getPomodoroStart() { return pomodoroStart; }
    public void setPomodoroStart(LocalDateTime start) { this.pomodoroStart = start; }

    public long getTotalSecondsSpent() { return totalSecondsSpent; }
    public void setTotalSecondsSpent(long seconds) { this.totalSecondsSpent = seconds; }
    
    // Convenience methods for backward compatibility
    public long getTotalMinutesSpent() { return totalSecondsSpent / 60; }
    public void setTotalMinutesSpent(long minutes) { this.totalSecondsSpent = minutes * 60; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    // Calculate total time from all tasks (including their subtasks)
    public long getTotalTaskSeconds() {
        return tasks.stream().mapToLong(Task::getCombinedSecondsSpent).sum();
    }
    
    // Get combined time (project's own time + all task times + all subtask times)
    public long getCombinedSecondsSpent() {
        return totalSecondsSpent + getTotalTaskSeconds();
    }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
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

    // Pomodoro getters/setters
    public int getPomodoroDuration() { return pomodoroDuration; }
    public void setPomodoroDuration(int duration) { this.pomodoroDuration = duration; }
    
    public int getBreakDuration() { return breakDuration; }
    public void setBreakDuration(int duration) { this.breakDuration = duration; }
    
    public boolean isBreak() { return isBreak; }
    public void setBreak(boolean isBreak) { this.isBreak = isBreak; }
    
    public long getPausedElapsedSeconds() { return pausedElapsedSeconds; }
    public void setPausedElapsedSeconds(long seconds) { this.pausedElapsedSeconds = seconds; }
    
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
    
    // Allow editing at any status
    public boolean isEditable() {
        return true; // Always editable, even when DONE
    }
}