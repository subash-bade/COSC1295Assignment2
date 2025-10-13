package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Ward implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id; // e.g. W1
    private final List<Room> rooms = new ArrayList<>();

    public Ward(String id) { this.id = id; }
    public String getId() { return id; }
    public List<Room> getRooms() { return rooms; }
}