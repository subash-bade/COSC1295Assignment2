package au.edu.rmit.cosc1295.carehome.service;

import java.time.*;
import java.util.*;
import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.ComplianceViolationException;
import au.edu.rmit.cosc1295.carehome.model.Shift;
import au.edu.rmit.cosc1295.carehome.model.ShiftBlock;
import au.edu.rmit.cosc1295.carehome.repo.RosterRepository;
import au.edu.rmit.cosc1295.carehome.repo.StaffRepository;

public final class CareHome {

    private final StaffRepository staffRepo;
    private final RosterRepository rosterRepo;

    public CareHome(StaffRepository staffRepo, RosterRepository rosterRepo) {
        this.staffRepo = staffRepo;
        this.rosterRepo = rosterRepo;
    }

    /**
     * Enforces:
     * - Nurses only 08:00–16:00 or 14:00–22:00 (exactly these)
     * - ≤ 8h/day per nurse (i.e., at most one block/day)
     * - ≥ 1h doctor coverage every day (sum of doctor minutes >= 60)
     */
    public void checkCompliance() throws ComplianceViolationException {
        var snapshot = rosterRepo.snapshot();
        for (DayOfWeek day : DayOfWeek.values()) {
            var shifts = new ArrayList<>(snapshot.getOrDefault(day, List.of()));

            // --- Nurse shift windows & daily limit ---
            Map<String, Integer> nurseBlocksPerDay = new HashMap<>();
            for (Shift sh : shifts) {
                var role = staffRepo.findById(sh.getStaffId()).map(s -> s.getRole()).orElse(null);
                if (role == null) continue;

                if (role == Role.NURSE) {
                    if (!RosterService.isAllowedNurseBlock(sh.getStart(), sh.getEnd())) {
                        throw new ComplianceViolationException(
                            "Nurse shift not in allowed blocks on " + day + " (" +
                            sh.getStart() + "-" + sh.getEnd() + ")");
                    }
                    nurseBlocksPerDay.merge(sh.getStaffId(), 1, Integer::sum);
                    if (nurseBlocksPerDay.get(sh.getStaffId()) > 1) {
                        throw new ComplianceViolationException(
                            "Nurse double-booked on " + day + " (more than one 8h block)");
                    }
                }
            }

            // --- Doctor coverage ≥ 60 minutes every day ---
            long doctorMinutes = shifts.stream()
                    .filter(s -> staffRepo.findById(s.getStaffId()).map(st -> st.getRole() == Role.DOCTOR).orElse(false))
                    .mapToLong(s -> Duration.between(s.getStart(), s.getEnd()).toMinutes())
                    .sum();
            if (doctorMinutes < 60) {
                throw new ComplianceViolationException("Insufficient doctor coverage on " + day + " ("
                        + doctorMinutes + " minutes)");
            }
        }
    }
}
