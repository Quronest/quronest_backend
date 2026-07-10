package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.Note;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    Note findByIdAndUser(UUID id, User user);

    @Query("""
            SELECT n FROM Note n WHERE n.user = :user
            AND (:taskId IS NULL OR n.task.id = :taskId)
            AND (cast(:startTime as timestamp) IS NULL OR n.creationTimestamp >= :startTime)
            AND (cast(:endTime as timestamp) IS NULL OR n.creationTimestamp <= :endTime)
            """)
    Page<Note> findNotesWithFilters(
            @Param("user") User user,
            @Param("taskId") UUID taskId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
