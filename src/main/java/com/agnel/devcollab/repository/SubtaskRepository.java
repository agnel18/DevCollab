package com.agnel.devcollab.repository;

import com.agnel.devcollab.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findByTaskId(Long taskId);
    List<Subtask> findByTaskIdOrderByIdAsc(Long taskId);
}