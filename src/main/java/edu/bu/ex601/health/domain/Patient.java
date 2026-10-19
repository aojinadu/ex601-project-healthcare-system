package edu.bu.ex601.health.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Patient {
    private final UUID id;
    private final String name;
    private final LocalDate dateOfBirth;

    public Patient(UUID id, String name, LocalDate dateOfBirth) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
}
