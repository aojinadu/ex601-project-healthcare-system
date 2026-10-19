package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.*;
import java.util.*;

public class InMemorySlotRepository implements SlotRepository {
    private final Map<UUID, AvailabilitySlot> store = new LinkedHashMap<>();

    @Override public synchronized Optional<AvailabilitySlot> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
    @Override public synchronized void save(AvailabilitySlot item) { store.put(item.getId(), item); }

    @Override public synchronized List<AvailabilitySlot> findByClinician(UUID clinicianId) {
        return store.values().stream().filter(s -> s.getClinician().getId().equals(clinicianId)).toList();
    }
}
