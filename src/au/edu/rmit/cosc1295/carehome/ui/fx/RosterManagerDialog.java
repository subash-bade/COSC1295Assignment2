package au.edu.rmit.cosc1295.carehome.ui.fx;

import java.time.DayOfWeek;
import java.time.LocalTime;

import au.edu.rmit.cosc1295.carehome.app.AppContext;
import au.edu.rmit.cosc1295.carehome.app.Services;
import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.exceptions.ValidationException;
import au.edu.rmit.cosc1295.carehome.model.Doctor;
import au.edu.rmit.cosc1295.carehome.model.Nurse;
import au.edu.rmit.cosc1295.carehome.model.ShiftBlock;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/** Simple modal window to CRUD staff and assign rosters. */
public final class RosterManagerDialog extends Stage {

    private final Services svc;

    public RosterManagerDialog(Stage owner, Services services) {
        this.svc = services;
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Roster & Staff Manager");

        var tabs = new TabPane();
        tabs.getTabs().add(tabStaff());
        tabs.getTabs().add(tabRosterNurse());
        tabs.getTabs().add(tabRosterDoctor());

        var root = new BorderPane(tabs);
        root.setPadding(new Insets(10));
        setScene(new Scene(root, 640, 420));
    }

    // ---------- Staff tab ----------
    private Tab tabStaff() {
        var box = new VBox(10); box.setPadding(new Insets(10));
        var tfId   = new TextField(); tfId.setPromptText("id (e.g. N2 or D2)");
        var tfName = new TextField(); tfName.setPromptText("name");
        var tfUser = new TextField(); tfUser.setPromptText("username");
        var role = new ComboBox<Role>(); role.getItems().addAll(Role.NURSE, Role.DOCTOR); role.setPromptText("Role");

        var btnAdd = new Button("Add Staff");
        var msg = new Label(); msg.setStyle("-fx-text-fill:#2f6df6;");

        btnAdd.setOnAction(e -> {
            var id = tfId.getText().trim();
            var name = tfName.getText().trim();
            var user = tfUser.getText().trim();
            var r = role.getValue();
            if (id.isEmpty() || name.isEmpty() || user.isEmpty() || r == null) {
                msg.setText("Fill id/name/username/role");
                return;
            }
            switch (r) {
                case NURSE -> AppContext.get().state.staff.save(new Nurse(id, name, user));
                case DOCTOR -> AppContext.get().state.staff.save(new Doctor(id, name, user));
                default -> {}
            }
            msg.setText("Saved " + r + " " + id);
        });

        box.getChildren().addAll(new Label("Create staff"),
                new HBox(8, new Label("Id:"), tfId, new Label("Name:"), tfName),
                new HBox(8, new Label("User:"), tfUser, new Label("Role:"), role),
                btnAdd, msg);

        var t = new Tab("Staff", box); t.setClosable(false);
        return t;
    }

    // ---------- Nurse roster tab ----------
    private Tab tabRosterNurse() {
        var box = new VBox(10); box.setPadding(new Insets(10));
        var cbNurse = new ComboBox<String>();
        AppContext.get().state.staff.findAll().stream()
                .filter(s -> s.getRole() == Role.NURSE).forEach(s -> cbNurse.getItems().add(s.getId()));
        cbNurse.setPromptText("nurseId");

        var cbDay = new ComboBox<DayOfWeek>(); cbDay.getItems().addAll(DayOfWeek.values()); cbDay.setPromptText("DAY");
        var cbBlock = new ComboBox<ShiftBlock>(); cbBlock.getItems().addAll(ShiftBlock.values()); cbBlock.setPromptText("MORNING/AFTERNOON");

        var btnAssign = new Button("Assign Nurse");
        var msg = new Label(); msg.setStyle("-fx-text-fill:#2f6df6;");

        btnAssign.setOnAction(e -> {
            try {
                var n = cbNurse.getValue();
                var d = cbDay.getValue();
                var b = cbBlock.getValue();
                if (n == null || d == null || b == null) { msg.setText("Select nurse/day/block"); return; }
                svc.roster.assignNurse(n, d, b);
                msg.setText("Rostered " + n + " " + d + " " + b);
            } catch (ValidationException ex) {
                msg.setText("ERROR: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(new Label("Roster nurse"),
                new HBox(8, new Label("Nurse:"), cbNurse),
                new HBox(8, new Label("Day:"), cbDay, new Label("Block:"), cbBlock),
                btnAssign, msg);

        var t = new Tab("Roster (Nurse)", box); t.setClosable(false);
        return t;
    }

    // ---------- Doctor roster tab ----------
    private Tab tabRosterDoctor() {
        var box = new VBox(10); box.setPadding(new Insets(10));
        var cbDoc = new ComboBox<String>();
        AppContext.get().state.staff.findAll().stream()
                .filter(s -> s.getRole() == Role.DOCTOR).forEach(s -> cbDoc.getItems().add(s.getId()));
        cbDoc.setPromptText("doctorId");

        var cbDay = new ComboBox<DayOfWeek>(); cbDay.getItems().addAll(DayOfWeek.values()); cbDay.setPromptText("DAY");
        var tfTime = new TextField(); tfTime.setPromptText("HH:mm (start)");

        var btnAssign = new Button("Assign Doctor 1h");
        var msg = new Label(); msg.setStyle("-fx-text-fill:#2f6df6;");

        btnAssign.setOnAction(e -> {
            try {
                var dId = cbDoc.getValue();
                var d = cbDay.getValue();
                var t = tfTime.getText().trim();
                if (dId == null || d == null || t.isEmpty()) { msg.setText("Select doctor/day/time"); return; }
                var st = LocalTime.parse(t);
                svc.roster.assignDoctor(dId, d, st);
                msg.setText("Rostered " + dId + " " + d + " " + st + "–" + st.plusHours(1));
            } catch (Exception ex) {
                msg.setText("ERROR: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(new Label("Roster doctor (1 hour)"),
                new HBox(8, new Label("Doctor:"), cbDoc),
                new HBox(8, new Label("Day:"), cbDay, new Label("Start:"), tfTime),
                btnAssign, msg);

        var t = new Tab("Roster (Doctor)", box); t.setClosable(false);
        return t;
    }
}
