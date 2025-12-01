package com.agnel.devcollab.repository;

import com.agnel.devcollab.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
}