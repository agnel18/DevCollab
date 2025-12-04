package com.agnel.devcollab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "board_column")
public class BoardColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int position; // Order in the board
    private String bgColor; // Background color

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    @JsonIgnoreProperties("columns")
    private Board board;

    @OneToMany(mappedBy = "boardColumn", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("boardColumn")
    private List<Project> projects = new ArrayList<>();

    // === Constructors ===
    public BoardColumn() {}

    public BoardColumn(String name, int position, Board board) {
        this.name = name;
        this.position = position;
        this.board = board;
        this.bgColor = getDefaultColorForPosition(position);
    }

    private String getDefaultColorForPosition(int position) {
        return switch (position) {
            case 0 -> "#3B82F6"; // Blue
            case 1 -> "#F59E0B"; // Yellow
            case 2 -> "#10B981"; // Green
            default -> "#6B7280"; // Gray
        };
    }

    // === Getters & Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public void addProject(Project project) {
        if (this.projects == null) {
            this.projects = new ArrayList<>();
        }
        project.setBoardColumn(this);
        this.projects.add(project);
    }

    public void removeProject(Project project) {
        if (this.projects != null) {
            this.projects.remove(project);
        }
    }
}
