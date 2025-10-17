package au.edu.rmit.cosc1295.carehome.archive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import au.edu.rmit.cosc1295.carehome.app.CareHomeState;
import au.edu.rmit.cosc1295.carehome.model.*;
import au.edu.rmit.cosc1295.carehome.repo.*;

public final class ArchiveService {

    private final CareHomeState state;

    public ArchiveService(CareHomeState state) {
        this.state = state;
    }

    /** Discharge resident: set status, vacate bed (if any), and write snapshot bundle to /archive/<residentId>-<timestamp>/ */
    public Path dischargeAndArchive(String residentId) throws IOException {
        var resident = state.residents.findById(residentId).orElseThrow(() ->
                new IllegalArgumentException("Resident not found: " + residentId));

        // 1) set status
        resident.setStatus(ResidentStatus.DISCHARGED);
        state.residents.save(resident);

        // 2) vacate bed if occupied
        state.beds.findByResidentId(residentId).ifPresent(b -> {
            b.vacate();
            state.beds.save(b);
        });

        // 3) snapshot
        var dir = makeArchiveDir(residentId);
        writeResidentJson(dir.resolve("resident.json"), resident);
        writePrescriptionsJson(dir.resolve("prescriptions.json"), state.prescriptions.findByResident(residentId));
        writeAdministrationsJson(dir.resolve("administrations.json"),
                state.administrations.findAll().stream().filter(a -> residentId.equals(a.getResidentId())).toList());
        writeAuditCsv(dir.resolve("audit.csv"), state.audit, residentId);

        return dir;
    }

    // --------------------- helpers ---------------------

    private static Path makeArchiveDir(String residentId) throws IOException {
        var ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        var dir = Path.of("archive", residentId + "-" + ts);
        Files.createDirectories(dir);
        return dir;
    }

    private static void writeResidentJson(Path path, Resident r) throws IOException {
        String json = """
            {
              "id": "%s",
              "name": "%s",
              "gender": "%s",
              "dateOfBirth": "%s",
              "status": "%s"
            }
            """.formatted(esc(r.getId()), esc(r.getName()), r.getGender(), r.getDateOfBirth(), r.getStatus());
        Files.writeString(path, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void writePrescriptionsJson(Path path, List<Prescription> list) throws IOException {
        var sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            var p = list.get(i);
            sb.append("  {\"id\":\"").append(esc(p.getId())).append("\",")
              .append("\"residentId\":\"").append(esc(p.getResidentId())).append("\",")
              .append("\"doctorId\":\"").append(esc(p.getDoctorId())).append("\",")
              .append("\"date\":\"").append(p.getDate()).append("\",")
              .append("\"orders\":[");
            for (int j = 0; j < p.getOrders().size(); j++) {
                var o = p.getOrders().get(j);
                sb.append("{\"id\":\"").append(esc(o.getId())).append("\",")
                  .append("\"drug\":\"").append(esc(o.getDrug())).append("\",")
                  .append("\"dose\":\"").append(esc(o.getDose())).append("\",")
                  .append("\"route\":\"").append(esc(o.getRoute())).append("\",")
                  .append("\"times\":[");
                for (int k = 0; k < o.getScheduleTimes().size(); k++) {
                    sb.append("\"").append(o.getScheduleTimes().get(k)).append("\"");
                    if (k < o.getScheduleTimes().size() - 1) sb.append(",");
                }
                sb.append("]}");
                if (j < p.getOrders().size() - 1) sb.append(",");
            }
            sb.append("]}");
            if (i < list.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void writeAdministrationsJson(Path path, List<AdministrationRecord> list) throws IOException {
        var sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < list.size(); i++) {
            var a = list.get(i);
            sb.append("  {\"id\":\"").append(esc(a.getId())).append("\",")
              .append("\"residentId\":\"").append(esc(a.getResidentId())).append("\",")
              .append("\"medOrderId\":\"").append(esc(a.getMedOrderId())).append("\",")
              .append("\"time\":\"").append(a.getTime()).append("\",")
              .append("\"nurseId\":\"").append(esc(a.getNurseId())).append("\",")
              .append("\"doseGiven\":\"").append(esc(a.getDoseGiven())).append("\",")
              .append("\"notes\":\"").append(esc(a.getNotes())).append("\"")
              .append("}");
            if (i < list.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void writeAuditCsv(Path path, AuditRepository auditRepo, String residentId) throws IOException {
        var sb = new StringBuilder();
        sb.append("timestamp,staffId,action,details\n");
        for (var e : auditRepo.all()) {
            // simple heuristic: keep entries whose details mention the resident id
            if (e.details != null && e.details.contains(residentId)) {
                sb.append(escCsv(e.ts.toString())).append(',')
                  .append(escCsv(e.staffId)).append(',')
                  .append(escCsv(e.action)).append(',')
                  .append(escCsv(e.details)).append('\n');
            }
        }
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escCsv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n");
        String t = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + t + "\"" : t;
    }
    
    /** Return the most recent archive directory for this resident, if any. */
    public static java.util.Optional<Path> findLatestSnapshot(String residentId) throws IOException {
        var root = Path.of("archive");
        if (!java.nio.file.Files.exists(root)) return java.util.Optional.empty();

        try (var stream = java.nio.file.Files.list(root)) {
            return stream
                .filter(p -> java.nio.file.Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().startsWith(residentId + "-"))
                .max(java.util.Comparator.comparing(p -> p.getFileName().toString()));
        }
    }
    
    /** Convenience: pretty print a path or "(none)". */
    public static String latestSnapshotPathOrNone(String residentId) {
        try {
            return findLatestSnapshot(residentId).map(Path::toString).orElse("(none)");
        } catch (IOException e) {
            return "(error: " + e.getMessage() + ")";
        }
    }
}
