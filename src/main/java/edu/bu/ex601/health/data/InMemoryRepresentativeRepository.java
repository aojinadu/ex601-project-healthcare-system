package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.*;
import java.util.*;

public class InMemoryRepresentativeRepository implements RepresentativeRepository {
    private final Map<UUID, Representative> store = new LinkedHashMap<>();

    @Override public synchronized Optional<Representative> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
    @Override public synchronized void save(Representative item) { store.put(item.getId(), item); }

}
