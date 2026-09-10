package com.example.nba.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByEntityAndEntityIdOrderByCreatedAtDesc(String entity, String entityId);
}
