package au.edu.rmit.cosc1295.carehome.model;

import java.time.LocalTime;

public enum ShiftBlock {
    MORNING(LocalTime.of(8,0), LocalTime.of(16,0)),
    AFTERNOON(LocalTime.of(14,0), LocalTime.of(22,0));

    public final LocalTime start;
    public final LocalTime end;
    ShiftBlock(LocalTime s, LocalTime e){ this.start = s; this.end = e; }
}