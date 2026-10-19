package edu.bu.ex601.health.presentation;

import edu.bu.ex601.health.domain.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Use case: a clinician publishes availability; anyone browses open slots. */
public class AvailabilityController {
    private final ClinicianRepository clinicians;
    private final SlotRepository slots;

    public AvailabilityController(ClinicianRepository clinicians, SlotRepository slots) {
        this.clinicians = clinicians;
        this.slots = slots;
    }

    public UUID publishSlot(UUID clinicianId, LocalDateTime start, int minutes) {
        Clinician clinician = clinicians.findById(clinicianId)
                .orElseThrow(() -> new DomainException("Unknown clinician"));
        AvailabilitySlot slot = new AvailabilitySlot(UUID.randomUUID(), clinician, start, start.plusMinutes(minutes));
        for (AvailabilitySlot existing : slots.findByClinician(clinicianId)) {
            if (existing.overlaps(slot)) {
                throw new DomainException("Slot overlaps an existing slot");
            }
        }
        slots.save(slot);
        return slot.getId();
    }

    public List<AvailabilitySlot> listOpenSlots(UUID clinicianId, LocalDate day) {
        return slots.findByClinician(clinicianId).stream()
                .filter(AvailabilitySlot::isOpen)
                .filter(s -> s.getStart().toLocalDate().equals(day))
                .toList();
    }
}
