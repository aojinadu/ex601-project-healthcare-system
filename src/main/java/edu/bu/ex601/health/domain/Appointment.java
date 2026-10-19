package edu.bu.ex601.health.domain;

import java.util.Objects;
import java.util.UUID;

public class Appointment {
    private final UUID id;
    private final Patient patient;
    private final Clinician clinician;
    private final AvailabilitySlot slot;
    private final UUID requestedBy;
    private AppointmentStatus status;

    Appointment(UUID id, Patient patient, Clinician clinician, AvailabilitySlot slot,
                AppointmentStatus status, UUID requestedBy) {
        this.id = Objects.requireNonNull(id);
        this.patient = Objects.requireNonNull(patient);
        this.clinician = Objects.requireNonNull(clinician);
        this.slot = Objects.requireNonNull(slot);
        this.status = Objects.requireNonNull(status);
        this.requestedBy = Objects.requireNonNull(requestedBy);
    }

    /** Only the patient the appointment is for may approve a proxy request. */
    public void approve(Patient decider) {
        requirePendingFor(decider);
        slot.confirmHold();
        status = AppointmentStatus.CONFIRMED;
    }

    public void decline(Patient decider) {
        requirePendingFor(decider);
        slot.releaseHold();
        status = AppointmentStatus.DECLINED;
    }

    public boolean isConductedBy(Clinician candidate) {
        return clinician.getId().equals(candidate.getId());
    }

    /** Creator: the appointment holds the patient, clinician and itself, which a note needs. */
    public VisitNote startVisitNote(Clinician author) {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new DomainException("Notes can only be written for a confirmed appointment");
        }
        if (!isConductedBy(author)) {
            throw new DomainException("Only the conducting clinician may start a note");
        }
        return new VisitNote(UUID.randomUUID(), this, author, null);
    }

    private void requirePendingFor(Patient decider) {
        if (!patient.getId().equals(decider.getId())) {
            throw new DomainException("Only the patient can decide on this request");
        }
        if (status != AppointmentStatus.PENDING_APPROVAL) {
            throw new DomainException("Appointment is not waiting for approval");
        }
    }

    public UUID getId() { return id; }
    public Patient getPatient() { return patient; }
    public Clinician getClinician() { return clinician; }
    public AvailabilitySlot getSlot() { return slot; }
    public UUID getRequestedBy() { return requestedBy; }
    public AppointmentStatus getStatus() { return status; }
}
