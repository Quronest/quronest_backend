package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsernameAndBlacklistedFalse(String username);
}
