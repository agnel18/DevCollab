package com.agnel.devcollab.controller;

import com.agnel.devcollab.entity.PomodoroLog;
import com.agnel.devcollab.entity.Subtask;
import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.repository.UserRepository;
import com.agnel.devcollab.service.PomodoroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for MECE Pomodoro System
 * Handles REST API endpoints and WebSocket messages
 */
@Controller
@RequestMapping("/pomodoro")
@SuppressWarnings("null")
public class PomodoroController {

    @Autowired
    private PomodoroService pomodoroService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.agnel.devcollab.repository.SubtaskRepository subtaskRepository;

    // ========== REST API ENDPOINTS ==========
    
    /**
     * Start a Pomodoro on a subtask
     */
    @PostMapping("/subtasks/{subtaskId}/start")
    @ResponseBody
    @SuppressWarnings("null")
    public ResponseEntity<?> startPomodoro(
            @PathVariable Long subtaskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new IllegalArgumentException("Subtask not found"));
            
            PomodoroLog log = pomodoroService.startPomodoroOnSubtask(subtask, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logId", log.getId());
            response.put("startTime", log.getStartTime().toString());
            response.put("cycleNumber", log.getCycleNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Pause a Pomodoro with reason
     */
    @PostMapping("/logs/{logId}/pause")
    @ResponseBody
    public ResponseEntity<?> pausePomodoro(
            @PathVariable Long logId,
            @RequestParam String reason) {
        
        try {
            pomodoroService.pauseWithReason(logId, reason);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Complete a Pomodoro cycle
     */
    @PostMapping("/logs/{logId}/complete")
    @ResponseBody
    public ResponseEntity<?> completePomodoro(
            @PathVariable Long logId,
            @RequestParam(defaultValue = "0") int distractions,
            @RequestParam(required = false) String notes) {
        
        try {
            Map<String, Object> result = pomodoroService.completeCycle(logId, distractions, notes != null ? notes : "");
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update estimation for a subtask
     */
    @PostMapping("/subtasks/{subtaskId}/estimate")
    @ResponseBody
    @SuppressWarnings("null")
    public ResponseEntity<?> updateEstimate(
            @PathVariable Long subtaskId,
            @RequestParam int estimate) {
        
        try {
            Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new IllegalArgumentException("Subtask not found"));
            
            pomodoroService.estimatePomodoros(subtask, estimate);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get active team Pomodoros for a project
     */
    @GetMapping("/projects/{projectId}/active")
    @ResponseBody
    public ResponseEntity<?> getActiveTeamPomodoros(@PathVariable Long projectId) {
        List<Map<String, Object>> activePomodoros = pomodoroService.getActiveTeamPomodoros(projectId);
        return ResponseEntity.ok(activePomodoros);
    }
    
    /**
     * Get user's weekly report
     */
    @GetMapping("/reports/weekly")
    @ResponseBody
    public ResponseEntity<?> getWeeklyReport(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Map<String, Object> report = pomodoroService.generateWeeklyReport(user.getId());
        return ResponseEntity.ok(report);
    }
    
    /**
     * Analytics dashboard page
     */
    @GetMapping("/projects/{projectId}/analytics")
    public String showAnalytics(@PathVariable Long projectId, Model model, 
                                @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get weekly report
        Map<String, Object> weeklyReport = pomodoroService.generateWeeklyReport(user.getId());
        model.addAttribute("weeklyReport", weeklyReport);
        
        // Get efficiency score
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        double efficiency = pomodoroService.calculateEfficiencyScore(user.getId(), weekStart, LocalDateTime.now());
        model.addAttribute("efficiencyScore", efficiency);
        
        // Get active team Pomodoros
        List<Map<String, Object>> teamPomodoros = pomodoroService.getActiveTeamPomodoros(projectId);
        model.addAttribute("teamPomodoros", teamPomodoros);
        
        model.addAttribute("projectId", projectId);
        
        return "pomodoro/analytics";
    }

    // ========== WEBSOCKET HANDLERS ==========
    
    /**
     * Handle WebSocket Pomodoro start broadcast
     */
    @MessageMapping("/pomodoro/start")
    @SendTo("/topic/pomodoro")
    public Map<String, Object> handlePomodoroStart(Map<String, Object> message) {
        // Simply broadcast to all subscribers
        message.put("timestamp", LocalDateTime.now().toString());
        return message;
    }
    
    /**
     * Handle WebSocket Pomodoro completion broadcast
     */
    @MessageMapping("/pomodoro/complete")
    @SendTo("/topic/pomodoro")
    public Map<String, Object> handlePomodoroComplete(Map<String, Object> message) {
        message.put("timestamp", LocalDateTime.now().toString());
        return message;
    }
}
