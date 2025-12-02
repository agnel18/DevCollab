package com.agnel.devcollab.repository;

import com.agnel.devcollab.entity.PomodoroLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PomodoroLogRepository extends JpaRepository<PomodoroLog, Long> {
    
    // Find all logs for a specific subtask
    List<PomodoroLog> findBySubtaskId(Long subtaskId);
    
    // Find all logs for a user
    List<PomodoroLog> findByUserId(Long userId);
    
    // Find completed Pomodoros
    List<PomodoroLog> findByCompletedTrue();
    
    // Find Pomodoros by user and completion status
    List<PomodoroLog> findByUserIdAndCompleted(Long userId, boolean completed);
    
    // Find Pomodoros in a date range for analytics
    @Query("SELECT p FROM PomodoroLog p WHERE p.startTime >= :start AND p.endTime <= :end")
    List<PomodoroLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Find user's Pomodoros in a date range
    @Query("SELECT p FROM PomodoroLog p WHERE p.user.id = :userId AND p.startTime >= :start AND p.endTime <= :end")
    List<PomodoroLog> findByUserIdAndDateRange(
        @Param("userId") Long userId, 
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
    
    // Get efficiency statistics
    @Query("SELECT AVG(p.distractions) FROM PomodoroLog p WHERE p.user.id = :userId AND p.completed = true")
    Double getAverageDistractionsByUserId(@Param("userId") Long userId);
    
    // Count completed Pomodoros for a user
    @Query("SELECT COUNT(p) FROM PomodoroLog p WHERE p.user.id = :userId AND p.completed = true")
    Long countCompletedPomodorosByUserId(@Param("userId") Long userId);
    
    // Find recent Pomodoros for team visibility
    @Query("SELECT p FROM PomodoroLog p WHERE p.subtask.project.id = :projectId ORDER BY p.startTime DESC")
    List<PomodoroLog> findRecentByProjectId(@Param("projectId") Long projectId);
}
