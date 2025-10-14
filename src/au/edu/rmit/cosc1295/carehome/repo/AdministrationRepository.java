package au.edu.rmit.cosc1295.carehome.repo;

import au.edu.rmit.cosc1295.carehome.model.AdministrationRecord;

public final class AdministrationRepository extends InMemoryRepository<AdministrationRecord> {
    @Override protected String getId(AdministrationRecord a) { return a.getId(); }
}
