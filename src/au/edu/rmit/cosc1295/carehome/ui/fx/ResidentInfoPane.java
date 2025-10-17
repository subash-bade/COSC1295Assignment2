package au.edu.rmit.cosc1295.carehome.ui.fx;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Comparator;

import au.edu.rmit.cosc1295.carehome.app.AppContext;
import au.edu.rmit.cosc1295.carehome.app.Services;
import au.edu.rmit.cosc1295.carehome.app.StateSerializer;
import au.edu.rmit.cosc1295.carehome.archive.ArchiveService;
import au.edu.rmit.cosc1295.carehome.auth.Role;
import au.edu.rmit.cosc1295.carehome.model.Bed;
import au.edu.rmit.cosc1295.carehome.model.Doctor;
import au.edu.rmit.cosc1295.carehome.model.Gender;
import au.edu.rmit.cosc1295.carehome.model.Manager;
import au.edu.rmit.cosc1295.carehome.model.MedicationOrder;
import au.edu.rmit.cosc1295.carehome.model.Nurse;
import au.edu.rmit.cosc1295.carehome.model.Prescription;
import au.edu.rmit.cosc1295.carehome.model.Resident;
import au.edu.rmit.cosc1295.carehome.model.Room;
import au.edu.rmit.cosc1295.carehome.model.Ward;
import au.edu.rmit.cosc1295.carehome.util.Ids;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class FxApp extends Application {

    private Services svc;

    @Override
    public void start(Stage stage) {
        var ctx = AppContext.loadOrInit();
        this.svc = new Services(ctx.state);

        stage.setTitle("CareHome (Phase 2)");
        stage.setScene(loginScene(stage));
        stage.setWidth(980);
        stage.setHeight(640);
        stage.show();
    }

    // ----------------- Login Scene -----------------
    private Scene loginScene(Stage stage) {
        var root = new VBox(12);
        root.setPadding(new Insets(24));

        var title = new Label("Login");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        var userBox = new HBox(8);
        var tfUser = new TextField();
        tfUser.setPromptText("username (e.g. dd or nn)");
        var comboRole = new ComboBox<Role>();
        comboRole.getItems().addAll(Role.MANAGER, Role.DOCTOR, Role.NURSE);
        comboRole.setPromptText("Role");

        var btnLogin = new Button("Login");
        userBox.getChildren().addAll(new Label("User:"), tfUser, new Label("Role:"), comboRole, btnLogin);
        userBox.setAlignment(Pos.CENTER_LEFT);

        var btnSeed = new Button("Seed demo data");
        btnSeed.setOnAction(e -> seedDemo());

        var lblMsg = new Label();
        lblMsg.setStyle("-fx-text-fill: #e74c3c;");

        btnLogin.setOnAction(e -> {
            var user = tfUser.getText() == null ? "" : tfUser.getText().trim();
            var role = comboRole.getValue();
            if (user.isEmpty() || role == null) {
                lblMsg.setText("Please enter username and select role.");
                return;
            }
            var staff = AppContext.get().state.staff.findAll().stream()
                    .filter(s -> role == s.getRole() && user.equalsIgnoreCase(s.getUsername()))
                    .findFirst();
            if (staff.isEmpty()) {
                lblMsg.setText("No staff found with that username/role. Create in CLI or seed demo.");
                return;
            }
            lblMsg.setText("");
            var scene = wardScene(stage, staff.get().getId(), staff.get().getRole());
            stage.setScene(scene);
        });

        root.getChildren().addAll(title, userBox, btnSeed, new Separator(), new Label("Tip: add staff in CLI or seed here"), lblMsg);
        return new Scene(root);
    }

    private void seedDemo() {
        var ctx = AppContext.get();
        if (ctx.state.wards.size() == 0) {
            var ward = new Ward("W1");
            var r1 = new Room("W1-R1");
            r1.getBeds().add(new Bed("W1-R1-B1", "Bed 1"));
            r1.getBeds().add(new Bed("W1-R1-B2", "Bed 2"));
            var r2 = new Room("W1-R2");
            r2.getBeds().add(new Bed("W1-R2-B1", "Bed 1"));
            r2.getBeds().add(new Bed("W1-R2-B2", "Bed 2"));
            ward.getRooms().addAll(java.util.List.of(r1, r2));
            ctx.state.wards.save(ward);
            for (var room : ward.getRooms()) for (var bed : room.getBeds()) ctx.state.beds.save(bed);
        }
        if (ctx.state.residents.size() == 0) {
            ctx.state.residents.save(new Resident(Ids.newId(), "Alice Example", Gender.FEMALE, LocalDate.of(1950,1,1)));
            ctx.state.residents.save(new Resident(Ids.newId(), "Bob Example", Gender.MALE, LocalDate.of(1949,3,2)));
        }
        if (ctx.state.staff.size() == 0) {
            ctx.state.staff.save(new Manager("M1", "Manager Mo", "mm"));
            ctx.state.staff.save(new Doctor("D1", "Dr Dee", "dd"));
            ctx.state.staff.save(new Nurse("N1", "Nurse Nina", "nn"));
        }
    }

    // ----------------- Ward Scene -----------------
    private Scene wardScene(Stage stage, String staffId, Role role) {
        var root = new BorderPane();

        // Top bar
        var top = new HBox(8);
        top.setPadding(new Insets(10));
        var lblRole = new Label("Logged in as " + role + " (" + staffId + ")");
        lblRole.setStyle("-fx-font-weight: bold;");
        var btnCompliance = new Button("Compliance");
        var btnAudit = new Button("Audit (console)");
        var btnSave = new Button("Save");
        var btnRoster = new Button("Roster/Staff");
        var btnDischarge = new Button("Discharge Selected");

        btnCompliance.setOnAction(e -> {
            try {
                svc.careHome.checkCompliance();
                alertInfo("Compliance", "All good ✅");
            } catch (Exception ex) {
                alertError("Compliance violation", ex.getMessage());
            }
        });
        btnAudit.setOnAction(e -> {
            AppContext.get().state.audit.all().forEach(a ->
                System.out.println(a.ts + " | " + a.staffId + " | " + a.action + " | " + a.details));
            alertInfo("Audit", "Printed to console.");
        });
        btnSave.setOnAction(e -> {
            try {
                StateSerializer.save(AppContext.get().state, AppContext.get().saveFile);
                alertInfo("Save", "State saved to " + AppContext.get().saveFile);
            } catch (Exception ex) {
                alertError("Save failed", ex.getMessage());
            }
        });
        btnRoster.setOnAction(e -> new RosterManagerDialog(stage, svc).showAndWait());
        // btnDischarge handler is set AFTER we create the info pane (needs it)

        top.getChildren().addAll(lblRole, new Separator(), btnCompliance, btnAudit, btnSave, btnRoster, btnDischarge);

        // Left actions (contextual by role)
        var left = new VBox(10);
        left.setPadding(new Insets(10));
        left.getChildren().add(new Label("Actions"));

        // Common: Assign / Move
        var tfResident = new TextField(); tfResident.setPromptText("residentId");
        var tfBed = new TextField(); tfBed.setPromptText("bedId");
        var btnAssign = new Button("Assign Resident → Bed");
        btnAssign.setOnAction(e -> tryAssign(tfResident.getText(), tfBed.getText(), staffId));

        var tfFrom = new TextField(); tfFrom.setPromptText("fromBedId");
        var tfTo = new TextField(); tfTo.setPromptText("toBedId");
        var btnMove = new Button("Move Resident (from → to)");
        btnMove.setOnAction(e -> tryMove(tfFrom.getText(), tfTo.getText(), staffId));

        left.getChildren().addAll(new Separator(), tfResident, tfBed, btnAssign, new Separator(), tfFrom, tfTo, btnMove);

        // Doctor-only: Add Rx
        if (role == Role.DOCTOR) {
            var rxTitle = new Label("Doctor");
            rxTitle.setStyle("-fx-font-weight: bold;");
            var tfDrug = new TextField(); tfDrug.setPromptText("drug (e.g. Paracetamol)");
            var tfDose = new TextField(); tfDose.setPromptText("dose (e.g. 500mg)");
            var tfRoute = new TextField(); tfRoute.setPromptText("route (e.g. PO)");
            var dpDay = new ComboBox<DayOfWeek>(); dpDay.getItems().addAll(DayOfWeek.values());
            dpDay.setPromptText("DAY for roster check");
            var tfTime = new TextField(); tfTime.setPromptText("HH:mm");
            var btnRx = new Button("Add Prescription");
            btnRx.setOnAction(e -> tryAddRx(staffId, tfResident.getText(), tfDrug.getText(), tfDose.getText(), tfRoute.getText(), dpDay.getValue(), tfTime.getText()));
            left.getChildren().addAll(new Separator(), rxTitle, tfDrug, tfDose, tfRoute, dpDay, tfTime, btnRx);
        }

        // Nurse-only: Administer
        if (role == Role.NURSE) {
            var nTitle = new Label("Nurse");
            nTitle.setStyle("-fx-font-weight: bold;");
            var tfOrder = new TextField(); tfOrder.setPromptText("medOrderId");
            var tfGiven = new TextField(); tfGiven.setPromptText("doseGiven");
            var tfNotes = new TextField(); tfNotes.setPromptText("notes");
            var dpDay = new ComboBox<DayOfWeek>(); dpDay.getItems().addAll(DayOfWeek.values());
            dpDay.setPromptText("DAY for roster check");
            var tfTime = new TextField(); tfTime.setPromptText("HH:mm");
            var btnAdmin = new Button("Record Administration");
            btnAdmin.setOnAction(e -> tryAdmin(staffId, tfResident.getText(), tfOrder.getText(), tfGiven.getText(), tfNotes.getText(), dpDay.getValue(), tfTime.getText()));
            left.getChildren().addAll(new Separator(), nTitle, tfOrder, tfGiven, tfNotes, dpDay, tfTime, btnAdmin);
        }

        // Center: simple ward map grid
        var center = new TilePane();
        center.setPadding(new Insets(10));
        center.setHgap(10);
        center.setVgap(10);

        var beds = AppContext.get().state.beds.findAll();
        beds.sort(Comparator.comparing(Bed::getId));

        // Right: resident details pane
        var info = new ResidentInfoPane();
        info.setMinWidth(320);

        // Now that 'info' exists, wire Discharge button
        btnDischarge.setOnAction(e -> {
            var rid = info.getCurrentResidentId();
            if (rid == null || rid.isBlank()) {
                alertWarn("Discharge", "Select an occupied bed first.");
                return;
            }
            try {
                final String staffIdFinal = staffId;
                final String residentIdFinal = rid;

                var path = svc.audit.audited(staffIdFinal, "DISCHARGE",
                        () -> "resident=" + residentIdFinal,
                        () -> {
                            var arch = new ArchiveService(AppContext.get().state);
                            return arch.dischargeAndArchive(residentIdFinal);
                        });
                alertInfo("Discharge", "Resident discharged.\nArchive: " + path);
                info.clear(); // bed vacated, clear panel
            } catch (Exception ex) {
                alertError("Discharge failed", ex.getMessage());
            }
        });

        for (var b : beds) {
            var btn = new Button(b.getId() + "\n" + b.getResidentId().orElse("(vacant)"));
            btn.setMinSize(140, 70);
            styleBedButton(btn, b);
            btn.setOnAction(e -> {
                // quick fill fields when clicked
                tfBed.setText(b.getId());
                tfFrom.setText(b.getId());

                b.getResidentId().ifPresentOrElse(
                    info::showResident,
                    info::clear
                );
            });
            center.getChildren().add(btn);
        }

        var centerScroll = new ScrollPane(center);
        centerScroll.setFitToWidth(true);
        centerScroll.setFitToHeight(true);

        root.setTop(top);
        root.setLeft(left);
        root.setCenter(centerScroll);
        root.setRight(info);
        BorderPane.setMargin(info, new Insets(10));

        return new Scene(root);
    }

    private void styleBedButton(Button btn, Bed bed) {
        String color = "#aaaaaa"; // vacant
        if (bed.getResidentId().isPresent()) {
            // try color by gender
            var resId = bed.getResidentId().get();
            var res = AppContext.get().state.residents.findById(resId).orElse(null);
            if (res != null) {
                color = switch (res.getGender()) {
                    case MALE -> "#2f6df6";
                    case FEMALE -> "#e94362";
                    default -> "#7f8c8d";
                };
            } else color = "#7f8c8d";
        }
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    // ----------------- Action handlers -----------------
    private void tryAssign(String residentId, String bedId, String staffId) {
        if (residentId.isBlank() || bedId.isBlank()) { alertWarn("Assign", "Enter residentId and bedId"); return; }
        try {
            svc.audit.auditedVoid(staffId, "BED_ASSIGN", () -> "res=" + residentId + ",bed=" + bedId,
                    () -> {
                        try { svc.beds.addResidentToVacantBed(residentId, bedId); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    });
            alertInfo("Assign", "Assigned.");
        } catch (Exception ex) {
            alertError("Assign failed", ex.getMessage());
        }
    }

    private void tryMove(String fromBed, String toBed, String staffId) {
        if (fromBed.isBlank() || toBed.isBlank()) { alertWarn("Move", "Enter fromBedId and toBedId"); return; }
        try {
            svc.audit.auditedVoid(staffId, "BED_MOVE", () -> "from=" + fromBed + ",to=" + toBed,
                    () -> {
                        try { svc.beds.moveResident(fromBed, toBed); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    });
            alertInfo("Move", "Moved.");
        } catch (Exception ex) {
            alertError("Move failed", ex.getMessage());
        }
    }

    private void tryAddRx(String docId, String residentId, String drug, String dose, String route,
                          DayOfWeek day, String hhmm) {
        if (residentId.isBlank() || drug.isBlank() || dose.isBlank() || route.isBlank()) {
            alertWarn("Add Rx", "Fill residentId, drug, dose, route");
            return;
        }

        // temp → final for lambda capture
        Instant tempWhen = Instant.now();
        if (day != null && hhmm != null && !hhmm.isBlank()) {
            try {
                var t = LocalTime.parse(hhmm);
                tempWhen = ZonedDateTime.now()
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                        .withHour(t.getHour()).withMinute(t.getMinute())
                        .withSecond(0).withNano(0)
                        .toInstant();
            } catch (Exception ex) {
                alertWarn("Add Rx", "Invalid time format HH:mm");
                return;
            }
        }
        final Instant when = tempWhen;

        var order = new MedicationOrder(null, drug, dose, route,
                java.util.List.of(LocalTime.of(8,0), LocalTime.of(20,0)));
        var rx = new Prescription(null, residentId, docId, LocalDate.now(),
                java.util.List.of(order));

        try {
            var rxId = svc.audit.audited(docId, "RX_ADD",
                    () -> "resident=" + residentId + ",drug=" + drug,
                    () -> svc.prescriptions.addPrescription(docId, residentId, rx, when));
            alertInfo("Add Rx", "Prescription added.\nrx=" + rxId + "\norder=" + order.getId());
        } catch (Exception ex) {
            alertError("Add Rx failed", ex.getMessage());
        }
    }

    private void tryAdmin(String nurseId, String residentId, String orderId,
                          String doseGiven, String notes, DayOfWeek day, String hhmm) {
        if (residentId.isBlank() || orderId.isBlank() || doseGiven.isBlank()) {
            alertWarn("Admin", "Fill residentId, medOrderId, doseGiven");
            return;
        }

        // temp → final for lambda capture
        Instant tempWhen = Instant.now();
        if (day != null && hhmm != null && !hhmm.isBlank()) {
            try {
                var t = LocalTime.parse(hhmm);
                tempWhen = ZonedDateTime.now()
                        .with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
                        .withHour(t.getHour()).withMinute(t.getMinute())
                        .withSecond(0).withNano(0)
                        .toInstant();
            } catch (Exception ex) {
                alertWarn("Admin", "Invalid time format HH:mm");
                return;
            }
        }
        final Instant when = tempWhen;

        try {
            var adminId = svc.audit.audited(nurseId, "ADMIN_RECORD",
                    () -> "resident=" + residentId + ",order=" + orderId,
                    () -> svc.medications.recordAdministration(nurseId, residentId, orderId, when, doseGiven, notes));
            alertInfo("Admin", "Administration recorded.\nid=" + adminId);
        } catch (Exception ex) {
            alertError("Admin failed", ex.getMessage());
        }
    }

    // ----------------- Alerts -----------------
    private void alertInfo(String title, String msg) { showAlert(Alert.AlertType.INFORMATION, title, msg); }
    private void alertWarn(String title, String msg) { showAlert(Alert.AlertType.WARNING, title, msg); }
    private void alertError(String title, String msg) { showAlert(Alert.AlertType.ERROR, title, msg); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        var a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg == null ? "" : msg);
        a.showAndWait();
    }
}
