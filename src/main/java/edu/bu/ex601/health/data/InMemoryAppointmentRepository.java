package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.*;
import java.util.*;

public class InMemoryAppointmentRepository implements AppointmentRepository {
    private final Map<UUID, Appointment> store = new LinkedHashMap<>();

    @Override public synchronized Optional<Appointment> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
    @Override public synchronized void save(Appointment item) { store.put(item.getId(), item); }

    @Override public synchronized List<Appointment> findPendingForPatient(UUID patientId) {
        return store.values().stream()
                .filter(a -> a.getPatient().getId().equals(patientId))
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING_APPROVAL)
                .toList();
    }
}
