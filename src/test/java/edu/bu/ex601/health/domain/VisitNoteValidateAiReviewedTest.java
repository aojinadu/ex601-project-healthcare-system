package edu.bu.ex601.health.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A2 Part 5: assistant-generated suite for VisitNote.validate(), kept after review.
 * Changes from the generated version:
 *  - STRENGTHENED invalidCodeDoesNotCountAsMissingDiagnosis: it only asserted that one message
 *    was absent, so it still passed when the code "123" was wrongly ACCEPTED (empty error list).
 *  - REJECTED violationOrderIsStable: it pins the order of messages, which no requirement states.
 *  - ADDED letterInThirdPositionIsAccepted: the generated suite never tried a real ICD-10-CM code
 *    with a letter in position 3 (e.g. M1A.0), so it passed against the D1 defect.
 * Note: several tests compare full message strings copied from VisitNote; they will need
 * updating if the wording changes, which is a known cost we accepted for readability.
 */
class VisitNoteValidateAiReviewedTest {

    private static final String TREATMENT_ERROR = "Treatment details are required";
    private static final String DIAGNOSIS_ERROR = "At least one diagnosis is required";
    private static final String FORMAT_ERROR_PREFIX = "Diagnosis code has an invalid format: ";

    private Clinician clinician;
    private Patient patient;
    private VisitNote note;

    @BeforeEach
    void setUp() {
        clinician = new Clinician(UUID.randomUUID(), "Dr. Jane Smith", "LIC-1001");
        patient = new Patient(UUID.randomUUID(), "John Doe", LocalDate.of(1990, 5, 17));
        LocalDateTime start = LocalDateTime.of(2026, 11, 2, 9, 0);
        AvailabilitySlot slot = new AvailabilitySlot(UUID.randomUUID(), clinician, start, start.plusMinutes(30));
        Appointment appointment = slot.bookFor(patient);
        note = appointment.startVisitNote(clinician);
    }

    private void addDx(String code) {
        note.addDiagnosis(clinician, code, "Description for " + code);
    }

    private void recordTreatment(String details) {
        note.recordTreatment(clinician, details);
    }

    @Test
    @DisplayName("Fresh note has two violations: missing treatment and missing diagnosis")
    void freshNoteReportsTreatmentAndDiagnosisViolations() {
        List<String> errors = note.validate();
        assertEquals(2, errors.size());
        assertEquals(TREATMENT_ERROR, errors.get(0));
        assertEquals(DIAGNOSIS_ERROR, errors.get(1));
    }

    @Test
    @DisplayName("Valid treatment and one valid diagnosis gives an empty list")
    void completeNoteHasNoViolations() {
        recordTreatment("Prescribed inhaler, follow up in two weeks");
        addDx("J45.909");
        List<String> errors = note.validate();
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Empty treatment string is reported when diagnosis is valid")
    void emptyTreatmentIsReported() {
        recordTreatment("");
        addDx("J45.909");
        assertEquals(List.of(TREATMENT_ERROR), note.validate());
    }

    @Test
    @DisplayName("Whitespace-only treatment is treated as blank")
    void whitespaceTreatmentIsBlank() {
        recordTreatment("   \t\n ");
        addDx("J45.909");
        assertEquals(List.of(TREATMENT_ERROR), note.validate());
    }

    @Test
    @DisplayName("Treatment with surrounding whitespace but real content is accepted")
    void paddedTreatmentIsAccepted() {
        recordTreatment("  rest and fluids  ");
        addDx("J45.909");
        assertTrue(note.validate().isEmpty());
    }

    @Test
    @DisplayName("Missing diagnosis is reported when treatment is present")
    void missingDiagnosisIsReported() {
        recordTreatment("Physiotherapy");
        assertEquals(List.of(DIAGNOSIS_ERROR), note.validate());
    }

    @Test
    @DisplayName("Invalid code format is reported with the offending code")
    void invalidCodeIsReportedWithCode() {
        recordTreatment("Physiotherapy");
        addDx("BAD");
        List<String> errors = note.validate();
        assertEquals(1, errors.size());
        assertEquals(FORMAT_ERROR_PREFIX + "BAD", errors.get(0));
    }

    @Test
    @DisplayName("Invalid code does not also trigger the missing-diagnosis violation")
    void invalidCodeDoesNotCountAsMissingDiagnosis() {
        recordTreatment("Physiotherapy");
        addDx("123");
        List<String> errors = note.validate();
        assertFalse(errors.contains(DIAGNOSIS_ERROR));
        assertEquals(1, errors.size());              // strengthened in review
        assertTrue(errors.get(0).contains("123"));
        assertFalse(note.isFinalized());
    }

    @Test
    @DisplayName("Every invalid code produces its own violation, valid ones are skipped")
    void eachInvalidCodeIsReportedOnce() {
        recordTreatment("Physiotherapy");
        addDx("J45.909");
        addDx("nope");
        addDx("E11.65");
        addDx("X1");
        List<String> errors = note.validate();
        assertEquals(2, errors.size());
        assertEquals(FORMAT_ERROR_PREFIX + "nope", errors.get(0));
        assertEquals(FORMAT_ERROR_PREFIX + "X1", errors.get(1));
    }

    @Test
    @DisplayName("Multiple valid diagnoses produce no violations")
    void multipleValidDiagnosesPass() {
        recordTreatment("Physiotherapy");
        addDx("J45.909");
        addDx("E11.65");
        addDx("I10");
        assertTrue(note.validate().isEmpty());
    }

    @Test
    @DisplayName("Blank treatment and invalid code are both reported, treatment first")
    void blankTreatmentAndInvalidCodeAreBothReported() {
        addDx("zzz");
        List<String> errors = note.validate();
        assertEquals(2, errors.size());
        assertEquals(TREATMENT_ERROR, errors.get(0));
        assertEquals(FORMAT_ERROR_PREFIX + "zzz", errors.get(1));
    }

    @Test
    @DisplayName("Accepted code formats: category only, and 1 to 4 character extensions")
    void validCodeFormatsAreAccepted() {
        recordTreatment("Physiotherapy");
        addDx("J45");
        addDx("J45.9");
        addDx("J45.90");
        addDx("J45.909");
        addDx("S72.0A1B".substring(0, 7));
        addDx("A00.ABCD");
        assertTrue(note.validate().isEmpty());
    }

    @Test
    @DisplayName("Lowercase letter in the category is rejected")
    void lowercaseCategoryRejected() {
        recordTreatment("Physiotherapy");
        addDx("j45.909");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "j45.909"), note.validate());
    }

    @Test
    @DisplayName("Lowercase letter in the extension is rejected")
    void lowercaseExtensionRejected() {
        recordTreatment("Physiotherapy");
        addDx("J45.9a");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "J45.9a"), note.validate());
    }

    @Test
    @DisplayName("Code without a leading letter is rejected")
    void missingLeadingLetterRejected() {
        recordTreatment("Physiotherapy");
        addDx("45.909");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "45.909"), note.validate());
    }

    @Test
    @DisplayName("Code with only one digit after the letter is rejected")
    void tooFewDigitsRejected() {
        recordTreatment("Physiotherapy");
        addDx("J4");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "J4"), note.validate());
    }

    @Test
    @DisplayName("Code with three digits after the letter is rejected")
    void tooManyCategoryDigitsRejected() {
        recordTreatment("Physiotherapy");
        addDx("J459");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "J459"), note.validate());
    }

    @Test
    @DisplayName("Missing dot before the extension is rejected")
    void missingDotRejected() {
        recordTreatment("Physiotherapy");
        addDx("J45909");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "J45909"), note.validate());
    }

    @Test
    @DisplayName("Dot with no extension is rejected")
    void trailingDotRejected() {
        recordTreatment("Physiotherapy");
        addDx("J45.");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "J45."), note.validate());
    }

    @Test
    @DisplayName("Extension longer than four characters is rejected")
    void tooLongExtensionRejected() {
        recordTreatment("Physiotherapy");
        addDx("J45.90909");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "J45.90909"), note.validate());
    }

    @Test
    @DisplayName("Empty code is rejected")
    void emptyCodeRejected() {
        recordTreatment("Physiotherapy");
        addDx("");
        assertEquals(List.of(FORMAT_ERROR_PREFIX), note.validate());
    }

    @Test
    @DisplayName("Code with leading or trailing whitespace is rejected")
    void paddedCodeRejected() {
        recordTreatment("Physiotherapy");
        addDx(" J45.909");
        addDx("J45.909 ");
        assertEquals(2, note.validate().size());
    }

    @Test
    @DisplayName("Code with a trailing newline is rejected")
    void trailingNewlineRejected() {
        recordTreatment("Physiotherapy");
        addDx("J45.909\n");
        assertEquals(1, note.validate().size());
    }

    @Test
    @DisplayName("Underscore or other symbols in the extension are rejected")
    void symbolsInExtensionRejected() {
        recordTreatment("Physiotherapy");
        addDx("J45.9_");
        addDx("J45-909");
        assertEquals(2, note.validate().size());
    }

    @Test
    @DisplayName("Letters in the two category digits are rejected")
    void lettersInCategoryDigitsRejected() {
        recordTreatment("Physiotherapy");
        addDx("JA5.909");
        assertEquals(List.of(FORMAT_ERROR_PREFIX + "JA5.909"), note.validate());
    }

    @Test
    @DisplayName("Letter in position 3 (real ICD-10-CM code M1A.0) is accepted")   // added in review (D1)
    void letterInThirdPositionIsAccepted() {
        recordTreatment("Allopurinol 100 mg daily");
        addDx("M1A.0");
        addDx("C4A.9");
        assertTrue(note.validate().isEmpty());
    }

    @Test
    @DisplayName("Calling validate repeatedly gives the same result and does not change the note")
    void validateIsRepeatableAndSideEffectFree() {
        addDx("bad");
        List<String> first = note.validate();
        List<String> second = note.validate();
        assertEquals(first, second);
        assertEquals(1, note.getDiagnoses().size());
        assertEquals("", note.getTreatment());
        assertFalse(note.isFinalized());
    }

    @Test
    @DisplayName("Mutating the returned list does not affect later validation")
    void returnedListIsIndependent() {
        recordTreatment("Physiotherapy");
        addDx("J45.909");
        List<String> errors = note.validate();
        errors.add("tampered");
        assertTrue(note.validate().isEmpty());
    }

    @Test
    @DisplayName("Fixing the violations makes a previously invalid note valid")
    void fixingViolationsClearsErrors() {
        assertEquals(2, note.validate().size());
        recordTreatment("Physiotherapy");
        assertEquals(List.of(DIAGNOSIS_ERROR), note.validate());
        addDx("J45.909");
        assertTrue(note.validate().isEmpty());
    }

    @Test
    @DisplayName("A valid note can be finalized and validate still returns empty afterwards")
    void validNoteFinalizesAndStaysValid() {
        recordTreatment("Physiotherapy");
        addDx("J45.909");
        assertDoesNotThrow(() -> note.finalizeNote(clinician, Instant.parse("2026-11-02T10:00:00Z")));
        assertTrue(note.isFinalized());
        assertTrue(note.validate().isEmpty());
    }
}
