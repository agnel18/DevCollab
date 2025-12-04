package com.agnel.devcollab.controller.api;

import com.agnel.devcollab.entity.Project;
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
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<Project>> getProjectsByBoard(@PathVariable long boardId) {
        if (!boardRepository.existsById(boardId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(projectRepository.findByBoardId(boardId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Project createProject(@RequestBody Project project) {
        project.setCreatedAt(LocalDateTime.now());
        return projectRepository.save(project);
    }

    @PatchMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<Project> updateProject(@PathVariable long id, @RequestBody Project updates) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (updates.getName() != null) project.setName(updates.getName());
                    if (updates.getDescription() != null) project.setDescription(updates.getDescription());
                    // Support moving to a different column
                    if (updates.getBoardColumn() != null && updates.getBoardColumn().getId() != null) {
                        return columnRepository.findById(updates.getBoardColumn().getId())
                                .map(column -> {
                                    project.setBoardColumn(column);
                                    if (updates.getEstimatedPomodoros() != null) project.setEstimatedPomodoros(updates.getEstimatedPomodoros());
                                    return ResponseEntity.ok(projectRepository.save(project));
                                })
                                .orElse(ResponseEntity.notFound().build());
                    }
                    if (updates.getEstimatedPomodoros() != null) project.setEstimatedPomodoros(updates.getEstimatedPomodoros());
                    return ResponseEntity.ok(projectRepository.save(project));
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
    public ResponseEntity<Project> startPomodoro(@PathVariable long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    // If resuming from pause, adjust start time to account for paused elapsed time
                    if (project.getPausedElapsedSeconds() > 0) {
                        project.setPomodoroStart(LocalDateTime.now().minusSeconds(project.getPausedElapsedSeconds()));
                    } else {
                        project.setPomodoroStart(LocalDateTime.now());
                    }
                    project.setBreak(false);
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pomodoro/pause")
    public ResponseEntity<Project> pausePomodoro(@PathVariable long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getPomodoroStart() != null) {
                        // Calculate elapsed seconds and store for resume
                        long secondsElapsed = java.time.Duration.between(project.getPomodoroStart(), LocalDateTime.now()).getSeconds();
                        project.setPausedElapsedSeconds(secondsElapsed);
                        project.setPomodoroStart(null); // Stop the timer
                    }
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pomodoro/stop")
    public ResponseEntity<Project> stopPomodoro(@PathVariable long id) {
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
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
