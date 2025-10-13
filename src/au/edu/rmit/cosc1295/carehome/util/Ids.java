package au.edu.rmit.cosc1295.carehome.util;
import java.util.UUID;

public final class Ids {
    private Ids() {}
    public static String newId() { return UUID.randomUUID().toString(); }
}
