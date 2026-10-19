package edu.bu.ex601.health.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** A clinician's note for one appointment. Append-only once finalized (see ADR-002). */
public class VisitNote {
    private final UUID id;
    private final Appointment appointment;
    private final Clinician author;
    private final VisitNote amends;
    private final List<Diagnosis> diagnoses = new ArrayList<>();
    private String treatment = "";
    private NoteStatus status = NoteStatus.DRAFT;
    private Instant finalizedAt;

    VisitNote(UUID id, Appointment appointment, Clinician author, VisitNote amends) {
        this.id = Objects.requireNonNull(id);
        this.appointment = Objects.requireNonNull(appointment);
        this.author = Objects.requireNonNull(author);
        this.amends = amends;
    }

    public Diagnosis addDiagnosis(Clinician actor, String code, String description) {
        requireEditableBy(actor);
        Diagnosis diagnosis = new Diagnosis(code, description);
        diagnoses.add(diagnosis);
        return diagnosis;
    }

    public void recordTreatment(Clinician actor, String details) {
        requireEditableBy(actor);
        this.treatment = Objects.requireNonNull(details);
    }

    /** Returns the list of rule violations; empty means the note may be finalized. */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (treatment.isBlank()) {
            errors.add("Treatment details are required");
        }
        if (diagnoses.isEmpty()) {
            errors.add("At least one diagnosis is required");
        }
        for (Diagnosis d : diagnoses) {
            if (!d.hasValidCodeFormat()) {
                errors.add("Diagnosis code has an invalid format: " + d.getCode());
            }
        }
        return errors;
    }

    public void finalizeNote(Clinician actor, Instant at) {
        requireEditableBy(actor);
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw new DomainException(String.join("; ", errors));
        }
        this.status = NoteStatus.FINAL;
        this.finalizedAt = Objects.requireNonNull(at);
    }

    /** Creates a new draft that references this note. This note is never modified. */
    public VisitNote amend(Clinician actor) {
        requireConductingClinician(actor);
        if (status != NoteStatus.FINAL) {
            throw new DomainException("Only a finalized note can be amended");
        }
        return new VisitNote(UUID.randomUUID(), appointment, actor, this);
    }

    public boolean isFinalized() {
        return status == NoteStatus.FINAL;
    }

    private void requireEditableBy(Clinician actor) {
        requireConductingClinician(actor);
        if (status == NoteStatus.FINAL) {
            throw new DomainException("A finalized note cannot be changed; amend it instead");
        }
    }

    private void requireConductingClinician(Clinician actor) {
        if (!appointment.isConductedBy(actor)) {
            throw new DomainException("Only the conducting clinician may change this note");
        }
    }

    public UUID getId() { return id; }
    public Appointment getAppointment() { return appointment; }
    public Clinician getAuthor() { return author; }
    public VisitNote getAmends() { return amends; }
    public List<Diagnosis> getDiagnoses() { return Collections.unmodifiableList(diagnoses); }
    public String getTreatment() { return treatment; }
    public NoteStatus getStatus() { return status; }
    public Instant getFinalizedAt() { return finalizedAt; }
}
