package au.edu.rmit.cosc1295.carehome.service;

import java.time.Instant;
import java.util.Objects;

import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.NotAuthorizedException;
import au.edu.rmit.cosc1295.carehome.exceptions.NotRosteredException;
import au.edu.rmit.cosc1295.carehome.exceptions.ResidentNotFoundException;
import au.edu.rmit.cosc1295.carehome.exceptions.ValidationException;
import au.edu.rmit.cosc1295.carehome.model.MedicationOrder;
import au.edu.rmit.cosc1295.carehome.model.Prescription;
import au.edu.rmit.cosc1295.carehome.repo.PrescriptionRepository;
import au.edu.rmit.cosc1295.carehome.repo.ResidentRepository;
import au.edu.rmit.cosc1295.carehome.util.Ids;

public final class PrescriptionService {
    private final ResidentRepository residentRepo;
    private final PrescriptionRepository rxRepo;
    private final AuthService auth;

    public PrescriptionService(ResidentRepository residentRepo, PrescriptionRepository rxRepo, AuthService auth) {
        this.residentRepo = Objects.requireNonNull(residentRepo);
        this.rxRepo = Objects.requireNonNull(rxRepo);
        this.auth = Objects.requireNonNull(auth);
    }

    /** Add a prescription; doctor must be rostered at 'when'. Returns the new prescription ID. */
    public String addPrescription(String doctorId, String residentId, Prescription draft, Instant when)
            throws NotAuthorizedException, NotRosteredException, ResidentNotFoundException, ValidationException {

        residentRepo.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found: " + residentId));

        auth.requireRole(doctorId, Role.DOCTOR);
        auth.requireRostered(doctorId, when);

        if (draft == null) throw new ValidationException("Prescription draft is null");
        if (draft.getOrders().isEmpty()) throw new ValidationException("Prescription has no orders");

        // assign IDs if missing
        if (draft.getId() == null) draft.setId(Ids.newId());
        for (MedicationOrder mo : draft.getOrders()) {
            if (mo.getId() == null) mo.setId(Ids.newId());
        }

        rxRepo.save(draft);
        return draft.getId();
    }
}