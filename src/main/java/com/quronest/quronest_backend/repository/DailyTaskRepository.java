package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.DailyTask;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, UUID> {
    List<DailyTask> findByPlanOrderByOrderAsc(DailyPlan plan);
}
