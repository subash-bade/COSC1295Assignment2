package au.edu.rmit.cosc1295.carehome.repo;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.*;
import au.edu.rmit.cosc1295.carehome.model.Shift;

public final class RosterRepository implements Serializable {
    private static final long serialVersionUID = 1L;

    // day -> list of shifts for that day
    private final Map<DayOfWeek, List<Shift>> byDay = new EnumMap<>(DayOfWeek.class);

    public RosterRepository() {
        for (DayOfWeek d : DayOfWeek.values()) byDay.put(d, new ArrayList<>());
    }

    public List<Shift> get(DayOfWeek day) { return byDay.get(day); }

    public void add(Shift shift) { byDay.get(shift.getDay()).add(shift); }

    public Map<DayOfWeek, List<Shift>> snapshot() {
        Map<DayOfWeek, List<Shift>> copy = new EnumMap<>(DayOfWeek.class);
        byDay.forEach((k,v) -> copy.put(k, List.copyOf(v)));
        return copy;
    }

    public void clear() { byDay.values().forEach(List::clear); }
}