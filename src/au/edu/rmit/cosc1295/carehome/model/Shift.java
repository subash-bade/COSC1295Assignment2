package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.time.*;

public final class Shift implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;
    private final String staffId;

    public Shift(DayOfWeek day, LocalTime start, LocalTime end, String staffId) {
        this.day = day; this.start = start; this.end = end; this.staffId = staffId;
    }
    public DayOfWeek getDay() { return day; }
    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }
    public String getStaffId() { return staffId; }
    public Duration duration() { return Duration.between(start, end); }
}