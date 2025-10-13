package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.util.Optional;

public final class Bed implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;        // e.g. W1-R2-B1
    private String label;           // human-readable
    private String residentId;      // null if vacant

    public Bed(String id, String label) { this.id = id; this.label = label; }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public Optional<String> getResidentId() { return Optional.ofNullable(residentId); }
    public boolean isVacant() { return residentId == null; }

    public void occupy(String residentId) { this.residentId = residentId; }
    public void vacate() { this.residentId = null; }
}