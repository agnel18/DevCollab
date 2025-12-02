package com.agnel.devcollab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class TimeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Project project;

    @ManyToOne
    private Task task;

    @ManyToOne
    private Subtask subtask;

    private String description;

    @ElementCollection
    private Set<String> tags;

    private LocalDateTime start;
    
    @Column(name = "end_time")
    private LocalDateTime end;

    private boolean pomodoro; // true if auto-created by Pomodoro
    private boolean billable; // for future

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public Subtask getSubtask() { return subtask; }
    public void setSubtask(Subtask subtask) { this.subtask = subtask; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    public boolean isPomodoro() { return pomodoro; }
    public void setPomodoro(boolean pomodoro) { this.pomodoro = pomodoro; }
    public boolean isBillable() { return billable; }
    public void setBillable(boolean billable) { this.billable = billable; }
}
