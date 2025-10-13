package au.edu.rmit.cosc1295.carehome.model;

import au.edu.rmit.cosc1295.carehome.auth.Role;

public final class Doctor extends Staff {
    public Doctor(String id, String name, String username) { super(id, name, username, Role.DOCTOR); }
}
