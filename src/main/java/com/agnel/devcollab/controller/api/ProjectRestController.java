package com.agnel.devcollab.controller.api;

import com.agnel.devcollab.entity.Project;
import com.agnel.devcollab.entity.Board;
import com.agnel.devcollab.entity.BoardColumn;
import com.agnel.devcollab.dto.CreateProjectRequest;
import com.agnel.devcollab.dto.ProjectResponse;
import com.agnel.devcollab.repository.ProjectRepository;
import com.agnel.devcollab.repository.ColumnRepository;
import com.agnel.devcollab.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@SuppressWarnings("null")
public class ProjectRestController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private BoardRepository boardRepository;

    @GetMapping
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByBoard(@PathVariable long boardId) {
        if (!boardRepository.existsById(boardId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projectRepository.findByBoardId(boardId).stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable long id) {
        return projectRepository.findById(id)
                .map(project -> ResponseEntity.ok(toResponse(project)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody CreateProjectRequest request) {
        try {
            // Validate required fields
            if (request.getBoardId() == null) {
                return ResponseEntity.badRequest().body("Board ID is required");
            }
            if (request.getBoardColumnId() == null) {
                return ResponseEntity.badRequest().body("Column ID is required");
            }

            // Fetch and validate board exists
            Board board = boardRepository.findById(request.getBoardId()).orElse(null);
            if (board == null) {
                return ResponseEntity.badRequest().body("Board not found");
            }

            // Fetch and validate column exists
            BoardColumn column = columnRepository.findById(request.getBoardColumnId()).orElse(null);
            if (column == null) {
                return ResponseEntity.badRequest().body("Column not found");
            }

            // Create new project
            Project project = new Project();
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            project.setEstimatedPomodoros(request.getEstimatedPomodoros() != null ? request.getEstimatedPomodoros() : 1);
            project.setBoard(board);
            project.setBoardColumn(column);
            project.setStatus(Project.Status.TODO);
            project.setCreatedAt(java.time.LocalDateTime.now());
            project.setTotalSecondsSpent(0);
            project.setPausedElapsedSeconds(0);

            Project saved = projectRepository.save(project);
            
            // Convert to response DTO to avoid circular references
            ProjectResponse response = new ProjectResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getEstimatedPomodoros(),
                saved.getBoard().getId(),
                saved.getBoardColumn().getId(),
                saved.getStatus().toString(),
                saved.getCreatedAt()
            );
            response.setTotalSecondsSpent(saved.getTotalSecondsSpent());
            response.setPausedElapsedSeconds(saved.getPausedElapsedSeconds());
            response.setCompletedPomodoros(saved.getCompletedPomodoros());
            response.setCurrentCycle(saved.getCurrentCycle());
            response.setPomodoroDuration(saved.getPomodoroDuration());
            response.setBreakDuration(saved.getBreakDuration());
            response.setIsBreak(saved.isBreak());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating project: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable long id, @RequestBody Project updates) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (updates.getName() != null) project.setName(updates.getName());
                    if (updates.getDescription() != null) project.setDescription(updates.getDescription());
                    if (updates.getStatus() != null) project.setStatus(updates.getStatus());
                    if (updates.getEstimatedPomodoros() != null) project.setEstimatedPomodoros(updates.getEstimatedPomodoros());
                    
                    // Support moving to a different column
                    if (updates.getBoardColumn() != null && updates.getBoardColumn().getId() != null) {
                        return columnRepository.findById(updates.getBoardColumn().getId())
                                .map(column -> {
                                    project.setBoardColumn(column);
                                    Project saved = projectRepository.save(project);
                                    return ResponseEntity.ok(toResponse(saved));
                                })
                                .orElse(ResponseEntity.notFound().build());
                    }
                    Project saved = projectRepository.save(project);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/pomodoro/start")
    public ResponseEntity<?> startPomodoro(@PathVariable long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    // If resuming from pause, adjust start time to account for paused elapsed time
                    if (project.getPausedElapsedSeconds() > 0) {
                        project.setPomodoroStart(LocalDateTime.now().minusSeconds(project.getPausedElapsedSeconds()));
                    } else {
                        project.setPomodoroStart(LocalDateTime.now());
                    }
                    project.setBreak(false);
                    Project saved = projectRepository.save(project);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pomodoro/pause")
    public ResponseEntity<?> pausePomodoro(@PathVariable long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getPomodoroStart() != null) {
                        // Calculate elapsed seconds and store for resume
                        long secondsElapsed = java.time.Duration.between(project.getPomodoroStart(), LocalDateTime.now()).getSeconds();
                        project.setPausedElapsedSeconds(secondsElapsed);
                        project.setPomodoroStart(null); // Stop the timer
                    }
                    Project saved = projectRepository.save(project);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pomodoro/stop")
    public ResponseEntity<?> stopPomodoro(@PathVariable long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getPomodoroStart() != null) {
                        long secondsSpent = java.time.Duration.between(project.getPomodoroStart(), LocalDateTime.now()).getSeconds();
                        project.setTotalSecondsSpent(project.getTotalSecondsSpent() + secondsSpent);
                        project.setPomodoroStart(null);
                        project.setPausedElapsedSeconds(0); // Clear paused state
                        
                        // Increment completed pomodoros if session was long enough (e.g., > 1 second for testing)
                        if (secondsSpent > 1) {
                            int completed = project.getCompletedPomodoros() != null ? project.getCompletedPomodoros() : 0;
                            project.setCompletedPomodoros(completed + 1);
                        }
                    } else if (project.getPausedElapsedSeconds() > 0) {
                        // If stopping while paused, add the paused time to total
                        project.setTotalSecondsSpent(project.getTotalSecondsSpent() + project.getPausedElapsedSeconds());
                        project.setPausedElapsedSeconds(0);
                    }
                    Project saved = projectRepository.save(project);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getEstimatedPomodoros(),
            project.getBoard().getId(),
            project.getBoardColumn().getId(),
            project.getStatus().toString(),
            project.getCreatedAt()
        );
        response.setTotalSecondsSpent(project.getTotalSecondsSpent());
        response.setPausedElapsedSeconds(project.getPausedElapsedSeconds());
        response.setCompletedPomodoros(project.getCompletedPomodoros());
        response.setCurrentCycle(project.getCurrentCycle());
        response.setPomodoroDuration(project.getPomodoroDuration());
        response.setBreakDuration(project.getBreakDuration());
        response.setIsBreak(project.isBreak());
        response.setPomodoroStart(project.getPomodoroStart());
        return response;
    }
}
