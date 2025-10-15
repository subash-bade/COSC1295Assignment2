package au.edu.rmit.cosc1295.carehome.app;

import java.time.ZoneId;

import au.edu.rmit.cosc1295.carehome.repo.*;
import au.edu.rmit.cosc1295.carehome.service.*;

public final class Services {
    public final AuditService audit;
    public final RosterService roster;
    public final AuthService auth;
    public final BedService beds;
    public final PrescriptionService prescriptions;
    public final MedicationService medications;

    public Services(CareHomeState state) {
        AuditRepository auditRepo = state.audit;
        StaffRepository staffRepo = state.staff;
        RosterRepository rosterRepo = state.roster;
        ResidentRepository residentRepo = state.residents;
        BedRepository bedRepo = state.beds;
        PrescriptionRepository rxRepo = state.prescriptions;
        AdministrationRepository adminRepo = state.administrations;

        this.audit = new AuditService(auditRepo);
        this.roster = new RosterService(staffRepo, rosterRepo);
        this.auth = new AuthService(staffRepo, rosterRepo, ZoneId.systemDefault());
        this.beds = new BedService(residentRepo, bedRepo);
        this.prescriptions = new PrescriptionService(residentRepo, rxRepo, auth);
        this.medications = new MedicationService(residentRepo, rxRepo, adminRepo, auth);
    }
}