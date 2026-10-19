package edu.bu.ex601.health.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlotRepository {
    Optional<AvailabilitySlot> findById(UUID id);
    List<AvailabilitySlot> findByClinician(UUID clinicianId);
    void save(AvailabilitySlot slot);
}
