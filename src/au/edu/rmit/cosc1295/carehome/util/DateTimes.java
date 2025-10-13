package au.edu.rmit.cosc1295.carehome.util;
import java.time.*;

public final class DateTimes {
    private DateTimes() {}

    public static LocalDate today() { return LocalDate.now(); }
    public static Instant now() { return Instant.now(); }

    public static ZoneId systemZone() { return ZoneId.systemDefault(); }
}
