package edu.bu.ex601.health.presentation;

import edu.bu.ex601.health.domain.*;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Use case: book for self, request on behalf of a patient, approve or decline a request. */
public class BookAppointmentController {
    private final PatientRepository patients;
    private final RepresentativeRepository representatives;
    private final ProxyAuthorizationRepository authorizations;
    private final SlotRepository slots;
    private final AppointmentRepository appointments;
    private final AuditLog auditLog;
    private final Clock clock;

    public BookAppointmentController(PatientRepository patients, RepresentativeRepository representatives,
                                     ProxyAuthorizationRepository authorizations, SlotRepository slots,
                                     AppointmentRepository appointments, AuditLog auditLog, Clock clock) {
        this.patients = patients;
        this.representatives = representatives;
        this.authorizations = authorizations;
        this.slots = slots;
        this.appointments = appointments;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    public UUID bookForSelf(UUID patientId, UUID slotId) {
        Patient patient = patient(patientId);
        AvailabilitySlot slot = slot(slotId);
        Appointment appointment = slot.bookFor(patient);
        save(slot, appointment);
        audit(patientId, appointment.getId(), AuditAction.APPOINTMENT_BOOKED);
        return appointment.getId();
    }

    public UUID requestOnBehalf(UUID representativeId, UUID patientId, UUID slotId) {
        Representative representative = representatives.findById(representativeId)
                .orElseThrow(() -> new DomainException("Unknown representative"));
        Patient patient = patient(patientId);
        Instant now = clock.instant();
        boolean authorized = authorizations.findByPatient(patientId).stream()
                .anyMatch(a -> a.permits(representative, patient, now));
        if (!authorized) {
            throw new DomainException("Representative is not authorized for this patient");
        }
        AvailabilitySlot slot = slot(slotId);
        Appointment appointment = slot.holdFor(patient, representative);
        save(slot, appointment);
        audit(representativeId, appointment.getId(), AuditAction.APPOINTMENT_REQUESTED_BY_PROXY);
        return appointment.getId();
    }

    public void approveRequest(UUID patientId, UUID appointmentId) {
        Appointment appointment = appointment(appointmentId);
        appointment.approve(patient(patientId));
        save(appointment.getSlot(), appointment);
        audit(patientId, appointmentId, AuditAction.APPOINTMENT_APPROVED);
    }

    public void declineRequest(UUID patientId, UUID appointmentId) {
        Appointment appointment = appointment(appointmentId);
        appointment.decline(patient(patientId));
        save(appointment.getSlot(), appointment);
        audit(patientId, appointmentId, AuditAction.APPOINTMENT_DECLINED);
    }

    public List<Appointment> pendingRequests(UUID patientId) {
        return appointments.findPendingForPatient(patientId);
    }

    private void save(AvailabilitySlot slot, Appointment appointment) {
        slots.save(slot);
        appointments.save(appointment);
    }

    private void audit(UUID actorId, UUID subjectId, AuditAction action) {
        auditLog.record(new AuditRecord(actorId, subjectId, action, clock.instant()));
    }

    private Patient patient(UUID id) {
        return patients.findById(id).orElseThrow(() -> new DomainException("Unknown patient"));
    }

    private AvailabilitySlot slot(UUID id) {
        return slots.findById(id).orElseThrow(() -> new DomainException("Unknown slot"));
    }

    private Appointment appointment(UUID id) {
        return appointments.findById(id).orElseThrow(() -> new DomainException("Unknown appointment"));
    }
}
