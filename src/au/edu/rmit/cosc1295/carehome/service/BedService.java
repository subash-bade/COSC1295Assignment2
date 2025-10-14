package au.edu.rmit.cosc1295.carehome.service;

import java.util.Objects;

import au.edu.rmit.cosc1295.carehome.exceptions.BedNotFoundException;
import au.edu.rmit.cosc1295.carehome.exceptions.BedOccupiedException;
import au.edu.rmit.cosc1295.carehome.exceptions.ResidentNotFoundException;
import au.edu.rmit.cosc1295.carehome.repo.BedRepository;
import au.edu.rmit.cosc1295.carehome.repo.ResidentRepository;
import au.edu.rmit.cosc1295.carehome.model.Bed;

public final class BedService {

    private final ResidentRepository residents;
    private final BedRepository beds;

    public BedService(ResidentRepository residents, BedRepository beds) {
        this.residents = Objects.requireNonNull(residents);
        this.beds = Objects.requireNonNull(beds);
    }

    /**
     * Admit/assign resident to a vacant bed.
     */
    public void addResidentToVacantBed(String residentId, String bedId)
            throws ResidentNotFoundException, BedNotFoundException, BedOccupiedException {

        var resident = residents.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found: " + residentId));

        var bed = beds.findById(bedId)
                .orElseThrow(() -> new BedNotFoundException("Bed not found: " + bedId));

        // Prevent the same resident being assigned twice
        var existing = beds.findByResidentId(resident.getId());
        if (existing.isPresent()) {
            throw new BedOccupiedException("Resident already assigned to bed " + existing.get().getId());
        }

        if (!bed.isVacant()) {
            throw new BedOccupiedException("Bed is occupied: " + bedId);
        }

        bed.occupy(resident.getId());
        beds.save(bed); // persist mutation in repo
    }

    /**
     * Move the resident occupying fromBedId into toBedId.
     * Operation is atomic: if the target is not vacant or any check fails, nothing changes.
     */
    public void moveResident(String fromBedId, String toBedId)
            throws BedNotFoundException, BedOccupiedException {

        if (fromBedId.equals(toBedId)) return; // no-op

        Bed from = beds.findById(fromBedId)
                .orElseThrow(() -> new BedNotFoundException("Source bed not found: " + fromBedId));
        Bed to = beds.findById(toBedId)
                .orElseThrow(() -> new BedNotFoundException("Target bed not found: " + toBedId));

        var residentIdOpt = from.getResidentId();
        if (residentIdOpt.isEmpty()) {
            throw new BedOccupiedException("Source bed is vacant: " + fromBedId);
        }
        if (!to.isVacant()) {
            throw new BedOccupiedException("Target bed is occupied: " + toBedId);
        }

        // Perform move
        String residentId = residentIdOpt.get();
        to.occupy(residentId);
        from.vacate();

        // persist
        beds.save(from);
        beds.save(to);
    }
}
