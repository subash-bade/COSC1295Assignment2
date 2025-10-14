package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.edu.rmit.cosc1295.carehome.exceptions.*;
import au.edu.rmit.cosc1295.carehome.model.*;
import au.edu.rmit.cosc1295.carehome.repo.*;
import au.edu.rmit.cosc1295.carehome.service.BedService;

public class BedServiceTest {

    ResidentRepository residentRepo;
    BedRepository bedRepo;
    BedService beds;

    Resident alice;
    Bed b1, b2;

    @BeforeEach
    void setup() {
        residentRepo = new ResidentRepository();
        bedRepo = new BedRepository();
        beds = new BedService(residentRepo, bedRepo);

        alice = new Resident("R1", "Alice Example", Gender.FEMALE, LocalDate.of(1950,1,1));
        residentRepo.save(alice);

        b1 = new Bed("W1-R1-B1", "Bed 1");
        b2 = new Bed("W1-R1-B2", "Bed 2");
        bedRepo.save(b1);
        bedRepo.save(b2);
    }

    @Test
    void addResidentToVacantBed_succeeds() throws Exception {
        beds.addResidentToVacantBed(alice.getId(), b1.getId());
        assertEquals(alice.getId(), bedRepo.findById(b1.getId()).get().getResidentId().orElse(null));
    }

    @Test
    void addResidentToOccupiedBed_throws() throws Exception {
        beds.addResidentToVacantBed(alice.getId(), b1.getId());
        assertThrows(BedOccupiedException.class,
                () -> beds.addResidentToVacantBed(alice.getId(), b1.getId()));
    }

    @Test
    void moveResident_atomicVacateAndOccupy() throws Exception {
        beds.addResidentToVacantBed(alice.getId(), b1.getId());
        beds.moveResident(b1.getId(), b2.getId());

        assertTrue(bedRepo.findById(b1.getId()).get().isVacant());
        assertEquals(alice.getId(), bedRepo.findById(b2.getId()).get().getResidentId().orElse(null));
    }

    @Test
    void moveResident_toOccupied_throwsAndNoChange() throws Exception {
        beds.addResidentToVacantBed(alice.getId(), b1.getId());

        // Occupy b2 with a dummy resident
        var bob = new Resident("R2", "Bob Example", Gender.MALE, LocalDate.of(1948,2,2));
        residentRepo.save(bob);
        beds.addResidentToVacantBed(bob.getId(), b2.getId());

        var ex = assertThrows(BedOccupiedException.class, () -> beds.moveResident(b1.getId(), b2.getId()));
        assertTrue(ex.getMessage().contains("occupied"));

        // State unchanged
        assertEquals(alice.getId(), bedRepo.findById(b1.getId()).get().getResidentId().orElse(null));
        assertEquals(bob.getId(), bedRepo.findById(b2.getId()).get().getResidentId().orElse(null));
    }
}