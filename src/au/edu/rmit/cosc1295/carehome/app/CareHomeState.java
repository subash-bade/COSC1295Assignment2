package au.edu.rmit.cosc1295.carehome.app;

import java.io.Serializable;
import au.edu.rmit.cosc1295.carehome.repo.*;

public final class CareHomeState implements Serializable {
    private static final long serialVersionUID = 1L;

    public final ResidentRepository residents;
    public final StaffRepository staff;
    public final BedRepository beds;
    public final WardRepository wards;
    public final RosterRepository roster;
    public final AuditRepository audit;

    public CareHomeState(ResidentRepository residents, StaffRepository staff,
                         BedRepository beds, WardRepository wards,
                         RosterRepository roster, AuditRepository audit) {
        this.residents = residents; this.staff = staff; this.beds = beds;
        this.wards = wards; this.roster = roster; this.audit = audit;
    }
}
