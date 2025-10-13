package au.edu.rmit.cosc1295.carehome.app;

import java.io.*;
import java.nio.file.*;

public final class StateSerializer {
    private StateSerializer(){}

    public static void save(CareHomeState state, Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(state);
        }
    }

    public static CareHomeState loadOrNew(Path path, CareHomeState fallback) {
        if (!Files.exists(path)) return fallback;
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            return (CareHomeState) ois.readObject();
        } catch (Exception e) {
            return fallback;
        }
    }
}
