package edu.bu.ex601.health.presentation;

import edu.bu.ex601.health.domain.*;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

/** Use case: record and amend a visit note (Assignment 1 slice, Ayotola Jinadu). */
public class RecordVisitNoteController {
    private final AppointmentRepository appointments;
    private final ClinicianRepository clinicians;
    private final VisitNoteRepository notes;
    private final AuditLog auditLog;
    private final Clock clock;

    public RecordVisitNoteController(AppointmentRepository appointments, ClinicianRepository clinicians,
                                     VisitNoteRepository notes, AuditLog auditLog, Clock clock) {
        this.appointments = appointments;
        this.clinicians = clinicians;
        this.notes = notes;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    public UUID startNote(UUID appointmentId, UUID clinicianId) {
        Appointment appointment = appointments.findById(appointmentId)
                .orElseThrow(() -> new DomainException("Unknown appointment"));
        Clinician clinician = clinician(clinicianId);
        VisitNote note = appointment.startVisitNote(clinician);
        notes.save(note);
        audit(clinicianId, note.getId(), AuditAction.NOTE_CREATED);
        return note.getId();
    }

    public void addDiagnosis(UUID noteId, UUID clinicianId, String code, String description) {
        VisitNote note = note(noteId);
        note.addDiagnosis(clinician(clinicianId), code, description);
        notes.save(note);
    }

    public void recordTreatment(UUID noteId, UUID clinicianId, String details) {
        VisitNote note = note(noteId);
        note.recordTreatment(clinician(clinicianId), details);
        notes.save(note);
    }

    /** Returns validation errors; an empty list means the note is now final. */
    public List<String> finalizeNote(UUID noteId, UUID clinicianId) {
        VisitNote note = note(noteId);
        Clinician clinician = clinician(clinicianId);
        // A2 defect D2: authorize before validating, so a non-conducting clinician learns nothing
        if (!note.getAppointment().isConductedBy(clinician)) {
            throw new DomainException("Only the conducting clinician may change this note");
        }
        List<String> errors = note.validate();
        if (!errors.isEmpty()) {
            return errors;
        }
        note.finalizeNote(clinician, clock.instant());
        notes.save(note);
        audit(clinicianId, noteId, AuditAction.NOTE_FINALIZED);
        return List.of();
    }

    public UUID amendNote(UUID noteId, UUID clinicianId) {
        VisitNote original = note(noteId);
        VisitNote amendment = original.amend(clinician(clinicianId));
        notes.save(amendment);
        audit(clinicianId, amendment.getId(), AuditAction.NOTE_AMENDED);
        return amendment.getId();
    }

    private Clinician clinician(UUID id) {
        return clinicians.findById(id).orElseThrow(() -> new DomainException("Unknown clinician"));
    }

    private VisitNote note(UUID id) {
        return notes.findById(id).orElseThrow(() -> new DomainException("Unknown note"));
    }

    private void audit(UUID actorId, UUID subjectId, AuditAction action) {
        auditLog.record(new AuditRecord(actorId, subjectId, action, clock.instant()));
    }
}
