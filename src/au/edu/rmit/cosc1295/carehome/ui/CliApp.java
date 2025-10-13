package au.edu.rmit.cosc1295.carehome.ui;

import java.util.Scanner;
import au.edu.rmit.cosc1295.carehome.app.*;
import au.edu.rmit.cosc1295.carehome.util.Ids;
import au.edu.rmit.cosc1295.carehome.model.*;

public final class CliApp {

    public void run() {
        System.out.println("[CareHome CLI] Ready. (Phase 1 skeleton)");
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Type 'seed' to create a demo ward+beds, or 'exit' to quit.");
            while (true) {
                System.out.print("> ");
                var cmd = sc.nextLine().trim();
                if (cmd.equalsIgnoreCase("exit")) break;
                if (cmd.equalsIgnoreCase("seed")) {
                    seedBeds();
                    System.out.println("Seeded W1 with 2 rooms x 2 beds.");
                } else {
                    System.out.println("Unknown command.");
                }
            }
        }
    }

    private void seedBeds() {
        var ctx = AppContext.get();
        var ward = new Ward("W1");
        var r1 = new Room("W1-R1");
        r1.getBeds().add(new Bed("W1-R1-B1", "Bed 1"));
        r1.getBeds().add(new Bed("W1-R1-B2", "Bed 2"));
        var r2 = new Room("W1-R2");
        r2.getBeds().add(new Bed("W1-R2-B1", "Bed 1"));
        r2.getBeds().add(new Bed("W1-R2-B2", "Bed 2"));
        ward.getRooms().add(r1); ward.getRooms().add(r2);

        ctx.state.wards.save(ward);
        for (var room : ward.getRooms())
            for (var bed : room.getBeds())
                ctx.state.beds.save(bed);

        // Add a demo resident (vacant bed for now)
        var res = new Resident(Ids.newId(), "Alice Example", Gender.FEMALE, java.time.LocalDate.of(1950,1,1));
        ctx.state.residents.save(res);
    }
}