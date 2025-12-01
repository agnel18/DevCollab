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
    @JoinColumn(name = "project_id")
    private Project project;

    private LocalDateTime pomodoroStart;
    private long totalSecondsSpent = 0; // Changed to seconds for accurate HH:MM:SS display

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public LocalDateTime getPomodoroStart() { return pomodoroStart; }
    public void setPomodoroStart(LocalDateTime start) { this.pomodoroStart = start; }

    public long getTotalSecondsSpent() { return totalSecondsSpent; }
    public void setTotalSecondsSpent(long seconds) { this.totalSecondsSpent = seconds; }
    
    // Convenience methods for backward compatibility
    public long getTotalMinutesSpent() { return totalSecondsSpent / 60; }
    public void setTotalMinutesSpent(long minutes) { this.totalSecondsSpent = minutes * 60; }
}