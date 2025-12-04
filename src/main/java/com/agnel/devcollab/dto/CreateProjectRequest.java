package com.agnel.devcollab.dto;

public class CreateProjectRequest {
    private String name;
    private String description;
    private Integer estimatedPomodoros;
    private Long boardId;
    private Long boardColumnId;

    // Getters and Setters
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
}
