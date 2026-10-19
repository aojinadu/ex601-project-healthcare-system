package edu.bu.ex601.health.domain;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository {
    Optional<Patient> findById(UUID id);
    void save(Patient patient);
}
