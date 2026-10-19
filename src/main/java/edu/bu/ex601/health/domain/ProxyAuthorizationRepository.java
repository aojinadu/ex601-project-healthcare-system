package edu.bu.ex601.health.domain;

import java.util.List;
import java.util.UUID;

public interface ProxyAuthorizationRepository {
    List<ProxyAuthorization> findByPatient(UUID patientId);
    void save(ProxyAuthorization authorization);
}
