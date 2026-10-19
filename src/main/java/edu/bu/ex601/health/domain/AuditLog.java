package edu.bu.ex601.health.domain;

import java.util.List;
import java.util.UUID;

public interface AuditLog {
    void record(AuditRecord entry);
    List<AuditRecord> findBySubject(UUID subjectId);
}
