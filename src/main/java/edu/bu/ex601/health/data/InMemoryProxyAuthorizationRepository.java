package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.ProxyAuthorization;
import edu.bu.ex601.health.domain.ProxyAuthorizationRepository;
import java.util.*;

public class InMemoryProxyAuthorizationRepository implements ProxyAuthorizationRepository {
    private final Map<UUID, ProxyAuthorization> store = new LinkedHashMap<>();

    @Override public synchronized List<ProxyAuthorization> findByPatient(UUID patientId) {
        return store.values().stream().filter(a -> a.getPatient().getId().equals(patientId)).toList();
    }
    @Override public synchronized void save(ProxyAuthorization authorization) { store.put(authorization.getId(), authorization); }
}
