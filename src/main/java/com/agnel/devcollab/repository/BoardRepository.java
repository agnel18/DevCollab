package com.agnel.devcollab.repository;

import com.agnel.devcollab.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByOwnerId(Long ownerId);
    List<Board> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
