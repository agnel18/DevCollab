package com.agnel.devcollab.repository;

import com.agnel.devcollab.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);
    List<Project> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<Project> findByBoardId(Long boardId);
    List<Project> findByBoardColumnId(Long boardColumnId);
}