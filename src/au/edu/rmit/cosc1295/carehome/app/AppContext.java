package au.edu.rmit.cosc1295.carehome.app;

import java.nio.file.Path;
import au.edu.rmit.cosc1295.carehome.repo.*;

public final class AppContext {
    private static AppContext INSTANCE;

    public final CareHomeState state;
    public final Path saveFile = Path.of("data", "carehome-state.bin");

    private AppContext(CareHomeState state) { this.state = state; }

    public static AppContext initNew() {
        var ctx = new AppContext(new CareHomeState(
            new ResidentRepository(),
            new StaffRepository(),
            new BedRepository(),
            new WardRepository(),
            new RosterRepository(),
            new AuditRepository()
        ));
        INSTANCE = ctx;
        return ctx;
    }

    public static AppContext loadOrInit() {
        var fresh = initNew();
        var loadedState = StateSerializer.loadOrNew(fresh.saveFile, fresh.state);
        INSTANCE = new AppContext(loadedState);
        return INSTANCE;
    }

    public static AppContext get() { return INSTANCE; }
}
