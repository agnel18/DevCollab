package com.agnel.devcollab.repository;

import com.agnel.devcollab.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByUserId(Long userId);
    List<TimeEntry> findByProjectId(Long projectId);
    List<TimeEntry> findByTaskId(Long taskId);
    List<TimeEntry> findBySubtaskId(Long subtaskId);
    List<TimeEntry> findByUserIdAndStartBetween(Long userId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
