package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.TimeEntry;
import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.entity.Project;
import com.agnel.devcollab.entity.Task;
import com.agnel.devcollab.entity.Subtask;
import com.agnel.devcollab.repository.TimeEntryRepository;
import com.agnel.devcollab.repository.UserRepository;
import com.agnel.devcollab.repository.ProjectRepository;
import com.agnel.devcollab.repository.TaskRepository;
import com.agnel.devcollab.repository.SubtaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/timer")
public class TimerController {
    @Autowired
    private TimeEntryRepository timeEntryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private SubtaskRepository subtaskRepository;

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody TimerStartRequest req) {
        TimeEntry entry = new TimeEntry();
        
        // Set entities from IDs if provided
        if (req.getUserId() != null) {
            User user = userRepository.findById(req.getUserId()).orElse(null);
            entry.setUser(user);
        }
        if (req.getProjectId() != null) {
            Project project = projectRepository.findById(req.getProjectId()).orElse(null);
            entry.setProject(project);
        }
        if (req.getTaskId() != null) {
            Task task = taskRepository.findById(req.getTaskId()).orElse(null);
            entry.setTask(task);
        }
        if (req.getSubtaskId() != null) {
            Subtask subtask = subtaskRepository.findById(req.getSubtaskId()).orElse(null);
            entry.setSubtask(subtask);
        }
        
        entry.setDescription(req.getDescription());
        entry.setTags(req.getTags());
        entry.setStart(LocalDateTime.now());
        entry.setPomodoro(req.isPomodoro());
        entry.setBillable(req.isBillable());
        
        TimeEntry saved = timeEntryRepository.save(entry);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop(@RequestBody TimerStopRequest req) {
        TimeEntry entry = timeEntryRepository.findById(req.getEntryId())
            .orElseThrow(() -> new RuntimeException("TimeEntry not found"));
        entry.setEnd(LocalDateTime.now());
        TimeEntry saved = timeEntryRepository.save(entry);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActive(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            userId = 1L; // Default for testing
        }
        List<TimeEntry> active = timeEntryRepository.findByUserId(userId)
            .stream().filter(e -> e.getEnd() == null).toList();
        return ResponseEntity.ok(active);
    }
}

// DTOs for requests (using IDs instead of full entities)
class TimerStartRequest {
    private Long userId;
    private Long projectId;
    private Long taskId;
    private Long subtaskId;
    private String description;
    private java.util.Set<String> tags;
    private boolean pomodoro;
    private boolean billable;
    
    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getSubtaskId() { return subtaskId; }
    public void setSubtaskId(Long subtaskId) { this.subtaskId = subtaskId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public java.util.Set<String> getTags() { return tags; }
    public void setTags(java.util.Set<String> tags) { this.tags = tags; }
    public boolean isPomodoro() { return pomodoro; }
    public void setPomodoro(boolean pomodoro) { this.pomodoro = pomodoro; }
    public boolean isBillable() { return billable; }
    public void setBillable(boolean billable) { this.billable = billable; }
}

class TimerStopRequest {
    private Long entryId;
    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }
}
