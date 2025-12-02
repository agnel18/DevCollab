package com.agnel.devcollab.controller.api;

import com.agnel.devcollab.entity.Task;
import com.agnel.devcollab.repository.TaskRepository;
import com.agnel.devcollab.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}")
    public List<Task> getTasksByProject(@PathVariable Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        if (task.getProject() == null || task.getProject().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return projectRepository.findById(task.getProject().getId())
                .map(project -> {
                    task.setProject(project);
                    task.setCreatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updates) {
        return taskRepository.findById(id)
                .map(task -> {
                    if (updates.getName() != null) task.setName(updates.getName());
                    if (updates.getDescription() != null) task.setDescription(updates.getDescription());
                    if (updates.getStatus() != null) {
                        task.setStatus(updates.getStatus());
                        if (updates.getStatus().toString().equals("DONE")) {
                            task.setCompletedAt(LocalDateTime.now());
                        }
                    }
                    if (updates.getEstimatedPomodoros() != null) task.setEstimatedPomodoros(updates.getEstimatedPomodoros());
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/pomodoro/start")
    public ResponseEntity<Task> startPomodoro(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setPomodoroStart(LocalDateTime.now());
                    task.setIsBreak(false);
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pomodoro/stop")
    public ResponseEntity<Task> stopPomodoro(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    if (task.getPomodoroStart() != null) {
                        long secondsSpent = java.time.Duration.between(task.getPomodoroStart(), LocalDateTime.now()).getSeconds();
                        task.setTotalSecondsSpent(task.getTotalSecondsSpent() + secondsSpent);
                        task.setPomodoroStart(null);
                        
                        if (secondsSpent > 1) {
                            int completed = task.getCompletedPomodoros() != null ? task.getCompletedPomodoros() : 0;
                            task.setCompletedPomodoros(completed + 1);
                        }
                    }
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
