package com.oncall.worklog.repository;

import com.oncall.worklog.entity.WorkLogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkLogEntryRepository extends JpaRepository<WorkLogEntryEntity, UUID> {
    List<WorkLogEntryEntity> findByWorkLogIdOrderByOccurredAtAsc(UUID workLogId);
}
