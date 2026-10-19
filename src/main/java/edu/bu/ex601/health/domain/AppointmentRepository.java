package edu.bu.ex601.health.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {
    Optional<Appointment> findById(UUID id);
    List<Appointment> findPendingForPatient(UUID patientId);
    void save(Appointment appointment);
}
