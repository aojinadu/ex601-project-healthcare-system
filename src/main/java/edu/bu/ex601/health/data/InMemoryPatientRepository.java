package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.*;
import java.util.*;

public class InMemoryPatientRepository implements PatientRepository {
    private final Map<UUID, Patient> store = new LinkedHashMap<>();

    @Override public synchronized Optional<Patient> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
    @Override public synchronized void save(Patient item) { store.put(item.getId(), item); }

}
