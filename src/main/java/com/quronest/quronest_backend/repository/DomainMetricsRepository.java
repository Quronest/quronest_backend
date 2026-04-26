package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.DomainMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DomainMetricsRepository extends JpaRepository<DomainMetrics, UUID> {
}
