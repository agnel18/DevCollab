package com.agnel.devcollab.service;

import com.agnel.devcollab.entity.PomodoroLog;
import com.agnel.devcollab.entity.PomodoroLog.BreakType;
import com.agnel.devcollab.entity.Subtask;
import com.agnel.devcollab.entity.User;
import com.agnel.devcollab.repository.PomodoroLogRepository;
import com.agnel.devcollab.repository.ProjectRepository;
import com.agnel.devcollab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MECE Pomodoro Service implementing 4 core pillars:
 * 1. Task Alignment - Bind Pomodoros to subtasks
 * 2. Timer Mechanics - 25/5 cycles with long breaks
 * 3. Collaboration - Real-time team sync
 * 4. Tracking/Analysis - Log sessions and calculate metrics
 */
@Service
public class PomodoroService {

    @Autowired
    private PomodoroLogRepository pomodoroLogRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ========== PILLAR 1: TASK ALIGNMENT ==========
    
    /**
     * Start a Pomodoro session on a subtask
     * @return The created PomodoroLog entity
     */
    @Transactional
    public PomodoroLog startPomodoroOnSubtask(Subtask subtask, User user) {
        // Check if there's already an active Pomodoro for this user
        Optional<PomodoroLog> activeLog = findActivePomodoro(user.getId());
        if (activeLog.isPresent()) {
            throw new IllegalStateException("User already has an active Pomodoro session");
        }
        
        PomodoroLog log = new PomodoroLog();
        log.setSubtask(subtask);
        log.setUser(user);
        log.setStartTime(LocalDateTime.now());
        log.setPomodorosUsed(1);
        log.setCompleted(false);
        log.setCycleNumber(subtask.getCurrentCycle());
        log.setBreakType(BreakType.NONE);
        log.setDistractions(0);
        
        // Set Pomodoro start time on subtask
        subtask.setPomodoroStart(LocalDateTime.now());
        
        PomodoroLog saved = pomodoroLogRepository.save(log);
        
        // Broadcast to team via WebSocket
        broadcastPomodoroStart(subtask, user);
        
        return saved;
    }
    
    /**
     * Update estimation for a subtask (1-5 Pomodoros)
     */
    @Transactional
    public void estimatePomodoros(Subtask subtask, int estimate) {
        if (estimate < 1 || estimate > 5) {
            throw new IllegalArgumentException("Estimate must be between 1 and 5 Pomodoros");
        }
        subtask.setEstimatedPomodoros(estimate);
    }
    
    /**
     * Auto-adjust estimation based on actual performance
     */
    public int suggestEstimate(Subtask subtask) {
        List<PomodoroLog> history = pomodoroLogRepository.findBySubtaskId(subtask.getId());
        if (history.isEmpty()) {
            return subtask.getEstimatedPomodoros();
        }
        
        // Calculate average Pomodoros actually used
        long totalCompleted = history.stream()
            .filter(PomodoroLog::isCompleted)
            .count();
            
        return (int) Math.ceil(Math.min(5, Math.max(1, totalCompleted)));
    }

    // ========== PILLAR 2: TIMER MECHANICS ==========
    
    /**
     * Complete a Pomodoro cycle (25 minutes work)
     */
    @Transactional
    public Map<String, Object> completeCycle(Long logId, int distractions, String notes) {
        PomodoroLog log = pomodoroLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("PomodoroLog not found"));
        
        log.setEndTime(LocalDateTime.now());
        log.setCompleted(true);
        log.setDistractions(distractions);
        log.setNotes(notes);
        
        Subtask subtask = log.getSubtask();
        
        // Update subtask progress
        subtask.setCompletedPomodoros(subtask.getCompletedPomodoros() + 1);
        subtask.incrementCycle();
        
        // Clear active timer
        subtask.setPomodoroStart(null);
        
        // Calculate total time spent
        long duration = ChronoUnit.SECONDS.between(log.getStartTime(), log.getEndTime());
        subtask.setTotalSecondsSpent(subtask.getTotalSecondsSpent() + duration);
        
        pomodoroLogRepository.save(log);
        
        // Determine break type
        BreakType breakType = determineBreakType(subtask.getCurrentCycle());
        
        Map<String, Object> result = new HashMap<>();
        result.put("breakType", breakType);
        result.put("cycleNumber", subtask.getCurrentCycle());
        result.put("completionPercentage", subtask.getCompletionPercentage());
        result.put("needsLongBreak", subtask.needsLongBreak());
        
        return result;
    }
    
    /**
     * Pause a Pomodoro with reason tracking
     */
    @Transactional
    public void pauseWithReason(Long logId, String reason) {
        PomodoroLog log = pomodoroLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("PomodoroLog not found"));
        
        if (log.isCompleted()) {
            throw new IllegalStateException("Cannot pause a completed Pomodoro");
        }
        
        // Increment distraction count
        log.setDistractions(log.getDistractions() + 1);
        
        // Append reason to notes
        String currentNotes = log.getNotes() != null ? log.getNotes() : "";
        String timestamp = LocalDateTime.now().toString();
        log.setNotes(currentNotes + "\n[" + timestamp + "] PAUSED: " + reason);
        
        pomodoroLogRepository.save(log);
    }
    
    /**
     * Determine break type based on cycle number
     */
    private BreakType determineBreakType(int cycleNumber) {
        if (cycleNumber == 1) {
            return BreakType.LONG; // Just completed 4th cycle
        } else {
            return BreakType.SHORT; // 5-minute break
        }
    }

    // ========== PILLAR 3: COLLABORATION ==========
    
    /**
     * Broadcast Pomodoro start to team via WebSocket
     */
    private void broadcastPomodoroStart(Subtask subtask, User user) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "pomodoro_start");
        message.put("subtaskId", subtask.getId());
        message.put("subtaskName", subtask.getName());
        message.put("userId", user.getId());
        message.put("username", user.getEmail());
        message.put("taskId", subtask.getTask().getId());
        message.put("projectId", subtask.getTask().getProject().getId());
        message.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/pomodoro/" + subtask.getTask().getProject().getId(), message);
    }
    
    /**
     * Get active Pomodoros for a project (team visibility)
     */
    public List<Map<String, Object>> getActiveTeamPomodoros(Long projectId) {
        List<PomodoroLog> recentLogs = pomodoroLogRepository.findRecentByProjectId(projectId);
        
        return recentLogs.stream()
            .filter(log -> !log.isCompleted() && log.getStartTime().isAfter(LocalDateTime.now().minusMinutes(30)))
            .map(log -> {
                Map<String, Object> info = new HashMap<>();
                info.put("subtaskName", log.getSubtask().getName());
                info.put("username", log.getUser().getEmail());
                info.put("startedAt", log.getStartTime());
                info.put("cycleNumber", log.getCycleNumber());
                info.put("minutesElapsed", ChronoUnit.MINUTES.between(log.getStartTime(), LocalDateTime.now()));
                return info;
            })
            .collect(Collectors.toList());
    }

    // ========== PILLAR 4: TRACKING/ANALYSIS ==========
    
    /**
     * Calculate efficiency score for a user (0-100)
     * Based on: completion rate, average distractions, estimate accuracy
     */
    public double calculateEfficiencyScore(Long userId, LocalDateTime start, LocalDateTime end) {
        List<PomodoroLog> logs = pomodoroLogRepository.findByUserIdAndDateRange(userId, start, end);
        
        if (logs.isEmpty()) return 0.0;
        
        // Completion rate (40% weight)
        long completed = logs.stream().filter(PomodoroLog::isCompleted).count();
        double completionRate = (completed * 1.0 / logs.size()) * 40;
        
        // Low distraction score (30% weight)
        double avgDistractions = logs.stream()
            .mapToInt(PomodoroLog::getDistractions)
            .average()
            .orElse(0.0);
        double distractionScore = Math.max(0, 30 - (avgDistractions * 5));
        
        // Estimate accuracy (30% weight)
        // Compare completed Pomodoros vs estimated for subtasks
        double estimateAccuracy = 30.0; // Placeholder - would need subtask comparison
        
        return Math.min(100, completionRate + distractionScore + estimateAccuracy);
    }
    
    /**
     * Generate weekly report for a user
     */
    public Map<String, Object> generateWeeklyReport(Long userId) {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();
        
        List<PomodoroLog> weekLogs = pomodoroLogRepository.findByUserIdAndDateRange(userId, weekStart, now);
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalPomodoros", weekLogs.size());
        report.put("completedPomodoros", weekLogs.stream().filter(PomodoroLog::isCompleted).count());
        report.put("totalMinutes", weekLogs.stream()
            .filter(PomodoroLog::isCompleted)
            .mapToLong(PomodoroLog::getDurationMinutes)
            .sum());
        report.put("averageDistractions", weekLogs.stream()
            .mapToInt(PomodoroLog::getDistractions)
            .average()
            .orElse(0.0));
        report.put("efficiencyScore", calculateEfficiencyScore(userId, weekStart, now));
        
        // Daily breakdown
        Map<String, Long> dailyCount = weekLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getStartTime().toLocalDate().toString(),
                Collectors.counting()
            ));
        report.put("dailyBreakdown", dailyCount);
        
        return report;
    }
    
    /**
     * Find active Pomodoro for a user
     */
    private Optional<PomodoroLog> findActivePomodoro(Long userId) {
        List<PomodoroLog> userLogs = pomodoroLogRepository.findByUserId(userId);
        return userLogs.stream()
            .filter(log -> !log.isCompleted() && log.getStartTime().isAfter(LocalDateTime.now().minusMinutes(30)))
            .findFirst();
    }
}
