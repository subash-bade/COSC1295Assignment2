package tests;


import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.ComplianceViolationException;
import au.edu.rmit.cosc1295.carehome.exceptions.ValidationException;
import au.edu.rmit.cosc1295.carehome.model.*;
import au.edu.rmit.cosc1295.carehome.repo.*;
import au.edu.rmit.cosc1295.carehome.service.*;



public class RosterServiceTest {

    StaffRepository staffRepo;
    RosterRepository rosterRepo;
    RosterService roster;
    CareHome careHome;

    Nurse nurse;
    Doctor doc;

    @BeforeEach
    void setup() {
        staffRepo = new StaffRepository();
        rosterRepo = new RosterRepository();
        roster = new RosterService(staffRepo, rosterRepo);
        careHome = new CareHome(staffRepo, rosterRepo);

        nurse = new Nurse("N1", "Nurse Nancy", "nnancy");
        doc   = new Doctor("D1", "Dr. Dan", "ddan");

        staffRepo.save(nurse);
        staffRepo.save(doc);

        // Ensure every test starts with 1h doctor coverage everyday by default
        for (DayOfWeek d : DayOfWeek.values()) {
            try { roster.assignDoctor(doc.getId(), d, LocalTime.of(9, 0)); }
            catch (ValidationException e) { fail(e); }
        }
    }

    @Test
    void validNurseShiftPassesCompliance() throws Exception {
        roster.assignNurse(nurse.getId(), DayOfWeek.MONDAY, ShiftBlock.MORNING);
        assertDoesNotThrow(() -> careHome.checkCompliance());
    }

    @Test
    void nurseShiftInvalidHoursFailsCompliance() throws Exception {
        // manually inject an invalid shift 09:00-17:00 (bypassing assignNurse)
        rosterRepo.add(new Shift(DayOfWeek.TUESDAY, LocalTime.of(9,0), LocalTime.of(17,0), nurse.getId()));
        var ex = assertThrows(ComplianceViolationException.class, () -> careHome.checkCompliance());
        assertTrue(ex.getMessage().contains("Nurse shift not in allowed blocks"));
    }

    @Test
    void nurseDoubleBookedSameDayFails() throws Exception {
        roster.assignNurse(nurse.getId(), DayOfWeek.WEDNESDAY, ShiftBlock.MORNING);
        roster.assignNurse(nurse.getId(), DayOfWeek.WEDNESDAY, ShiftBlock.AFTERNOON);
        var ex = assertThrows(ComplianceViolationException.class, () -> careHome.checkCompliance());
        assertTrue(ex.getMessage().contains("double-booked"));
    }

    @Test
    void missingDoctorCoverageDayFails() throws Exception {
        // Clear one day's doctor shift by making a fresh repo for Thursday
        rosterRepo.get(DayOfWeek.THURSDAY).clear(); // remove the default 1h we added in setup
        var ex = assertThrows(ComplianceViolationException.class, () -> careHome.checkCompliance());
        assertTrue(ex.getMessage().contains("Insufficient doctor coverage"));
    }
}