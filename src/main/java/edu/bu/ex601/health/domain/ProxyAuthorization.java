package edu.bu.ex601.health.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A patient's grant that lets one representative request bookings for them. */
public class ProxyAuthorization {
    private final UUID id;
    private final Patient patient;
    private final Representative representative;
    private final Instant grantedAt;
    private Instant revokedAt;

    public ProxyAuthorization(UUID id, Patient patient, Representative representative, Instant grantedAt) {
        this.id = Objects.requireNonNull(id);
        this.patient = Objects.requireNonNull(patient);
        this.representative = Objects.requireNonNull(representative);
        this.grantedAt = Objects.requireNonNull(grantedAt);
    }

    /** True when this grant lets the representative act for the patient at the given moment. */
    public boolean permits(Representative actor, Patient forPatient, Instant at) {
        return representative.getId().equals(actor.getId())
                && patient.getId().equals(forPatient.getId())
                && !at.isBefore(grantedAt)
                && (revokedAt == null || at.isBefore(revokedAt));
    }

    public void revoke(Instant at) {
        if (revokedAt != null) {
            throw new DomainException("Authorization is already revoked");
        }
        this.revokedAt = Objects.requireNonNull(at);
    }

    public UUID getId() { return id; }
    public Patient getPatient() { return patient; }
    public Representative getRepresentative() { return representative; }
    public Instant getGrantedAt() { return grantedAt; }
    public Instant getRevokedAt() { return revokedAt; }
}
