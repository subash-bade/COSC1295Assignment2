package au.edu.rmit.cosc1295.carehome.service;

import java.time.Instant;
import java.util.Objects;

import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.*;
import au.edu.rmit.cosc1295.carehome.model.AdministrationRecord;
import au.edu.rmit.cosc1295.carehome.model.Prescription;
import au.edu.rmit.cosc1295.carehome.repo.AdministrationRepository;
import au.edu.rmit.cosc1295.carehome.repo.PrescriptionRepository;
import au.edu.rmit.cosc1295.carehome.repo.ResidentRepository;
import au.edu.rmit.cosc1295.carehome.util.Ids;

public final class MedicationService {
    private final ResidentRepository residentRepo;
    private final PrescriptionRepository rxRepo;
    private final AdministrationRepository adminRepo;
    private final AuthService auth;

    public MedicationService(ResidentRepository residentRepo, PrescriptionRepository rxRepo,
                             AdministrationRepository adminRepo, AuthService auth) {
        this.residentRepo = Objects.requireNonNull(residentRepo);
        this.rxRepo = Objects.requireNonNull(rxRepo);
        this.adminRepo = Objects.requireNonNull(adminRepo);
        this.auth = Objects.requireNonNull(auth);
    }

    public String recordAdministration(String nurseId, String residentId, String medOrderId,
                                       Instant when, String doseGiven, String notes)
            throws NotAuthorizedException, NotRosteredException, ResidentNotFoundException, ValidationException {

        residentRepo.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found: " + residentId));

        auth.requireRole(nurseId, Role.NURSE);
        auth.requireRostered(nurseId, when);

        // Verify medOrder belongs to resident
        boolean found = false;
        for (Prescription p : rxRepo.findByResident(residentId)) {
            found = p.getOrders().stream().anyMatch(o -> o.getId().equals(medOrderId));
            if (found) break;
        }
        if (!found) throw new ValidationException("Medication order not found for resident");

        var rec = new AdministrationRecord(Ids.newId(), residentId, medOrderId, when, nurseId, doseGiven, notes);
        adminRepo.save(rec);
        return rec.getId();
    }
}