package au.edu.rmit.cosc1295.carehome.repo;


import au.edu.rmit.cosc1295.carehome.model.Ward;

public final class WardRepository extends InMemoryRepository<Ward> {
    @Override protected String getId(Ward entity) { return entity.getId(); }
}