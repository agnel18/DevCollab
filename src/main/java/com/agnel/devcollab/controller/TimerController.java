package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.TimeEntry;
import com.agnel.devcollab.repository.TimeEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/start")
    public TimeEntry start(@RequestBody TimerStartRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        TimeEntry entry = new TimeEntry();
        entry.setUser(req.getUser());
        entry.setProject(req.getProject());
        entry.setSubtask(req.getSubtask());
        entry.setDescription(req.getDescription());
        entry.setTags(req.getTags());
        entry.setStart(LocalDateTime.now());
        entry.setPomodoro(req.isPomodoro());
        entry.setBillable(req.isBillable());
        return timeEntryRepository.save(entry);
    }

    @PostMapping("/stop")
    public TimeEntry stop(@RequestBody TimerStopRequest req) {
        TimeEntry entry = timeEntryRepository.findById(req.getEntryId()).orElseThrow();
        entry.setEnd(LocalDateTime.now());
        return timeEntryRepository.save(entry);
    }

    @GetMapping("/active")
    public List<TimeEntry> getActive(@AuthenticationPrincipal UserDetails userDetails) {
        // Return all running timers for the user
        return timeEntryRepository.findByUserId(/* get userId from userDetails */ 1L)
            .stream().filter(e -> e.getEnd() == null).toList();
    }
}

// DTOs for requests
class TimerStartRequest {
    private com.agnel.devcollab.entity.User user;
    private com.agnel.devcollab.entity.Project project;
    private com.agnel.devcollab.entity.Subtask subtask;
    private String description;
    private java.util.Set<String> tags;
    private boolean pomodoro;
    private boolean billable;
    // getters/setters
    public com.agnel.devcollab.entity.User getUser() { return user; }
    public void setUser(com.agnel.devcollab.entity.User user) { this.user = user; }
    public com.agnel.devcollab.entity.Project getProject() { return project; }
    public void setProject(com.agnel.devcollab.entity.Project project) { this.project = project; }
    public com.agnel.devcollab.entity.Subtask getSubtask() { return subtask; }
    public void setSubtask(com.agnel.devcollab.entity.Subtask subtask) { this.subtask = subtask; }
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
