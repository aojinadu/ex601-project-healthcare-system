package edu.bu.ex601.health.domain;

import java.util.Optional;
import java.util.UUID;

public interface VisitNoteRepository {
    Optional<VisitNote> findById(UUID id);
    void save(VisitNote note);
}
