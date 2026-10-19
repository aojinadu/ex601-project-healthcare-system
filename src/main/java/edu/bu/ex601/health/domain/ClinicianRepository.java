package edu.bu.ex601.health.domain;

import java.util.Optional;
import java.util.UUID;

public interface ClinicianRepository {
    Optional<Clinician> findById(UUID id);
    void save(Clinician clinician);
}
