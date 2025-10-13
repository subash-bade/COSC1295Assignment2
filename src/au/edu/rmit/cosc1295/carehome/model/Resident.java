package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.time.LocalDate;

public final class Resident implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
    private ResidentStatus status;

    public Resident(String id, String name, Gender gender, LocalDate dob) {
        this.id = id; this.name = name; this.gender = gender; this.dateOfBirth = dob;
        this.status = ResidentStatus.ADMITTED;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Gender getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public ResidentStatus getStatus() { return status; }
    public void setStatus(ResidentStatus status) { this.status = status; }
}