package edu.bu.ex601.health.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** A block of time a clinician has published as bookable. */
public class AvailabilitySlot {
    private final UUID id;
    private final Clinician clinician;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private SlotStatus status = SlotStatus.OPEN;

    public AvailabilitySlot(UUID id, Clinician clinician, LocalDateTime start, LocalDateTime end) {
        this.id = Objects.requireNonNull(id);
        this.clinician = Objects.requireNonNull(clinician);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        if (!end.isAfter(start)) {
            throw new DomainException("A slot must end after it starts");
        }
    }

    public boolean overlaps(AvailabilitySlot other) {
        return clinician.getId().equals(other.clinician.getId())
                && start.isBefore(other.end)
                && other.start.isBefore(end);
    }

    public boolean isOpen() {
        return status == SlotStatus.OPEN;
    }

    /** Patient books for themself: the appointment is confirmed and the slot is taken. */
    public Appointment bookFor(Patient patient) {
        requireOpen();
        status = SlotStatus.BOOKED;
        return new Appointment(UUID.randomUUID(), patient, clinician, this, AppointmentStatus.CONFIRMED, patient.getId());
    }

    /** A representative requests a booking: the slot is held until the patient decides. */
    public Appointment holdFor(Patient patient, Representative requester) {
        requireOpen();
        status = SlotStatus.HELD;
        return new Appointment(UUID.randomUUID(), patient, clinician, this, AppointmentStatus.PENDING_APPROVAL, requester.getId());
    }

    void confirmHold() {
        if (status != SlotStatus.HELD) {
            throw new DomainException("Only a held slot can be confirmed");
        }
        status = SlotStatus.BOOKED;
    }

    void releaseHold() {
        if (status != SlotStatus.HELD) {
            throw new DomainException("Only a held slot can be released");
        }
        status = SlotStatus.OPEN;
    }

    private void requireOpen() {
        if (status != SlotStatus.OPEN) {
            throw new DomainException("Slot is no longer available");
        }
    }

    public UUID getId() { return id; }
    public Clinician getClinician() { return clinician; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public SlotStatus getStatus() { return status; }
}
