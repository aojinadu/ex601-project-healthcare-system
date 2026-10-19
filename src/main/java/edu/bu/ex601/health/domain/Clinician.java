package edu.bu.ex601.health.domain;

import java.util.Objects;
import java.util.UUID;

public class Clinician {
    private final UUID id;
    private final String fullName;
    private final String licenceNumber;

    public Clinician(UUID id, String fullName, String licenceNumber) {
        this.id = Objects.requireNonNull(id);
        this.fullName = Objects.requireNonNull(fullName);
        this.licenceNumber = Objects.requireNonNull(licenceNumber);
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getLicenceNumber() { return licenceNumber; }
}
