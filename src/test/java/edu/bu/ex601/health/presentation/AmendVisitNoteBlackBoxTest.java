package edu.bu.ex601.health.presentation;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.ex601.health.domain.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A2 Part 2, Behavior B: amend a finalized note.  Basis: R4, R5, I1, I3, I4.
 */
@DisplayName("Behavior B - amend a finalized note (black-box)")
class AmendVisitNoteBlackBoxTest {
    private VisitNoteFixture f;
    private UUID original;

    @BeforeEach
    void setUp() {
        f = new VisitNoteFixture();
        original = f.finalizedNote();      // FINAL, 1 diagnosis (I10), treatment recorded
    }

    private void assertOriginalUntouched() {
        VisitNote o = f.note(original);
        assertEquals(NoteStatus.FINAL, o.getStatus());
        assertEquals("Start amlodipine 5 mg daily; recheck in 4 weeks", o.getTreatment());
        assertEquals(1, o.getDiagnoses().size());
        assertEquals("I10", o.getDiagnoses().get(0).getCode());
        assertEquals(VisitNoteFixture.NOW, o.getFinalizedAt());
    }

    // ---------- Partitions ----------

    @Test @DisplayName("B-P1 conducting clinician amends FINAL note -> new DRAFT that references it, original untouched, NOTE_AMENDED audited")
    void p1_validAmendment() {
        UUID amendmentId = f.controller.amendNote(original, f.okafor.getId());
        VisitNote a = f.note(amendmentId);
        assertNotEquals(original, amendmentId);
        assertEquals(NoteStatus.DRAFT, a.getStatus());                  // I3
        assertSame(f.note(original), a.getAmends());                    // I3
        assertEquals(f.okafor, a.getAuthor());
        assertSame(f.confirmed, a.getAppointment());
        assertOriginalUntouched();                                      // I3
        assertEquals(1, f.auditCount(amendmentId, AuditAction.NOTE_AMENDED));   // I4
    }

    @Test @DisplayName("B-P2 invalid: amend a DRAFT -> DomainException, nothing saved or audited")
    void p2_draft() {
        UUID draft = f.controller.startNote(f.appointments.findById(f.confirmed.getId()).get().getId(), f.okafor.getId());
        int before = f.audit.findBySubject(draft).size();
        assertThrows(DomainException.class, () -> f.controller.amendNote(draft, f.okafor.getId()));
        assertEquals(before, f.audit.findBySubject(draft).size());
    }

    @Test @DisplayName("B-P3 invalid: Dr Chen amends -> DomainException, original untouched (I1)")
    void p3_otherClinician() {
        assertThrows(DomainException.class, () -> f.controller.amendNote(original, f.chen.getId()));
        assertOriginalUntouched();
    }

    @Test @DisplayName("B-P4 invalid: unknown clinician id -> DomainException")
    void p4_unknownClinician() {
        assertThrows(DomainException.class, () -> f.controller.amendNote(original, UUID.randomUUID()));
        assertOriginalUntouched();
    }

    @Test @DisplayName("B-P5 invalid: edit the original after amending -> DomainException (I3)")
    void p5_editOriginalAfterAmend() {
        f.controller.amendNote(original, f.okafor.getId());
        assertThrows(DomainException.class,
                () -> f.controller.recordTreatment(original, f.okafor.getId(), "Changed"));
        assertThrows(DomainException.class,
                () -> f.controller.addDiagnosis(original, f.okafor.getId(), "E11.9", "T2DM"));
        assertOriginalUntouched();
    }

    @Test @DisplayName("B-P6 the amendment follows R2/R3 like any draft: edit, finalize, audited")
    void p6_amendmentLifecycle() {
        UUID a = f.controller.amendNote(original, f.okafor.getId());
        f.controller.addDiagnosis(a, f.okafor.getId(), "I10", "Essential hypertension");
        f.controller.recordTreatment(a, f.okafor.getId(), "Amlodipine 10 mg daily");
        assertTrue(f.controller.finalizeNote(a, f.okafor.getId()).isEmpty());
        assertEquals(NoteStatus.FINAL, f.note(a).getStatus());
        assertEquals(1, f.auditCount(a, AuditAction.NOTE_FINALIZED));
        assertOriginalUntouched();
    }

    // ---------- Boundaries: number of amendments (A1 model: a note amends 0..1 notes; a note may be amended many times) ----------

    @Test @DisplayName("B-B1 0 amendments -> original has no correction, amends() is null")
    void b1_noAmendment() {
        assertNull(f.note(original).getAmends());
        assertEquals(0, f.auditCount(original, AuditAction.NOTE_AMENDED));
    }

    @Test @DisplayName("B-B2 chain of 2: amend, finalize the amendment, amend it again -> allowed, links in order")
    void b2_chainOfTwo() {
        UUID a1 = f.controller.amendNote(original, f.okafor.getId());
        f.controller.addDiagnosis(a1, f.okafor.getId(), "I10", "HTN");
        f.controller.recordTreatment(a1, f.okafor.getId(), "Amlodipine 10 mg");
        f.controller.finalizeNote(a1, f.okafor.getId());
        UUID a2 = f.controller.amendNote(a1, f.okafor.getId());
        assertSame(f.note(a1), f.note(a2).getAmends());
        assertSame(f.note(original), f.note(a1).getAmends());
        assertOriginalUntouched();
    }

    @Test @DisplayName("B-B3 a second amendment of the SAME note -> allowed, both reference the original (A1: VisitNote * -> 0..1 amends)")
    void b3_secondAmendmentOfSameNote() {
        UUID a1 = f.controller.amendNote(original, f.okafor.getId());
        UUID a2 = f.controller.amendNote(original, f.okafor.getId());
        assertNotEquals(a1, a2);
        assertSame(f.note(original), f.note(a1).getAmends());
        assertSame(f.note(original), f.note(a2).getAmends());
        assertEquals(1, f.auditCount(a1, AuditAction.NOTE_AMENDED));
        assertEquals(1, f.auditCount(a2, AuditAction.NOTE_AMENDED));
        assertOriginalUntouched();
    }
}
