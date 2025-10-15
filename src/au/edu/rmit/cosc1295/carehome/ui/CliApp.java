package au.edu.rmit.cosc1295.carehome.ui;

import java.util.Scanner;

import au.edu.rmit.cosc1295.carehome.app.AppContext;
import au.edu.rmit.cosc1295.carehome.model.Bed;
import au.edu.rmit.cosc1295.carehome.model.Gender;
import au.edu.rmit.cosc1295.carehome.model.Resident;
import au.edu.rmit.cosc1295.carehome.model.Room;
import au.edu.rmit.cosc1295.carehome.model.Ward;
import au.edu.rmit.cosc1295.carehome.repo.AuditRepository;
import au.edu.rmit.cosc1295.carehome.util.Ids;

public final class CliApp {

    public void run() {
        System.out.println("[CareHome CLI] Ready. (Phase 1 skeleton + audit view)");
        try (Scanner sc = new Scanner(System.in)) {
            printHelp();
            while (true) {
                System.out.print("> ");
                var cmd = sc.nextLine().trim();

                if (cmd.equalsIgnoreCase("exit")) {
                    break;
                } else if (cmd.equalsIgnoreCase("help")) {
                    printHelp();
                } else if (cmd.equalsIgnoreCase("seed")) {
                    seedBeds();
                    System.out.println("Seeded W1 with 2 rooms x 2 beds, plus demo resident.");
                } else if (cmd.equalsIgnoreCase("audit")) {
                    listAudit();
                } else {
                    System.out.println("Unknown command. Type 'help' to see options.");
                }
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
              help   - show this menu
              seed   - create demo ward/rooms/beds and a sample resident
              audit  - list audit log entries
              exit   - save & quit
            """);
    }

    private void seedBeds() {
        var ctx = AppContext.get();

        // Ward/rooms/beds
        var ward = new Ward("W1");
        var r1 = new Room("W1-R1");
        r1.getBeds().add(new Bed("W1-R1-B1", "Bed 1"));
        r1.getBeds().add(new Bed("W1-R1-B2", "Bed 2"));
        var r2 = new Room("W1-R2");
        r2.getBeds().add(new Bed("W1-R2-B1", "Bed 1"));
        r2.getBeds().add(new Bed("W1-R2-B2", "Bed 2"));
        ward.getRooms().add(r1);
        ward.getRooms().add(r2);

        ctx.state.wards.save(ward);
        for (var room : ward.getRooms()) {
            for (var bed : room.getBeds()) {
                ctx.state.beds.save(bed);
            }
        }

        // Demo resident
        var res = new Resident(Ids.newId(), "Alice Example", Gender.FEMALE,
                java.time.LocalDate.of(1950, 1, 1));
        ctx.state.residents.save(res);
    }

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