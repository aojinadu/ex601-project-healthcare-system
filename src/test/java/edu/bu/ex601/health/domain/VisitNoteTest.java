package edu.bu.ex601.health.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VisitNoteTest {
    private Clinician conducting;
    private Clinician other;
    private VisitNote note;

    @BeforeEach
    void setUp() {
        conducting = new Clinician(UUID.randomUUID(), "Dr. A", "L-1");
        other = new Clinician(UUID.randomUUID(), "Dr. B", "L-2");
        Patient patient = new Patient(UUID.randomUUID(), "P", LocalDate.of(1990, 1, 1));
        AvailabilitySlot slot = new AvailabilitySlot(UUID.randomUUID(), conducting,
                LocalDateTime.of(2026, 11, 2, 9, 0), LocalDateTime.of(2026, 11, 2, 9, 30));
        Appointment appointment = slot.bookFor(patient);
        note = appointment.startVisitNote(conducting);
    }

    @Test
    void cannotFinalizeWithoutTreatmentAndDiagnosis() {
        assertEquals(2, note.validate().size());
        assertThrows(DomainException.class, () -> note.finalizeNote(conducting, Instant.now()));
    }

    @Test
    void rejectsBadlyFormattedDiagnosisCodeAtFinalize() {
        note.recordTreatment(conducting, "Rest");
        note.addDiagnosis(conducting, "hypertension", "free text");
        assertFalse(note.validate().isEmpty());
    }

    @Test
    void finalizedNoteCannotBeEdited() {
        note.recordTreatment(conducting, "Rest");
        note.addDiagnosis(conducting, "J06.9", "Upper respiratory infection");
        note.finalizeNote(conducting, Instant.now());
        assertThrows(DomainException.class, () -> note.recordTreatment(conducting, "Changed"));
    }

    @Test
    void amendmentIsNewDraftAndOriginalIsUntouched() {
        note.recordTreatment(conducting, "Rest");
        note.addDiagnosis(conducting, "J06.9", "Upper respiratory infection");
        note.finalizeNote(conducting, Instant.now());

        VisitNote amendment = note.amend(conducting);

        assertEquals(NoteStatus.DRAFT, amendment.getStatus());
        assertSame(note, amendment.getAmends());
        assertEquals("Rest", note.getTreatment());
        assertTrue(note.isFinalized());
    }

    @Test
    void onlyConductingClinicianMayEdit() {
        assertThrows(DomainException.class, () -> note.recordTreatment(other, "x"));
    }

    @Test
    void draftCannotBeAmended() {
        assertThrows(DomainException.class, () -> note.amend(conducting));
    }
}
