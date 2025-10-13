package au.edu.rmit.cosc1295.carehome.repo;

import au.edu.rmit.cosc1295.carehome.model.Resident;

public final class ResidentRepository extends InMemoryRepository<Resident> {
    @Override protected String getId(Resident entity) { return entity.getId(); }
}