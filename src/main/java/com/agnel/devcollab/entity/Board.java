package com.agnel.devcollab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "board")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String color; // for visual distinction

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("board")
    private List<BoardColumn> columns = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("board")
    private List<Project> projects = new ArrayList<>();

    // === Constructors ===
    public Board() {
        this.createdAt = LocalDateTime.now();
        initializeDefaultColumns();
    }

    public Board(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
        this.color = "#3B82F6";
        initializeDefaultColumns();
    }

    private void initializeDefaultColumns() {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        // Create default columns if empty
        if (this.columns.isEmpty()) {
            this.columns.add(new BoardColumn("To Do", 0, this));
            this.columns.add(new BoardColumn("Doing", 1, this));
            this.columns.add(new BoardColumn("Done", 2, this));
        }
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public List<BoardColumn> getColumns() { return columns; }
    public void setColumns(List<BoardColumn> columns) { this.columns = columns; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public void addColumn(BoardColumn column) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        column.setBoard(this);
        this.columns.add(column);
    }

    public void removeColumn(BoardColumn column) {
        if (this.columns != null) {
            this.columns.remove(column);
        }
    }
}
