package com.agnel.devcollab.controller.api;

import com.agnel.devcollab.entity.Project;
import com.agnel.devcollab.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
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
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project updates) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (updates.getName() != null) project.setName(updates.getName());
                    if (updates.getDescription() != null) project.setDescription(updates.getDescription());
                    if (updates.getStatus() != null) {
                        project.setStatus(updates.getStatus());
                        if (updates.getStatus().toString().equals("DONE")) {
                            project.setCompletedAt(LocalDateTime.now());
                        }
                    }
                    if (updates.getEstimatedPomodoros() != null) project.setEstimatedPomodoros(updates.getEstimatedPomodoros());
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/pomodoro/start")
    public ResponseEntity<Project> startPomodoro(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setPomodoroStart(LocalDateTime.now());
                    project.setBreak(false);
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pomodoro/stop")
    public ResponseEntity<Project> stopPomodoro(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(project -> {
                    if (project.getPomodoroStart() != null) {
                        long secondsSpent = java.time.Duration.between(project.getPomodoroStart(), LocalDateTime.now()).getSeconds();
                        project.setTotalSecondsSpent(project.getTotalSecondsSpent() + secondsSpent);
                        project.setPomodoroStart(null);
                        
                        // Increment completed pomodoros if session was long enough (e.g., > 1 second for testing)
                        if (secondsSpent > 1) {
                            int completed = project.getCompletedPomodoros() != null ? project.getCompletedPomodoros() : 0;
                            project.setCompletedPomodoros(completed + 1);
                        }
                    }
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
