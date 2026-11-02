package edu.bu.ex601.health.presentation;

import edu.bu.ex601.health.data.*;
import edu.bu.ex601.health.domain.*;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Shared wiring for the A2 visit-note tests: in-memory repositories, a fixed clock,
 * Dr Okafor (conducting), Dr Chen (not on the appointment) and one CONFIRMED appointment.
 */
class VisitNoteFixture {
    static final Instant NOW = Instant.parse("2026-11-02T10:00:00Z");

    final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    final InMemoryAppointmentRepository appointments = new InMemoryAppointmentRepository();
    final InMemoryClinicianRepository clinicians = new InMemoryClinicianRepository();
    final InMemoryVisitNoteRepository notes = new InMemoryVisitNoteRepository();
    final InMemoryAuditLog audit = new InMemoryAuditLog();
    final RecordVisitNoteController controller =
            new RecordVisitNoteController(appointments, clinicians, notes, audit, clock);

    final Clinician okafor = new Clinician(UUID.randomUUID(), "Dr. Ada Okafor", "NB-12345");
    final Clinician chen = new Clinician(UUID.randomUUID(), "Dr. Wei Chen", "NB-67890");
    final Patient margaret = new Patient(UUID.randomUUID(), "Margaret Doyle", LocalDate.of(1948, 3, 2));
    final Appointment confirmed;

    VisitNoteFixture() {
        clinicians.save(okafor);
        clinicians.save(chen);
        confirmed = newSlot(9).bookFor(margaret);           // self-booked -> CONFIRMED
        appointments.save(confirmed);
    }

    AvailabilitySlot newSlot(int hour) {
        LocalDateTime start = LocalDateTime.of(2026, 11, 2, hour, 0);
        return new AvailabilitySlot(UUID.randomUUID(), okafor, start, start.plusMinutes(30));
    }

    /** A draft started by Dr Okafor on the confirmed appointment. */
    UUID draft() {
        return controller.startNote(confirmed.getId(), okafor.getId());
    }

    /** A complete, finalized note: one valid diagnosis and a treatment plan. */
    UUID finalizedNote() {
        UUID id = draft();
        controller.addDiagnosis(id, okafor.getId(), "I10", "Essential hypertension");
        controller.recordTreatment(id, okafor.getId(), "Start amlodipine 5 mg daily; recheck in 4 weeks");
        controller.finalizeNote(id, okafor.getId());
        return id;
    }

    VisitNote note(UUID id) { return notes.findById(id).orElseThrow(); }

    long auditCount(UUID subject, AuditAction action) {
        return audit.findBySubject(subject).stream().filter(r -> r.action() == action).count();
    }
}
