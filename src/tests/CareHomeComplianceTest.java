package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.edu.rmit.cosc1295.carehome.exceptions.ComplianceViolationException;
import au.edu.rmit.cosc1295.carehome.model.*;
import au.edu.rmit.cosc1295.carehome.repo.*;
import au.edu.rmit.cosc1295.carehome.service.*;

public class CareHomeComplianceTest {

    StaffRepository staffRepo;
    RosterRepository rosterRepo;
    CareHome careHome;
    RosterService roster;

    Nurse n1;
    Doctor d1;

    @BeforeEach
    void setup() throws Exception {
        staffRepo = new StaffRepository();
        rosterRepo = new RosterRepository();
        careHome = new CareHome(staffRepo, rosterRepo);
        roster = new RosterService(staffRepo, rosterRepo);

        n1 = new Nurse("N1", "Nina", "nina");
        d1 = new Doctor("D1", "Dee", "dee");
        staffRepo.save(n1);
        staffRepo.save(d1);

        // Satisfy doctor rule first: 1h coverage every day at 10:00
        for (DayOfWeek d : DayOfWeek.values()) {
            roster.assignDoctor(d1.getId(), d, LocalTime.of(10, 0));
        }
    }

    @Test
    void ok_when_nurse_in_allowed_block_once_per_day() throws Exception {
        roster.assignNurse(n1.getId(), DayOfWeek.MONDAY, ShiftBlock.MORNING);
        assertDoesNotThrow(() -> careHome.checkCompliance());
    }

    @Test
    void fail_when_nurse_has_custom_hours() {
        // Inject a bad shift (09:00–17:00) to violate allowed blocks
        rosterRepo.add(new Shift(DayOfWeek.TUESDAY, LocalTime.of(9,0), LocalTime.of(17,0), n1.getId()));
        var ex = assertThrows(ComplianceViolationException.class, () -> careHome.checkCompliance());
        assertTrue(ex.getMessage().toLowerCase().contains("allowed"));
    }

    @Test
    void fail_when_nurse_double_booked_same_day() throws Exception {
        roster.assignNurse(n1.getId(), DayOfWeek.WEDNESDAY, ShiftBlock.MORNING);
        roster.assignNurse(n1.getId(), DayOfWeek.WEDNESDAY, ShiftBlock.AFTERNOON);
        var ex = assertThrows(ComplianceViolationException.class, () -> careHome.checkCompliance());
        assertTrue(ex.getMessage().toLowerCase().contains("double"));
    }

    @Test
    void fail_when_doctor_coverage_missing_a_day() {
        rosterRepo.get(DayOfWeek.SUNDAY).clear(); // remove Sunday coverage
        var ex = assertThrows(ComplianceViolationException.class, () -> careHome.checkCompliance());
        assertTrue(ex.getMessage().toLowerCase().contains("doctor"));
    }
}