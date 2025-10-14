package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class MedicationOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                 // set by service if null
    private String drug;
    private String dose;               // e.g., "500 mg"
    private String route;              // e.g., "PO"
    private final List<LocalTime> scheduleTimes = new ArrayList<>();

    public MedicationOrder(String id, String drug, String dose, String route, List<LocalTime> times) {
        this.id = id; this.drug = drug; this.dose = dose; this.route = route;
        if (times != null) this.scheduleTimes.addAll(times);
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDrug() { return drug; }
    public String getDose() { return dose; }
    public String getRoute() { return route; }
    public List<LocalTime> getScheduleTimes() { return scheduleTimes; }
}