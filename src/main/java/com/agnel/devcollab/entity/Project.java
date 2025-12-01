package com.agnel.devcollab.entity;

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

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtask> subtasks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.TODO;

    public enum Status {
        TODO, DOING, DONE
    }

    private LocalDateTime pomodoroStart;
    private long totalSecondsSpent = 0; // Changed to seconds for accurate HH:MM:SS display

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

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getPomodoroStart() { return pomodoroStart; }
    public void setPomodoroStart(LocalDateTime start) { this.pomodoroStart = start; }

    public long getTotalSecondsSpent() { return totalSecondsSpent; }
    public void setTotalSecondsSpent(long seconds) { this.totalSecondsSpent = seconds; }
    
    // Convenience methods for backward compatibility
    public long getTotalMinutesSpent() { return totalSecondsSpent / 60; }
    public void setTotalMinutesSpent(long minutes) { this.totalSecondsSpent = minutes * 60; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }

    public long getTotalSubtaskSeconds() {
        return subtasks.stream().mapToLong(Subtask::getTotalSecondsSpent).sum();
    }
}