package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.Task;
import com.agnel.devcollab.entity.Project;
import com.agnel.devcollab.repository.TaskRepository;
import com.agnel.devcollab.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
@SuppressWarnings("null")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    // Get all tasks for a project
    @GetMapping
    public ResponseEntity<?> getTasksByProject(@RequestParam Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdOrderByIdAsc(projectId);
        return ResponseEntity.ok(tasks);
    }

    // Get single task
    @GetMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<?> getTask(@PathVariable Long id) {
        return taskRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Create new task
    @PostMapping
    @SuppressWarnings("null")
    public ResponseEntity<?> createTask(@RequestBody TaskRequest req) {
        Project project = projectRepository.findById(req.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));

        Task task = new Task();
        task.setName(req.getName());
        task.setDescription(req.getDescription());
        task.setProject(project);
        task.setStatus(Task.Status.TODO);
        task.setCreatedAt(LocalDateTime.now());
        
        if (req.getEstimatedPomodoros() != null) {
            task.setEstimatedPomodoros(req.getEstimatedPomodoros());
        }
        
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    // Update task (including DONE tasks - always editable)
    @PatchMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskUpdateRequest req) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        if (req.getName() != null) task.setName(req.getName());
        if (req.getDescription() != null) task.setDescription(req.getDescription());
        if (req.getStatus() != null) task.setStatus(req.getStatus());
        if (req.getEstimatedPomodoros() != null) task.setEstimatedPomodoros(req.getEstimatedPomodoros());

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    // Delete task
    @DeleteMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Start Pomodoro timer on task
    @PostMapping("/{id}/pomodoro/start")
    @SuppressWarnings("null")
    public ResponseEntity<?> startPomodoro(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getPomodoroStart() != null) {
            return ResponseEntity.badRequest().body("Timer already running");
        }

        task.setPomodoroStart(LocalDateTime.now());
        task.setBreak(false);
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    // Stop Pomodoro timer on task
    @PostMapping("/{id}/pomodoro/stop")
    @SuppressWarnings("null")
    public ResponseEntity<?> stopPomodoro(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getPomodoroStart() == null) {
            return ResponseEntity.badRequest().body("No timer running");
        }

        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = java.time.Duration.between(task.getPomodoroStart(), now).getSeconds();
        
        task.setTotalSecondsSpent(task.getTotalSecondsSpent() + secondsElapsed);
        task.setPomodoroStart(null);
        
        if (!task.isBreak()) {
            task.setCompletedPomodoros(task.getCompletedPomodoros() + 1);
            task.incrementCycle();
        }

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    // Move task status (TODO -> DOING -> DONE, and back)
    @PostMapping("/{id}/move")
    @SuppressWarnings("null")
    public ResponseEntity<?> moveTask(@PathVariable Long id, @RequestParam Task.Status newStatus) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }
}

// DTOs
class TaskRequest {
    private Long projectId;
    private String name;
    private String description;
    private Integer estimatedPomodoros;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getEstimatedPomodoros() { return estimatedPomodoros; }
    public void setEstimatedPomodoros(Integer estimatedPomodoros) { this.estimatedPomodoros = estimatedPomodoros; }
}

class TaskUpdateRequest {
    private String name;
    private String description;
    private Task.Status status;
    private Integer estimatedPomodoros;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Task.Status getStatus() { return status; }
    public void setStatus(Task.Status status) { this.status = status; }
    public Integer getEstimatedPomodoros() { return estimatedPomodoros; }
    public void setEstimatedPomodoros(Integer estimatedPomodoros) { this.estimatedPomodoros = estimatedPomodoros; }
}
