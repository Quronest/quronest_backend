package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.Anchor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnchorRepository extends JpaRepository<Anchor, UUID> {

    List<Anchor> findByReferenceId(UUID referenceId);
}
