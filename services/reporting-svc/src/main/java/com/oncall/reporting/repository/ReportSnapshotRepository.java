package com.oncall.reporting.repository;

import com.oncall.reporting.entity.ReportSnapshotEntity;
import com.oncall.reporting.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshotEntity, UUID> {

    List<ReportSnapshotEntity> findByReportTypeOrderByGeneratedAtDesc(ReportType reportType);

    List<ReportSnapshotEntity> findByTeamIdOrderByGeneratedAtDesc(UUID teamId);

    List<ReportSnapshotEntity> findByReportTypeAndTeamIdOrderByGeneratedAtDesc(
            ReportType reportType, UUID teamId);
}
