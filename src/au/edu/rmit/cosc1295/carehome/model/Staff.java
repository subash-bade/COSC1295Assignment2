package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import au.edu.rmit.cosc1295.carehome.auth.Role;

public abstract class Staff implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final String id;
    protected String name;
    protected String username;
    protected Role role;

    protected Staff(String id, String name, String username, Role role) {
        this.id = id; this.name = name; this.username = username; this.role = role;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
}
