package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.table.Anchor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnchorRepository extends JpaRepository<Anchor, UUID> {

    List<Anchor> findByReferenceId(UUID referenceId);
}
