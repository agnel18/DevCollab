package com.agnel.devcollab.dto;

import java.time.LocalDateTime;

public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Integer estimatedPomodoros;
    private Long boardId;
    private Long boardColumnId;
    private String status;
    private LocalDateTime createdAt;
    private Long totalSecondsSpent;
    private Long pausedElapsedSeconds;
    private Integer completedPomodoros;
    private Integer currentCycle;
    private LocalDateTime pomodoroStart;
    private LocalDateTime completedAt;
    private Integer pomodoroDuration;
    private Integer breakDuration;
    private Boolean isBreak;

    public ProjectResponse() {}

    public ProjectResponse(Long id, String name, String description, Integer estimatedPomodoros,
                          Long boardId, Long boardColumnId, String status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.estimatedPomodoros = estimatedPomodoros;
        this.boardId = boardId;
        this.boardColumnId = boardColumnId;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getEstimatedPomodoros() { return estimatedPomodoros; }
    public void setEstimatedPomodoros(Integer estimatedPomodoros) { this.estimatedPomodoros = estimatedPomodoros; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public Long getBoardColumnId() { return boardColumnId; }
    public void setBoardColumnId(Long boardColumnId) { this.boardColumnId = boardColumnId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getTotalSecondsSpent() { return totalSecondsSpent; }
    public void setTotalSecondsSpent(Long totalSecondsSpent) { this.totalSecondsSpent = totalSecondsSpent; }

    public Long getPausedElapsedSeconds() { return pausedElapsedSeconds; }
    public void setPausedElapsedSeconds(Long pausedElapsedSeconds) { this.pausedElapsedSeconds = pausedElapsedSeconds; }

    public Integer getCompletedPomodoros() { return completedPomodoros; }
    public void setCompletedPomodoros(Integer completedPomodoros) { this.completedPomodoros = completedPomodoros; }

    public Integer getCurrentCycle() { return currentCycle; }
    public void setCurrentCycle(Integer currentCycle) { this.currentCycle = currentCycle; }

    public LocalDateTime getPomodoroStart() { return pomodoroStart; }
    public void setPomodoroStart(LocalDateTime pomodoroStart) { this.pomodoroStart = pomodoroStart; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getPomodoroDuration() { return pomodoroDuration; }
    public void setPomodoroDuration(Integer pomodoroDuration) { this.pomodoroDuration = pomodoroDuration; }

    public Integer getBreakDuration() { return breakDuration; }
    public void setBreakDuration(Integer breakDuration) { this.breakDuration = breakDuration; }

    public Boolean getIsBreak() { return isBreak; }
    public void setIsBreak(Boolean isBreak) { this.isBreak = isBreak; }
}
