package au.edu.rmit.cosc1295.carehome.service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.ValidationException;
import au.edu.rmit.cosc1295.carehome.model.Shift;
import au.edu.rmit.cosc1295.carehome.model.ShiftBlock;
import au.edu.rmit.cosc1295.carehome.repo.RosterRepository;
import au.edu.rmit.cosc1295.carehome.repo.StaffRepository;

public final class RosterService {
    private final StaffRepository staffRepo;
    private final RosterRepository rosterRepo;

    public RosterService(StaffRepository staffRepo, RosterRepository rosterRepo) {
        this.staffRepo = staffRepo;
        this.rosterRepo = rosterRepo;
    }

    public void assignNurse(String nurseId, DayOfWeek day, ShiftBlock block) throws ValidationException {
        var staff = staffRepo.findById(nurseId)
                .orElseThrow(() -> new ValidationException("Nurse not found: " + nurseId));
        if (staff.getRole() != Role.NURSE) {
            throw new ValidationException("Staff is not a nurse: " + nurseId);
        }
        rosterRepo.add(new Shift(day, block.start, block.end, nurseId));
    }

    /** Exactly one hour coverage block for doctor */
    public void assignDoctor(String doctorId, DayOfWeek day, LocalTime start) throws ValidationException {
        var staff = staffRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found: " + doctorId));
        if (staff.getRole() != Role.DOCTOR) {
            throw new ValidationException("Staff is not a doctor: " + doctorId);
        }
        rosterRepo.add(new Shift(day, start, start.plusHours(1), doctorId));
    }

    public List<Shift> shifts(DayOfWeek day) {
        return List.copyOf(rosterRepo.get(day));
    }

    /** Convenience: map day -> list of shifts */
    public Map<DayOfWeek, List<Shift>> weeklyRosterSnapshot() {
        return rosterRepo.snapshot();
    }

    // ------------- helpers used by CareHome.checkCompliance --------------
    static boolean equalsBlock(LocalTime s, LocalTime e, ShiftBlock block) {
        return s.equals(block.start) && e.equals(block.end);
    }

    static boolean isAllowedNurseBlock(LocalTime s, LocalTime e) {
        return equalsBlock(s, e, ShiftBlock.MORNING) || equalsBlock(s, e, ShiftBlock.AFTERNOON);
    }

    static long totalMinutesForStaffOnDay(List<Shift> shifts, String staffId) {
        return shifts.stream()
                .filter(sh -> sh.getStaffId().equals(staffId))
                .mapToLong(sh -> Duration.between(sh.getStart(), sh.getEnd()).toMinutes())
                .sum();
    }

    static Set<String> staffIdsForRoleOnDay(List<Shift> shifts, Role role, StaffRepository staffRepo) {
        return shifts.stream()
                .map(Shift::getStaffId)
                .filter(id -> staffRepo.findById(id).map(s -> s.getRole() == role).orElse(false))
                .collect(Collectors.toSet());
    }
}