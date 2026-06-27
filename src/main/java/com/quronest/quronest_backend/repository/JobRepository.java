package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Job findByIdAndStatus(UUID id, JobStatus status);

    Job findByUserAndTypeAndStatusIn(User user, JobType type, Collection<JobStatus> statuses);
}
