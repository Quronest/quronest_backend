package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.model.table.UserJourney;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserJourneyRepository extends JpaRepository<UserJourney, UUID> {
    UserJourney findByUser(User user);
}
