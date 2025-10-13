package au.edu.rmit.cosc1295.carehome.repo;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

public final class AuditRepository implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final class Entry implements Serializable {
        private static final long serialVersionUID = 1L;
        public final Instant ts;
        public final String staffId;
        public final String action;
        public final String details;
        public Entry(Instant ts, String staffId, String action, String details) {
            this.ts = ts; this.staffId = staffId; this.action = action; this.details = details;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    public void append(Entry e) { entries.add(e); }
    public List<Entry> all() { return List.copyOf(entries); }
}
