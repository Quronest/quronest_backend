package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.DailyTaskLog;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DailyTaskLogRepository extends JpaRepository<DailyTaskLog, UUID> {
    DailyTaskLog findByTaskAndUser(DailyTask task, User user);
}
