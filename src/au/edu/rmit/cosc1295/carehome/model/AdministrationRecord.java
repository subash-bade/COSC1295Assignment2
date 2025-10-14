package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.time.Instant;

public final class AdministrationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String residentId;
    private final String medOrderId;
    private final Instant time;
    private final String nurseId;
    private final String doseGiven;
    private final String notes;

    public AdministrationRecord(String id, String residentId, String medOrderId,
                                Instant time, String nurseId, String doseGiven, String notes) {
        this.id = id; this.residentId = residentId; this.medOrderId = medOrderId;
        this.time = time; this.nurseId = nurseId; this.doseGiven = doseGiven; this.notes = notes;
    }

    public String getId() { return id; }
    public String getResidentId() { return residentId; }
    public String getMedOrderId() { return medOrderId; }
    public Instant getTime() { return time; }
    public String getNurseId() { return nurseId; }
    public String getDoseGiven() { return doseGiven; }
    public String getNotes() { return notes; }
}
