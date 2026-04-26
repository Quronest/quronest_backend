package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.UserJourneyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserJourneyHistoryRepository extends JpaRepository<UserJourneyHistory, UUID> {
}
