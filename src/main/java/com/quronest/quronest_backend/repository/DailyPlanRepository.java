package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.enums.UserGroup;
import com.quronest.quronest_backend.model.enums.UserPhase;
import com.quronest.quronest_backend.model.table.DailyPlan;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, UUID> {
    List<DailyPlan> findByUserAndPlanDateBetweenOrderByPlanDateAsc(User user, LocalDate planDateStart,
                                                                   LocalDate planDateEnd);

    DailyPlan findByUserAndGroupAndPhaseAndPlanDate(User user, UserGroup group, UserPhase phase, LocalDate planDate);
}
