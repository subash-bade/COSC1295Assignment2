package au.edu.rmit.cosc1295.carehome.repo;


import au.edu.rmit.cosc1295.carehome.model.Staff;

public final class StaffRepository extends InMemoryRepository<Staff> {
    @Override protected String getId(Staff entity) { return entity.getId(); }
}
