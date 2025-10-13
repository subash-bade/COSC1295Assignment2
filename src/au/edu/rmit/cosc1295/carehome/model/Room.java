package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id; // e.g. W1-R2
    private final List<Bed> beds = new ArrayList<>();

    public Room(String id) { this.id = id; }
    public String getId() { return id; }
    public List<Bed> getBeds() { return beds; }
}