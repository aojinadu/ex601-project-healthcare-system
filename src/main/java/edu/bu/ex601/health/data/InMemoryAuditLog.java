package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.AuditLog;
import edu.bu.ex601.health.domain.AuditRecord;
import java.util.*;

/** Append-only: there is no update or delete operation. */
public class InMemoryAuditLog implements AuditLog {
    private final List<AuditRecord> entries = new ArrayList<>();

    @Override public synchronized void record(AuditRecord entry) { entries.add(entry); }
    @Override public synchronized List<AuditRecord> findBySubject(UUID subjectId) {
        return entries.stream().filter(e -> e.subjectId().equals(subjectId)).toList();
    }
}
