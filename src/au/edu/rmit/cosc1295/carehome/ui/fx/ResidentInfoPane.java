package au.edu.rmit.cosc1295.carehome.ui.fx;

import java.time.format.DateTimeFormatter;

import au.edu.rmit.cosc1295.carehome.app.AppContext;
import au.edu.rmit.cosc1295.carehome.model.Prescription;
import au.edu.rmit.cosc1295.carehome.model.Resident;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

public final class ResidentInfoPane extends VBox {

    private final Label title = new Label("Resident");
    private final Label name  = new Label("-");
    private final Label dob   = new Label("-");
    private final Label gender= new Label("-");
    private final VBox  ordersBox = new VBox(6);

    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private String currentResidentId = null;

    public ResidentInfoPane() {
        super(8);
        setPadding(new Insets(10));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        getChildren().addAll(title, new Separator(), name, dob, gender, new Separator(),
                new Label("Prescriptions"), ordersBox);
        clear();
    }

    public void clear() {
        currentResidentId = null;
        name.setText("Name: -");
        dob.setText("DOB: -");
        gender.setText("Gender: -");
        ordersBox.getChildren().setAll(new Label("(no resident selected)"));
    }

    public void showResident(String residentId) {
        var resOpt = AppContext.get().state.residents.findById(residentId);
        if (resOpt.isEmpty()) { clear(); return; }
        currentResidentId = residentId;

        Resident r = resOpt.get();
        name.setText("Name: " + r.getName());
        dob.setText("DOB: " + (r.getDateOfBirth() == null ? "-" : DOB_FMT.format(r.getDateOfBirth())));
        gender.setText("Gender: " + r.getGender());

        var list = AppContext.get().state.prescriptions.findByResident(residentId);
        ordersBox.getChildren().clear();
        if (list.isEmpty()) {
            ordersBox.getChildren().add(new Label("(no prescriptions)"));
            return;
        }

        for (Prescription p : list) {
            var rxHdr = new Label("Rx " + p.getId() + "  •  Doctor " + p.getDoctorId() + "  •  " + p.getDate());
            rxHdr.setStyle("-fx-font-weight: bold;");
            ordersBox.getChildren().add(rxHdr);
            for (var o : p.getOrders()) {
                var line = new Label("  • " + o.getDrug() + " " + o.getDose() + " " + o.getRoute()
                        + "  times=" + o.getScheduleTimes() + "   (orderId=" + o.getId() + ")");
                var copy = new Button("Copy orderId");
                copy.setOnAction(e -> {
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(o.getId());
                    Clipboard.getSystemClipboard().setContent(cc);
                });
                var box = new VBox(4, line, copy);
                box.setStyle("-fx-background-color:#f5f6f7; -fx-padding:6; -fx-background-radius:6;");
                ordersBox.getChildren().add(box);
            }
        }
    }

    public String getCurrentResidentId() {
        return currentResidentId;
    }
}
