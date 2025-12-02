package com.agnel.devcollab.ws;

import java.time.LocalDateTime;

public class TimerSyncMessage {
    public enum Action { START, STOP, UPDATE }
    private Action action;
    private Long entryId;
    private Long userId;
    private Long subtaskId;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    // getters/setters
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSubtaskId() { return subtaskId; }
    public void setSubtaskId(Long subtaskId) { this.subtaskId = subtaskId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
}
