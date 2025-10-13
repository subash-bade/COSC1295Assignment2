package au.edu.rmit.cosc1295.carehome.repo;

import java.util.Optional;
import au.edu.rmit.cosc1295.carehome.model.Bed;

public final class BedRepository extends InMemoryRepository<Bed> {
    @Override protected String getId(Bed entity) { return entity.getId(); }

    public Optional<Bed> findByResidentId(String residentId) {
        return store.values().stream()
            .filter(b -> b.getResidentId().orElse(null) != null && b.getResidentId().get().equals(residentId))
            .findFirst();
    }
}