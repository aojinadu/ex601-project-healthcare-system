package edu.bu.ex601.health.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingTest {
    private Clinician clinician;
    private Patient patient;
    private Representative representative;
    private AvailabilitySlot slot;

    @BeforeEach
    void setUp() {
        clinician = new Clinician(UUID.randomUUID(), "Dr. A", "L-1");
        patient = new Patient(UUID.randomUUID(), "P", LocalDate.of(1950, 1, 1));
        representative = new Representative(UUID.randomUUID(), "R");
        slot = new AvailabilitySlot(UUID.randomUUID(), clinician,
                LocalDateTime.of(2026, 11, 2, 9, 0), LocalDateTime.of(2026, 11, 2, 9, 30));
    }

    @Test
    void slotCannotBeBookedTwice() {
        slot.bookFor(patient);
        Patient second = new Patient(UUID.randomUUID(), "Q", LocalDate.of(1980, 1, 1));
        assertThrows(DomainException.class, () -> slot.bookFor(second));
    }

    @Test
    void proxyRequestHoldsSlotUntilPatientApproves() {
        Appointment request = slot.holdFor(patient, representative);
        assertEquals(SlotStatus.HELD, slot.getStatus());
        assertEquals(AppointmentStatus.PENDING_APPROVAL, request.getStatus());

        request.approve(patient);

        assertEquals(SlotStatus.BOOKED, slot.getStatus());
        assertEquals(AppointmentStatus.CONFIRMED, request.getStatus());
    }

    @Test
    void declinedRequestReopensSlot() {
        Appointment request = slot.holdFor(patient, representative);
        request.decline(patient);
        assertTrue(slot.isOpen());
        assertEquals(AppointmentStatus.DECLINED, request.getStatus());
    }

    @Test
    void onlyThePatientCanApprove() {
        Appointment request = slot.holdFor(patient, representative);
        Patient someoneElse = new Patient(UUID.randomUUID(), "X", LocalDate.of(1970, 1, 1));
        assertThrows(DomainException.class, () -> request.approve(someoneElse));
    }

    @Test
    void revokedAuthorizationNoLongerPermits() {
        Instant granted = Instant.parse("2026-10-01T00:00:00Z");
        ProxyAuthorization auth = new ProxyAuthorization(UUID.randomUUID(), patient, representative, granted);
        assertTrue(auth.permits(representative, patient, granted.plusSeconds(60)));
        auth.revoke(granted.plusSeconds(120));
        assertFalse(auth.permits(representative, patient, granted.plusSeconds(180)));
    }

    @Test
    void overlappingSlotsForSameClinicianAreDetected() {
        AvailabilitySlot overlapping = new AvailabilitySlot(UUID.randomUUID(), clinician,
                LocalDateTime.of(2026, 11, 2, 9, 15), LocalDateTime.of(2026, 11, 2, 9, 45));
        assertTrue(slot.overlaps(overlapping));
    }
}
