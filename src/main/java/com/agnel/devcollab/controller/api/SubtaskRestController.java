package com.agnel.devcollab.controller.api;

import com.agnel.devcollab.entity.Subtask;
import com.agnel.devcollab.repository.SubtaskRepository;
import com.agnel.devcollab.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/subtasks")
public class SubtaskRestController {

    @Autowired
    private SubtaskRepository subtaskRepository;

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public List<Subtask> getAllSubtasks() {
        return subtaskRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subtask> getSubtask(@PathVariable Long id) {
        return subtaskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/task/{taskId}")
    public List<Subtask> getSubtasksByTask(@PathVariable Long taskId) {
        return subtaskRepository.findByTaskId(taskId);
    }

    @PostMapping
    public ResponseEntity<Subtask> createSubtask(@RequestBody Subtask subtask) {
        if (subtask.getTask() == null || subtask.getTask().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return taskRepository.findById(subtask.getTask().getId())
                .map(task -> {
                    subtask.setTask(task);
                    return ResponseEntity.ok(subtaskRepository.save(subtask));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Subtask> updateSubtask(@PathVariable Long id, @RequestBody Subtask updates) {
        return subtaskRepository.findById(id)
                .map(subtask -> {
                    if (updates.getName() != null) subtask.setName(updates.getName());
                    if (updates.getCompleted() != null) subtask.setCompleted(updates.getCompleted());
                    if (updates.getEstimatedPomodoros() != null) subtask.setEstimatedPomodoros(updates.getEstimatedPomodoros());
                    return ResponseEntity.ok(subtaskRepository.save(subtask));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtask(@PathVariable Long id) {
        if (subtaskRepository.existsById(id)) {
            subtaskRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/pomodoro/start")
    public ResponseEntity<Subtask> startPomodoro(@PathVariable Long id) {
        return subtaskRepository.findById(id)
                .map(subtask -> {
                    subtask.setPomodoroStart(LocalDateTime.now());
                    return ResponseEntity.ok(subtaskRepository.save(subtask));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pomodoro/stop")
    public ResponseEntity<Subtask> stopPomodoro(@PathVariable Long id) {
        return subtaskRepository.findById(id)
                .map(subtask -> {
                    if (subtask.getPomodoroStart() != null) {
                        long secondsSpent = java.time.Duration.between(subtask.getPomodoroStart(), LocalDateTime.now()).getSeconds();
                        subtask.setTotalSecondsSpent(subtask.getTotalSecondsSpent() + secondsSpent);
                        subtask.setPomodoroStart(null);
                        
                        if (secondsSpent > 1) {
                            int completed = subtask.getCompletedPomodoros() != null ? subtask.getCompletedPomodoros() : 0;
                            subtask.setCompletedPomodoros(completed + 1);
                        }
                    }
                    return ResponseEntity.ok(subtaskRepository.save(subtask));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
