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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {
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

    // Get all time entries for the current user for a given week
    @GetMapping("/week")
    public ResponseEntity<?> getWeekEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start) {
        LocalDateTime weekStart = start.atStartOfDay();
        LocalDateTime weekEnd = start.plusDays(7).atTime(LocalTime.MAX);
        Long userId = 1L; // Default for testing
        List<TimeEntry> entries = timeEntryRepository.findByUserIdAndStartBetween(userId, weekStart, weekEnd);
        return ResponseEntity.ok(entries);
    }

    // Create a new time entry
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TimeEntryRequest req) {
        TimeEntry entry = new TimeEntry();
        
        // Set entities from IDs
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
        entry.setStart(req.getStart());
        entry.setEnd(req.getEnd());
        entry.setTags(req.getTags());
        entry.setPomodoro(req.isPomodoro());
        entry.setBillable(req.isBillable());
        
        TimeEntry saved = timeEntryRepository.save(entry);
        return ResponseEntity.ok(saved);
    }

    // Update an existing time entry (resize, move, edit details)
    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TimeEntryUpdateRequest req) {
        TimeEntry entry = timeEntryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("TimeEntry not found"));
        
        if (req.getStart() != null) entry.setStart(req.getStart());
        if (req.getEnd() != null) entry.setEnd(req.getEnd());
        if (req.getDescription() != null) entry.setDescription(req.getDescription());
        if (req.getTags() != null) entry.setTags(req.getTags());
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
        
        TimeEntry saved = timeEntryRepository.save(entry);
        return ResponseEntity.ok(saved);
    }

    // Delete a time entry
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        timeEntryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

// DTO for creating time entries
class TimeEntryRequest {
    private Long userId;
    private Long projectId;
    private Long taskId;
    private Long subtaskId;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private Set<String> tags;
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
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public boolean isPomodoro() { return pomodoro; }
    public void setPomodoro(boolean pomodoro) { this.pomodoro = pomodoro; }
    public boolean isBillable() { return billable; }
    public void setBillable(boolean billable) { this.billable = billable; }
}

// DTO for updating time entries
class TimeEntryUpdateRequest {
    private Long projectId;
    private Long taskId;
    private Long subtaskId;
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private Set<String> tags;
    
    // Getters and setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getSubtaskId() { return subtaskId; }
    public void setSubtaskId(Long subtaskId) { this.subtaskId = subtaskId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
}
