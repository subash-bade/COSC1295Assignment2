package au.edu.rmit.cosc1295.carehome.repo;

import java.util.List;
import java.util.stream.Collectors;

import au.edu.rmit.cosc1295.carehome.model.Prescription;

public final class PrescriptionRepository extends InMemoryRepository<Prescription> {
    @Override protected String getId(Prescription p) { return p.getId(); }

    public List<Prescription> findByResident(String residentId) {
        return store.values().stream().filter(p -> p.getResidentId().equals(residentId))
                .collect(Collectors.toList());
    }
}
