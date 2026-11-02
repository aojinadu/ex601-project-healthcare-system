package edu.bu.ex601.health.presentation;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.ex601.health.domain.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A2 Part 2, Behavior A: finalize a visit note.  Basis: R2, R3, R5, I1, I2, I3, I4.
 * Expected values were written from the test basis before the code was run.
 */
@DisplayName("Behavior A - finalize a visit note (black-box)")
class FinalizeVisitNoteBlackBoxTest {
    private VisitNoteFixture f;
    private UUID id;

    @BeforeEach
    void setUp() {
        f = new VisitNoteFixture();
        id = f.draft();
    }

    private List<String> finalizeWith(String treatment, String... codes) {
        if (treatment != null) f.controller.recordTreatment(id, f.okafor.getId(), treatment);
        for (String c : codes) f.controller.addDiagnosis(id, f.okafor.getId(), c, "desc");
        return f.controller.finalizeNote(id, f.okafor.getId());
    }

    private void assertStillDraft() {
        assertEquals(NoteStatus.DRAFT, f.note(id).getStatus());
        assertNull(f.note(id).getFinalizedAt());
        assertEquals(0, f.auditCount(id, AuditAction.NOTE_FINALIZED));
    }

    // ---------- Partitions ----------

    @Test @DisplayName("A-P1 treatment + one valid code, conducting clinician -> [] , FINAL, one NOTE_FINALIZED")
    void p1_completeNote() {
        List<String> errors = finalizeWith("Rest and fluids", "J06.9");
        assertTrue(errors.isEmpty());
        assertEquals(NoteStatus.FINAL, f.note(id).getStatus());
        assertEquals(VisitNoteFixture.NOW, f.note(id).getFinalizedAt());
        assertEquals(1, f.auditCount(id, AuditAction.NOTE_FINALIZED));
        assertEquals(f.okafor.getId(), f.audit.findBySubject(id).get(1).actorId());
    }

    @Test @DisplayName("A-P2 invalid: no treatment, no diagnosis -> 2 errors, still DRAFT")
    void p2_empty() {
        assertEquals(2, finalizeWith(null).size());
        assertStillDraft();
    }

    @Test @DisplayName("A-P3 invalid: whitespace-only treatment -> 1 error, still DRAFT")
    void p3_blankTreatment() {
        assertEquals(1, finalizeWith("   ", "J06.9").size());
        assertStillDraft();
    }

    @Test @DisplayName("A-P4 invalid: malformed code 'hypertension' -> 1 error naming the code")
    void p4_badCode() {
        List<String> errors = finalizeWith("Rest", "hypertension");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("hypertension"));
        assertStillDraft();
    }

    @Test @DisplayName("A-P5 invalid: Dr Chen (not conducting) finalizes a complete note -> rejected, still DRAFT")
    void p5_otherClinicianCompleteNote() {
        f.controller.recordTreatment(id, f.okafor.getId(), "Rest");
        f.controller.addDiagnosis(id, f.okafor.getId(), "J06.9", "URI");
        assertThrows(DomainException.class, () -> f.controller.finalizeNote(id, f.chen.getId()));
        assertStillDraft();
    }

    @Test @DisplayName("A-P6 invalid: Dr Chen finalizes an INCOMPLETE note -> rejected for authorization (I1)")
    void p6_otherClinicianIncompleteNote() {
        assertThrows(DomainException.class, () -> f.controller.finalizeNote(id, f.chen.getId()));
        assertStillDraft();
    }

    @Test @DisplayName("A-P7 invalid: note already FINAL -> rejected, finalizedAt and audit unchanged (I3)")
    void p7_alreadyFinal() {
        UUID done = f.finalizedNote();
        assertThrows(DomainException.class, () -> f.controller.finalizeNote(done, f.okafor.getId()));
        assertEquals(1, f.auditCount(done, AuditAction.NOTE_FINALIZED));
    }

    @Test @DisplayName("A-P8 invalid: unknown note id -> DomainException")
    void p8_unknownNote() {
        assertThrows(DomainException.class, () -> f.controller.finalizeNote(UUID.randomUUID(), f.okafor.getId()));
    }

    // ---------- Boundaries: number of diagnoses (at least 1) ----------

    @Test @DisplayName("A-B1 0 diagnoses -> 1 error")
    void b1_zeroDiagnoses() {
        assertEquals(1, finalizeWith("Rest").size());
        assertStillDraft();
    }

    @Test @DisplayName("A-B2 1 diagnosis -> []")
    void b2_oneDiagnosis() {
        assertTrue(finalizeWith("Rest", "J06.9").isEmpty());
    }

    @Test @DisplayName("A-B3 2 diagnoses -> []")
    void b3_twoDiagnoses() {
        assertTrue(finalizeWith("Rest", "J06.9", "I10").isEmpty());
        assertEquals(2, f.note(id).getDiagnoses().size());
    }

    // ---------- Boundaries: code category = letter + 2 characters ----------

    @Test @DisplayName("A-B4 category of 2 chars 'J4' -> 1 error")
    void b4_categoryTooShort() { assertEquals(1, finalizeWith("Rest", "J4").size()); }

    @Test @DisplayName("A-B5 category of 3 chars 'J45' -> []")
    void b5_categoryExact() { assertTrue(finalizeWith("Rest", "J45").isEmpty()); }

    @Test @DisplayName("A-B6 category of 4 chars 'J456' (no dot) -> 1 error")
    void b6_categoryTooLong() { assertEquals(1, finalizeWith("Rest", "J456").size()); }

    // ---------- Boundaries: subcategory after the dot = 1 to 4 characters ----------

    @Test @DisplayName("A-B7 dot with 0 chars 'J45.' -> 1 error")
    void b7_emptySuffix() { assertEquals(1, finalizeWith("Rest", "J45.").size()); }

    @Test @DisplayName("A-B8 1 char 'J45.9' -> []")
    void b8_suffix1() { assertTrue(finalizeWith("Rest", "J45.9").isEmpty()); }

    @Test @DisplayName("A-B9 4 chars 'J45.9091' -> []")
    void b9_suffix4() { assertTrue(finalizeWith("Rest", "J45.9091").isEmpty()); }

    @Test @DisplayName("A-B10 5 chars 'J45.90912' -> 1 error")
    void b10_suffix5() { assertEquals(1, finalizeWith("Rest", "J45.90912").size()); }

    @Test @DisplayName("A-B11 lowercase 'j45.9' -> 1 error (codes are upper case)")
    void b11_lowercase() { assertEquals(1, finalizeWith("Rest", "j45.9").size()); }

    @Test @DisplayName("A-B12 letter in 3rd position 'M1A.0' (real ICD-10-CM code) -> []")
    void b12_letterInThirdPosition() {
        assertTrue(finalizeWith("Allopurinol 100 mg daily", "M1A.0").isEmpty());
        assertEquals(NoteStatus.FINAL, f.note(id).getStatus());
    }
}
