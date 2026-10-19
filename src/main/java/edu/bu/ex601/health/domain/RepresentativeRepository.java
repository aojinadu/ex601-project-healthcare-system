package edu.bu.ex601.health.domain;

import java.util.Optional;
import java.util.UUID;

public interface RepresentativeRepository {
    Optional<Representative> findById(UUID id);
    void save(Representative representative);
}
