package edu.bu.ex601.health.domain;

import java.util.Objects;
import java.util.UUID;

/** A trusted person (family member, caregiver) who may book on a patient's behalf. */
public class Representative {
    private final UUID id;
    private final String fullName;

    public Representative(UUID id, String fullName) {
        this.id = Objects.requireNonNull(id);
        this.fullName = Objects.requireNonNull(fullName);
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
}
