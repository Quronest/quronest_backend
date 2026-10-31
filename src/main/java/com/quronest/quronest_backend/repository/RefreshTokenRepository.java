package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.RefreshToken;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    RefreshToken findByTokenAndExpiryDateLessThanEqual(String token, LocalDateTime expiryDate);

    List<RefreshToken> findAllByUser(User user);

    void deleteByToken(String token);

    void deleteAllByUser(User user);
}
