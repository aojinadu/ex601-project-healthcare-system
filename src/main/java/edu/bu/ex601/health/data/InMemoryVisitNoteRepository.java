package edu.bu.ex601.health.data;

import edu.bu.ex601.health.domain.*;
import java.util.*;

public class InMemoryVisitNoteRepository implements VisitNoteRepository {
    private final Map<UUID, VisitNote> store = new LinkedHashMap<>();

    @Override public synchronized Optional<VisitNote> findById(UUID id) { return Optional.ofNullable(store.get(id)); }
    @Override public synchronized void save(VisitNote item) { store.put(item.getId(), item); }

}
