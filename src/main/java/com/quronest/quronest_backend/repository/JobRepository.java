package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Job findByIdAndStatus(UUID id, JobStatus status);

    Job findByUserAndTypeAndStatusIn(User user, JobType type, Collection<JobStatus> statuses);

    Job findByIdAndUser(UUID id, User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Job j SET j.status = :targetStatus, j.failedAt = :now, j.updateTimestamp = :now " +
            "WHERE j.status = :sourceStatus AND j.expiredAt IS NOT NULL AND j.expiredAt <= :now")
    int updateStatusForExpiredJobs(@Param("sourceStatus") JobStatus sourceStatus,
                                   @Param("targetStatus") JobStatus targetStatus,
                                   @Param("now") LocalDateTime now);
}
