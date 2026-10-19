package edu.bu.ex601.health.presentation;

import static org.junit.jupiter.api.Assertions.*;

import edu.bu.ex601.health.data.*;
import edu.bu.ex601.health.domain.*;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookAppointmentControllerTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC);
    private InMemoryProxyAuthorizationRepository authorizations;
    private InMemoryAuditLog auditLog;
    private BookAppointmentController booking;
    private AvailabilityController availability;
    private Patient patient;
    private Representative representative;
    private Clinician clinician;

    @BeforeEach
    void setUp() {
        var patients = new InMemoryPatientRepository();
        var representatives = new InMemoryRepresentativeRepository();
        var clinicians = new InMemoryClinicianRepository();
        var slots = new InMemorySlotRepository();
        authorizations = new InMemoryProxyAuthorizationRepository();
        auditLog = new InMemoryAuditLog();
        booking = new BookAppointmentController(patients, representatives, authorizations, slots,
                new InMemoryAppointmentRepository(), auditLog, clock);
        availability = new AvailabilityController(clinicians, slots);

        patient = new Patient(UUID.randomUUID(), "Margaret", LocalDate.of(1948, 3, 2));
        representative = new Representative(UUID.randomUUID(), "Daniel");
        clinician = new Clinician(UUID.randomUUID(), "Dr. Okafor", "NB-1");
        patients.save(patient);
        representatives.save(representative);
        clinicians.save(clinician);
    }

    @Test
    void unauthorizedRepresentativeIsRejectedAndSlotStaysOpen() {
        UUID slotId = availability.publishSlot(clinician.getId(), LocalDateTime.of(2026, 10, 20, 9, 0), 30);
        assertThrows(DomainException.class,
                () -> booking.requestOnBehalf(representative.getId(), patient.getId(), slotId));
        assertEquals(1, availability.listOpenSlots(clinician.getId(), LocalDate.of(2026, 10, 20)).size());
    }

    @Test
    void authorizedRequestIsAuditedAndAppearsAsPending() {
        authorizations.save(new ProxyAuthorization(UUID.randomUUID(), patient, representative,
                Instant.parse("2026-10-01T00:00:00Z")));
        UUID slotId = availability.publishSlot(clinician.getId(), LocalDateTime.of(2026, 10, 20, 9, 0), 30);

        UUID appointmentId = booking.requestOnBehalf(representative.getId(), patient.getId(), slotId);

        assertEquals(1, booking.pendingRequests(patient.getId()).size());
        assertEquals(AuditAction.APPOINTMENT_REQUESTED_BY_PROXY,
                auditLog.findBySubject(appointmentId).get(0).action());
    }

    @Test
    void overlappingSlotIsRejected() {
        availability.publishSlot(clinician.getId(), LocalDateTime.of(2026, 10, 20, 9, 0), 30);
        assertThrows(DomainException.class,
                () -> availability.publishSlot(clinician.getId(), LocalDateTime.of(2026, 10, 20, 9, 15), 30));
    }
}
