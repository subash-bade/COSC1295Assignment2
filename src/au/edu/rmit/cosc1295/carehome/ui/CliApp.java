package au.edu.rmit.cosc1295.carehome.ui;

import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import au.edu.rmit.cosc1295.carehome.app.AppContext;
import au.edu.rmit.cosc1295.carehome.app.Services;
import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.*;
import au.edu.rmit.cosc1295.carehome.model.*;
import au.edu.rmit.cosc1295.carehome.repo.AuditRepository;
import au.edu.rmit.cosc1295.carehome.util.Ids;

public final class CliApp {

    private final Services svc;

    public CliApp(Services services) {
        this.svc = services;
    }

    public void run() {
        System.out.println("[CareHome CLI] Ready. Type 'help' for commands.");
        try (Scanner sc = new Scanner(System.in)) {
            printHelp();
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                var line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                var parts = Arrays.asList(line.split("\\s+"));
                var cmd = parts.get(0).toLowerCase();
                var args = parts.subList(1, parts.size());

                try {
                    switch (cmd) {
                        case "exit" -> { return; }
                        case "help" -> printHelp();
                        case "seed" -> seed();
                        case "listbeds" -> listBeds();
                        case "listres" -> listResidents();
                        case "liststaff" -> listStaff();
                        case "assign" -> cmdAssign(args);
                        case "move" -> cmdMove(args);
                        case "makedoctor" -> cmdMakeStaff(args, Role.DOCTOR);
                        case "makenurse" -> cmdMakeStaff(args, Role.NURSE);
                        case "rosternurse" -> cmdRosterNurse(args);
                        case "rosterdoc" -> cmdRosterDoc(args);
                        case "addrx" -> cmdAddRx(args);
                        case "listorders" -> cmdListOrders(args);
                        case "admin" -> cmdAdmin(args);
                        case "audit" -> listAudit();
                        default -> System.out.println("Unknown command. Type 'help'.");
                    }
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getClass().getSimpleName() + (e.getMessage()==null?"":": "+e.getMessage()));
                }
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
              help
              exit
              seed                            # demo ward/rooms/beds + one resident
              listbeds                        # show all beds and occupancy
              listres                         # list residents (id, name, gender, dob)
              liststaff                       # list staff
              assign <residentId> <bedId>     # place resident in vacant bed
              move <fromBedId> <toBedId>      # move resident between beds
              
              makedoctor <id> <name> <user>   # create a doctor
              makenurse  <id> <name> <user>   # create a nurse
              rosternurse <nurseId> <DAY> <MORNING|AFTERNOON>
              rosterdoc   <docId>   <DAY> <HH:mm>     # exactly 1 hour coverage at start time
              
              addrx <docId> <residentId> <drug> <dose> <route> [DAY HH:mm]
                # Adds a prescription. If DAY HH:mm provided, uses that moment for roster check.
                # Otherwise uses the current time (Instant.now()).
              
              listorders <residentId>     # list resident prescriptions & orders
              admin <nurseId> <residentId> <medOrderId> <doseGiven> [notes...] [DAY HH:mm]
        	   # Records an administration. If DAY HH:mm provided, uses that moment for roster check; otherwise uses now.
              
              audit                           # show audit log
            """);
    }

    // ---------- seed & listings ----------
    private void seed() {
        var ctx = AppContext.get();
        // ward/rooms/beds
        var ward = new Ward("W1");
        var r1 = new Room("W1-R1");
        r1.getBeds().add(new Bed("W1-R1-B1", "Bed 1"));
        r1.getBeds().add(new Bed("W1-R1-B2", "Bed 2"));
        var r2 = new Room("W1-R2");
        r2.getBeds().add(new Bed("W1-R2-B1", "Bed 1"));
        r2.getBeds().add(new Bed("W1-R2-B2", "Bed 2"));
        ward.getRooms().addAll(List.of(r1, r2));
        ctx.state.wards.save(ward);
        for (var room : ward.getRooms()) for (var bed : room.getBeds()) ctx.state.beds.save(bed);

        // demo resident
        var res = new Resident(Ids.newId(), "Alice Example", Gender.FEMALE, LocalDate.of(1950, 1, 1));
        ctx.state.residents.save(res);

        System.out.println("Seed complete. ResidentId=" + res.getId());
    }

    private void listBeds() {
        var beds = AppContext.get().state.beds.findAll();
        if (beds.isEmpty()) { System.out.println("(no beds)"); return; }
        for (var b : beds) {
            var occ = b.getResidentId().orElse("(vacant)");
            System.out.printf("%s %-10s occ=%s%n", b.getId(), b.getLabel(), occ);
        }
    }

    private void listResidents() {
        var all = AppContext.get().state.residents.findAll();
        if (all.isEmpty()) { System.out.println("(no residents)"); return; }
        for (var r : all) {
            System.out.printf("%s  %s  %s  %s%n", r.getId(), r.getName(), r.getGender(), r.getDateOfBirth());
        }
    }

    private void listStaff() {
        var all = AppContext.get().state.staff.findAll();
        if (all.isEmpty()) { System.out.println("(no staff)"); return; }
        for (var s : all) {
            System.out.printf("%s  %-6s  %s (%s)%n", s.getId(),
                    s.getRole(), s.getName(), s.getUsername());
        }
    }

    // ---------- commands: beds ----------
    private void cmdAssign(List<String> a) throws Exception {
        if (a.size() < 2) { System.out.println("Usage: assign <residentId> <bedId>"); return; }
        var residentId = a.get(0);
        var bedId = a.get(1);
        svc.audit.auditedVoid("system", "BED_ASSIGN", () -> "res="+residentId+",bed="+bedId,
                () -> {
                    try {
                        svc.beds.addResidentToVacantBed(residentId, bedId);
                        System.out.println("Assigned.");
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }

    private void cmdMove(List<String> a) throws Exception {
        if (a.size() < 2) { System.out.println("Usage: move <fromBedId> <toBedId>"); return; }
        var from = a.get(0);
        var to = a.get(1);
        svc.audit.auditedVoid("system", "BED_MOVE", () -> "from="+from+",to="+to,
                () -> {
                    try {
                        svc.beds.moveResident(from, to);
                        System.out.println("Moved.");
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
    }

    // ---------- commands: staff & roster ----------
    private void cmdMakeStaff(List<String> a, Role role) {
        if (a.size() < 3) {
            System.out.println("Usage: make" + (role==Role.DOCTOR?"doctor":"nurse") + " <id> <name> <username>");
            return;
        }
        var id = a.get(0);
        var name = a.get(1);
        var user = a.get(2);
        switch (role) {
            case DOCTOR -> AppContext.get().state.staff.save(new Doctor(id, name, user));
            case NURSE  -> AppContext.get().state.staff.save(new Nurse(id, name, user));
            default -> {}
        }
        System.out.println("Created " + role + " id=" + id);
    }

    private void cmdRosterNurse(List<String> a) throws ValidationException {
        if (a.size() < 3) { System.out.println("Usage: rosternurse <nurseId> <DAY> <MORNING|AFTERNOON>"); return; }
        var nurseId = a.get(0);
        var day = DayOfWeek.valueOf(a.get(1).toUpperCase());
        var block = ShiftBlock.valueOf(a.get(2).toUpperCase());
        svc.roster.assignNurse(nurseId, day, block);
        System.out.println("Rostered nurse " + nurseId + " on " + day + " " + block);
    }

    private void cmdRosterDoc(List<String> a) throws ValidationException {
        if (a.size() < 3) { System.out.println("Usage: rosterdoc <docId> <DAY> <HH:mm>"); return; }
        var docId = a.get(0);
        var day = DayOfWeek.valueOf(a.get(1).toUpperCase());
        var start = LocalTime.parse(a.get(2));
        svc.roster.assignDoctor(docId, day, start);
        System.out.println("Rostered doctor " + docId + " on " + day + " " + start + "–" + start.plusHours(1));
    }

    // ---------- commands: meds ----------
    private void cmdAddRx(List<String> a) throws Exception {
        if (a.size() < 5) { System.out.println("Usage: addrx <docId> <residentId> <drug> <dose> <route> [DAY HH:mm]"); return; }
        var docId = a.get(0);
        var residentId = a.get(1);
        var drug = a.get(2);
        var dose = a.get(3);
        var route = a.get(4);

        // Optional DAY HH:mm
        Instant when;
        if (a.size() >= 7) {
            var day = DayOfWeek.valueOf(a.get(5).toUpperCase());
            var start = LocalTime.parse(a.get(6));
            var now = ZonedDateTime.now();
            // pick a date in the current week matching the requested day
            var base = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                          .withHour(start.getHour()).withMinute(start.getMinute())
                          .withSecond(0).withNano(0);
            when = base.toInstant();
        } else {
            when = Instant.now();
        }

        var order = new MedicationOrder(null, drug, dose, route, List.of(LocalTime.of(8,0), LocalTime.of(20,0)));
        var rx = new Prescription(null, residentId, docId, LocalDate.now(), List.of(order));

        var rxId = svc.audit.audited(docId, "RX_ADD",
                () -> "resident="+residentId+",drug="+drug,
                () -> svc.prescriptions.addPrescription(docId, residentId, rx, when));
        System.out.println("Prescription added. id=" + rxId + " orderId=" + order.getId());
    }

    private void cmdListOrders(List<String> a) {
        if (a.size() < 1) { System.out.println("Usage: listorders <residentId>"); return; }
        var residentId = a.get(0);
        var list = AppContext.get().state.prescriptions.findByResident(residentId);
        if (list.isEmpty()) { System.out.println("(no prescriptions)"); return; }
        for (var p : list) {
            System.out.println("Rx " + p.getId() + " by " + p.getDoctorId() + " on " + p.getDate());
            for (var o : p.getOrders()) {
                System.out.println("  order " + o.getId() + "  " + o.getDrug() + " " + o.getDose() + " " + o.getRoute()
                        + " times=" + o.getScheduleTimes());
            }
        }
    }

    private void cmdAdmin(List<String> a) throws Exception {
        if (a.size() < 4) {
            System.out.println("Usage: admin <nurseId> <residentId> <medOrderId> <doseGiven> [notes...] [DAY HH:mm]");
            return;
        }
        var nurseId = a.get(0);
        var residentId = a.get(1);
        var medOrderId = a.get(2);
        var doseGiven = a.get(3);

        // parse into local (mutable) temps first
        Instant tempWhen = Instant.now();
        String tempNotes = "";

        if (a.size() >= 6) {
            // Try to parse last two args as DAY and HH:mm
            try {
                var dayToken = a.get(a.size() - 2).toUpperCase();
                var timeToken = a.get(a.size() - 1);
                var day = DayOfWeek.valueOf(dayToken);
                var time = LocalTime.parse(timeToken);
                var base = ZonedDateTime.now()
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                        .withHour(time.getHour()).withMinute(time.getMinute())
                        .withSecond(0).withNano(0);
                tempWhen = base.toInstant();
                tempNotes = (a.size() > 6) ? String.join(" ", a.subList(4, a.size() - 2)) : "";
            } catch (Exception parseFail) {
                // Not a valid DAY/HH:mm → treat everything after dose as notes; keep 'now'
                tempNotes = String.join(" ", a.subList(4, a.size()));
            }
        } else if (a.size() > 4) {
            tempNotes = String.join(" ", a.subList(4, a.size()));
        }

        // make final/effectively-final copies for the lambdas
        final Instant when = tempWhen;
        final String notes = tempNotes;

        var adminId = svc.audit.audited(nurseId, "ADMIN_RECORD",
                () -> "resident=" + residentId + ",order=" + medOrderId,
                () -> svc.medications.recordAdministration(nurseId, residentId, medOrderId, when, doseGiven, notes));
        System.out.println("Administration recorded. id=" + adminId);
    }



    // ---------- audit ----------
    private void listAudit() {
        var entries = AppContext.get().state.audit.all();
        if (entries.isEmpty()) {
            System.out.println("(no audit entries yet)");
            return;
        }
        for (AuditRepository.Entry e : entries) {
            System.out.println(e.ts + " | staff=" + e.staffId + " | " + e.action + " | " + e.details);
        }
    }
}