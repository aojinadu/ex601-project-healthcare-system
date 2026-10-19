package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.*;
import java.util.*;

public class InMemoryClinicianRepository implements ClinicianRepository {
    private final Map<UUID, Clinician> store = new LinkedHashMap<>();

    @Override public synchronized Optional<Clinician> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
    @Override public synchronized void save(Clinician item) { store.put(item.getId(), item); }

}
