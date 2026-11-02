package edu.bu.ex601.health.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A2 Part 3: white-box suite for VisitNote.validate().
 * CFG: N = 9, E = 12, C(G) = 5. One test per basis path, plus one loop test (two iterations).
 * Each test checks the returned list AND that the note's state did not change:
 * validate() is a query, so a side effect here would be a defect.
 */
@DisplayName("VisitNote.validate - branch coverage (white-box)")
class VisitNoteValidateBranchCoverageTest {
    private Clinician okafor;
    private VisitNote note;

    @BeforeEach
    void setUp() {
        okafor = new Clinician(UUID.randomUUID(), "Dr. Ada Okafor", "NB-12345");
        Patient p = new Patient(UUID.randomUUID(), "Margaret Doyle", LocalDate.of(1948, 3, 2));
        AvailabilitySlot slot = new AvailabilitySlot(UUID.randomUUID(), okafor,
                LocalDateTime.of(2026, 11, 2, 9, 0), LocalDateTime.of(2026, 11, 2, 9, 30));
        note = slot.bookFor(p).startVisitNote(okafor);
    }

    private void assertStateUnchanged(String treatment, int diagnoses) {
        assertEquals(NoteStatus.DRAFT, note.getStatus());
        assertNull(note.getFinalizedAt());
        assertEquals(treatment, note.getTreatment());
        assertEquals(diagnoses, note.getDiagnoses().size());
    }

    // covers Path 1: N1 -> N2(T) -> N3 -> N4(T) -> N5 -> N6(F) -> N9
    @Test @DisplayName("Path 1 - blank treatment, no diagnoses -> 2 errors")
    void path1_blankTreatment_noDiagnoses() {
        List<String> errors = note.validate();
        assertEquals(2, errors.size());
        assertTrue(errors.get(0).contains("Treatment"));
        assertTrue(errors.get(1).contains("diagnosis"));
        assertStateUnchanged("", 0);
    }

    // covers Path 2: N1 -> N2(F) -> N4(T) -> N5 -> N6(F) -> N9
    @Test @DisplayName("Path 2 - treatment, no diagnoses -> 1 error (diagnosis required)")
    void path2_treatment_noDiagnoses() {
        note.recordTreatment(okafor, "Rest");
        List<String> errors = note.validate();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("diagnosis"));
        assertStateUnchanged("Rest", 0);
    }

    // covers Path 3: N1 -> N2(F) -> N4(F) -> N6(T) -> N7(F) -> N6(F) -> N9
    @Test @DisplayName("Path 3 - treatment, one valid code -> no errors")
    void path3_treatment_validCode() {
        note.recordTreatment(okafor, "Rest");
        note.addDiagnosis(okafor, "J06.9", "URI");
        assertTrue(note.validate().isEmpty());
        assertStateUnchanged("Rest", 1);
    }

    // covers Path 4: N1 -> N2(F) -> N4(F) -> N6(T) -> N7(T) -> N8 -> N6(F) -> N9
    @Test @DisplayName("Path 4 - treatment, one malformed code -> 1 error naming it")
    void path4_treatment_badCode() {
        note.recordTreatment(okafor, "Rest");
        note.addDiagnosis(okafor, "hypertension", "free text");
        List<String> errors = note.validate();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("hypertension"));
        assertStateUnchanged("Rest", 1);
    }

    // covers Path 5: N1 -> N2(T) -> N3 -> N4(F) -> N6(T) -> N7(F) -> N6(F) -> N9
    @Test @DisplayName("Path 5 - blank treatment, one valid code -> 1 error (treatment required)")
    void path5_blankTreatment_validCode() {
        note.recordTreatment(okafor, "   ");
        note.addDiagnosis(okafor, "I10", "HTN");
        List<String> errors = note.validate();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("Treatment"));
        assertStateUnchanged("   ", 1);
    }

    // loop check (not a new basis path): N6 taken twice, N7 false then true
    @Test @DisplayName("Loop - valid then malformed code -> exactly 1 error, for the second code only")
    void loop_twoIterations() {
        note.recordTreatment(okafor, "Rest");
        note.addDiagnosis(okafor, "I10", "HTN");
        note.addDiagnosis(okafor, "E11-9", "typo in code");
        List<String> errors = note.validate();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("E11-9"));
        assertFalse(errors.get(0).contains("I10"));
        assertStateUnchanged("Rest", 2);
    }
}
