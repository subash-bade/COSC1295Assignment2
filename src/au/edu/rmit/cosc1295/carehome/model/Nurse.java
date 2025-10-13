package au.edu.rmit.cosc1295.carehome.model;

import au.edu.rmit.cosc1295.carehome.auth.Role;

public final class Nurse extends Staff {
    public Nurse(String id, String name, String username) { super(id, name, username, Role.NURSE); }
}