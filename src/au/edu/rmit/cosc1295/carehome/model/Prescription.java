package au.edu.rmit.cosc1295.carehome.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                // set by service if null
    private String residentId;
    private String doctorId;
    private LocalDate date;
    private final List<MedicationOrder> orders = new ArrayList<>();

    public Prescription(String id, String residentId, String doctorId, LocalDate date, List<MedicationOrder> orders) {
        this.id = id; this.residentId = residentId; this.doctorId = doctorId; this.date = date;
        if (orders != null) this.orders.addAll(orders);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getResidentId() { return residentId; }
    public String getDoctorId() { return doctorId; }
    public LocalDate getDate() { return date; }
    public List<MedicationOrder> getOrders() { return orders; }
}