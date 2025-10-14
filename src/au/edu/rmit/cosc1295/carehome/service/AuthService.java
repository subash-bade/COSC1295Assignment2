package au.edu.rmit.cosc1295.carehome.service;

import java.time.*;
import java.util.EnumSet;

import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.NotAuthorizedException;
import au.edu.rmit.cosc1295.carehome.exceptions.NotRosteredException;
import au.edu.rmit.cosc1295.carehome.model.Shift;
import au.edu.rmit.cosc1295.carehome.repo.RosterRepository;
import au.edu.rmit.cosc1295.carehome.repo.StaffRepository;

public final class AuthService {
    private final StaffRepository staffRepo;
    private final RosterRepository rosterRepo;
    private final ZoneId zone;

    public AuthService(StaffRepository staffRepo, RosterRepository rosterRepo, ZoneId zone) {
        this.staffRepo = staffRepo;
        this.rosterRepo = rosterRepo;
        this.zone = zone == null ? ZoneId.systemDefault() : zone;
    }

    public void requireRole(String staffId, Role... allowed) throws NotAuthorizedException {
        var staff = staffRepo.findById(staffId).orElseThrow(() -> new NotAuthorizedException("Unknown staff: " + staffId));
        var set = EnumSet.noneOf(Role.class);
        for (var r : allowed) set.add(r);
        if (!set.contains(staff.getRole())) {
            throw new NotAuthorizedException("Role not permitted: " + staff.getRole());
        }
    }

    public void requireRostered(String staffId, Instant at) throws NotRosteredException {
        if (!isRostered(staffId, at)) {
            throw new NotRosteredException("Staff not rostered at this time");
        }
    }

    public boolean isRostered(String staffId, Instant at) {
        var ldt = LocalDateTime.ofInstant(at, zone);
        var day = ldt.getDayOfWeek();
        var t = ldt.toLocalTime();
        for (Shift s : rosterRepo.get(day)) {
            if (!s.getStaffId().equals(staffId)) continue;
            // treat end as exclusive bound
            boolean within = !t.isBefore(s.getStart()) && t.isBefore(s.getEnd());
            if (within) return true;
        }
        return false;
    }
}