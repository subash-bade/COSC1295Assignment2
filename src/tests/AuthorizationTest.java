package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.edu.rmit.cosc1295.carehome.exceptions.*;
import au.edu.rmit.cosc1295.carehome.model.*;
import au.edu.rmit.cosc1295.carehome.repo.*;
import au.edu.rmit.cosc1295.carehome.service.*;
import au.edu.rmit.cosc1295.carehome.util.Ids;

public class AuthorizationTest {

    StaffRepository staffRepo;
    ResidentRepository residentRepo;
    PrescriptionRepository rxRepo;
    AdministrationRepository adminRepo;
    RosterRepository rosterRepo;

    AuthService auth;
    RosterService roster;
    PrescriptionService rxService;
    MedicationService medService;

    Doctor doc;
    Nurse nurse;
    Resident alice;

    final ZoneId zone = ZoneId.systemDefault();

    @BeforeEach
    void setup() throws Exception {
        staffRepo = new StaffRepository();
        residentRepo = new ResidentRepository();
        rxRepo = new PrescriptionRepository();
        adminRepo = new AdministrationRepository();
        rosterRepo = new RosterRepository();

        auth = new AuthService(staffRepo, rosterRepo, zone);
        roster = new RosterService(staffRepo, rosterRepo);
        rxService = new PrescriptionService(residentRepo, rxRepo, auth);
        medService = new MedicationService(residentRepo, rxRepo, adminRepo, auth);

        doc = new Doctor("D1", "Dr. Dee", "ddee");
        nurse = new Nurse("N1", "Nurse Neil", "nneil");
        staffRepo.save(doc);
        staffRepo.save(nurse);

        alice = new Resident("R1", "Alice", Gender.FEMALE, LocalDate.of(1950,1,1));
        residentRepo.save(alice);

        // By default, give doctor 1h coverage each day at 10:00–11:00
        for (DayOfWeek d : DayOfWeek.values()) roster.assignDoctor(doc.getId(), d, LocalTime.of(10,0));
        // By default, give nurse MORNING on Monday
        roster.assignNurse(nurse.getId(), DayOfWeek.MONDAY, ShiftBlock.MORNING);
    }

    private Prescription sampleRx() {
        var mo = new MedicationOrder(null, "Paracetamol", "500 mg", "PO",
                                     List.of(LocalTime.of(8,0), LocalTime.of(20,0)));
        return new Prescription(null, alice.getId(), doc.getId(), LocalDate.now(), List.of(mo));
    }

    @Test
    void doctor_adds_prescription_when_rostered_ok() throws Exception {
        // Monday 10:30 local
        var when = ZonedDateTime.now(zone).with(DayOfWeek.MONDAY).withHour(10).withMinute(30).withSecond(0).withNano(0).toInstant();
        String rxId = rxService.addPrescription(doc.getId(), alice.getId(), sampleRx(), when);
        assertNotNull(rxId);
        assertEquals(1, rxRepo.findByResident(alice.getId()).size());
    }

    @Test
    void nurse_cannot_add_prescription() {
        var when = Instant.now();
        var rx = sampleRx();
        assertThrows(NotAuthorizedException.class,
            () -> rxService.addPrescription(nurse.getId(), alice.getId(), rx, when));
    }

    @Test
    void doctor_not_rostered_cannot_add_prescription() {
        // pick a time outside doctor's 10–11
        var when = ZonedDateTime.now(zone).with(DayOfWeek.TUESDAY).withHour(12).withMinute(0).withSecond(0).withNano(0).toInstant();
        var rx = sampleRx();
        assertThrows(NotRosteredException.class,
            () -> rxService.addPrescription(doc.getId(), alice.getId(), rx, when));
    }

    @Test
    void nurse_records_administration_when_rostered_ok() throws Exception {
        // First add a prescription (doctor during coverage)
        var dWhen = ZonedDateTime.now(zone).with(DayOfWeek.WEDNESDAY).withHour(10).withMinute(15).withSecond(0).withNano(0).toInstant();
        var rx = sampleRx();
        String rxId = rxService.addPrescription(doc.getId(), alice.getId(), rx, dWhen);
        String medOrderId = rxRepo.findByResident(alice.getId()).get(0).getOrders().get(0).getId();

        // Nurse is rostered Monday MORNING; record at Monday 09:00
        var nWhen = ZonedDateTime.now(zone).with(DayOfWeek.MONDAY).withHour(9).withMinute(0).withSecond(0).withNano(0).toInstant();
        String adminId = medService.recordAdministration(nurse.getId(), alice.getId(), medOrderId, nWhen, "500 mg", "OK");
        assertNotNull(adminId);
        assertEquals(1, adminRepo.size());
    }

    @Test
    void nurse_not_rostered_cannot_administer() throws Exception {
        // Add Rx under doctor
        var dWhen = ZonedDateTime.now(zone).with(DayOfWeek.THURSDAY).withHour(10).withMinute(5).withSecond(0).withNano(0).toInstant();
        String rxId = rxService.addPrescription(doc.getId(), alice.getId(), sampleRx(), dWhen);
        assertNotNull(rxId);
        String medOrderId = rxRepo.findByResident(alice.getId()).get(0).getOrders().get(0).getId();

        // Try to administer on TUESDAY 23:00 — nurse only has Monday MORNING
        var nWhen = ZonedDateTime.now(zone).with(DayOfWeek.TUESDAY).withHour(23).withMinute(0).withSecond(0).withNano(0).toInstant();
        assertThrows(NotRosteredException.class,
            () -> medService.recordAdministration(nurse.getId(), alice.getId(), medOrderId, nWhen, "500 mg", "late"));
    }
}