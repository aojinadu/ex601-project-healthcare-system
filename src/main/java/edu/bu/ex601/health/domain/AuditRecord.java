package edu.bu.ex601.health.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Immutable record of who did what to which record, and when. */
public record AuditRecord(UUID actorId, UUID subjectId, AuditAction action, Instant occurredAt) {
    public AuditRecord {
        Objects.requireNonNull(actorId);
        Objects.requireNonNull(subjectId);
        Objects.requireNonNull(action);
        Objects.requireNonNull(occurredAt);
    }
}
