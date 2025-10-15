package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import au.edu.rmit.cosc1295.carehome.app.*;
import au.edu.rmit.cosc1295.carehome.model.*;

public class StateSerializationTest {

    @Test
    void save_then_load_restores_repos_sizes() throws Exception {
        // fresh context in temp dir
        var tmp = Files.createTempDirectory("carehome-");
        var saveFile = tmp.resolve("state.bin");

        // build a small world
        var fresh = AppContext.initNew();
        var ward = new Ward("WZ");
        var room = new Room("WZ-R1");
        room.getBeds().add(new Bed("WZ-R1-B1", "Bed 1"));
        ward.getRooms().add(room);
        fresh.state.wards.save(ward);
        for (var b : room.getBeds()) fresh.state.beds.save(b);

        var res = new Resident("RX", "X", Gender.OTHER, LocalDate.of(1970,1,1));
        fresh.state.residents.save(res);

        // save
        StateSerializer.save(fresh.state, saveFile);

        // load
        var loaded = StateSerializer.loadOrNew(saveFile, AppContext.initNew().state);

        assertEquals(1, loaded.wards.size());
        assertEquals(1, loaded.beds.size());
        assertEquals(1, loaded.residents.size());
    }
}