package edu.bu.ex601.health.presentation;

import edu.bu.ex601.health.data.*;
import edu.bu.ex601.health.domain.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Checkpoint 1 baseline: wires the three layers together and runs the MVP flow end to end.
 * The web UI replaces this entry point in Checkpoint 2.
 */
public final class ConsoleApp {

    public static void main(String[] args) {
        Clock clock = Clock.systemUTC();
        var patients = new InMemoryPatientRepository();
        var clinicians = new InMemoryClinicianRepository();
        var representatives = new InMemoryRepresentativeRepository();
        var authorizations = new InMemoryProxyAuthorizationRepository();
        var slots = new InMemorySlotRepository();
        var appointments = new InMemoryAppointmentRepository();
        var notes = new InMemoryVisitNoteRepository();
        var auditLog = new InMemoryAuditLog();

        var availability = new AvailabilityController(clinicians, slots);
        var booking = new BookAppointmentController(patients, representatives, authorizations,
                slots, appointments, auditLog, clock);
        var visitNotes = new RecordVisitNoteController(appointments, clinicians, notes, auditLog, clock);

        Patient margaret = new Patient(UUID.randomUUID(), "Margaret Doyle", LocalDate.of(1948, 3, 2));
        Representative daniel = new Representative(UUID.randomUUID(), "Daniel Doyle");
        Clinician drOkafor = new Clinician(UUID.randomUUID(), "Dr. Ada Okafor", "NB-12345");
        patients.save(margaret);
        representatives.save(daniel);
        clinicians.save(drOkafor);
        authorizations.save(new ProxyAuthorization(UUID.randomUUID(), margaret, daniel, clock.instant()));

        LocalDateTime tomorrow9 = LocalDate.now(clock).plusDays(1).atTime(9, 0);
        UUID slotId = availability.publishSlot(drOkafor.getId(), tomorrow9, 30);
        System.out.println("Published slot " + tomorrow9 + " for " + drOkafor.getFullName());

        UUID appointmentId = booking.requestOnBehalf(daniel.getId(), margaret.getId(), slotId);
        System.out.println(daniel.getFullName() + " requested it for " + margaret.getName() + " (pending approval)");

        booking.approveRequest(margaret.getId(), appointmentId);
        System.out.println(margaret.getName() + " approved; appointment confirmed");

        UUID noteId = visitNotes.startNote(appointmentId, drOkafor.getId());
        visitNotes.addDiagnosis(noteId, drOkafor.getId(), "I10", "Essential hypertension");
        visitNotes.recordTreatment(noteId, drOkafor.getId(), "Start amlodipine 5 mg daily; recheck in 4 weeks");
        System.out.println("Finalize errors: " + visitNotes.finalizeNote(noteId, drOkafor.getId()));

        UUID amendmentId = visitNotes.amendNote(noteId, drOkafor.getId());
        System.out.println("Amendment draft " + amendmentId + " created; original note unchanged");
        System.out.println("Audit entries for appointment: " + auditLog.findBySubject(appointmentId).size());
    }

    private ConsoleApp() { }
}
