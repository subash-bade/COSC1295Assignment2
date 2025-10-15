package au.edu.rmit.cosc1295.carehome.service;

import java.time.Instant;
import java.util.function.Supplier;

import au.edu.rmit.cosc1295.carehome.repo.AuditRepository;

/** Wrap any operation so it logs success/failure to AuditRepository. */
public final class AuditService {
    private final AuditRepository audit;

    public AuditService(AuditRepository audit) { this.audit = audit; }

    @FunctionalInterface
    public interface Action<T> { T run() throws Exception; }

    public <T> T audited(String staffId, String action, Supplier<String> details, Action<T> body) throws Exception {
        try {
            T result = body.run();
            audit.append(new AuditRepository.Entry(Instant.now(), staffId, action, details.get()));
            return result;
        } catch (Exception e) {
            audit.append(new AuditRepository.Entry(Instant.now(), staffId, action + ":FAILED",
                    details.get() + " | " + e.getClass().getSimpleName() + (e.getMessage() == null ? "" : (":" + e.getMessage()))));
            throw e;
        }
    }

    /** Fire-and-forget variant for void operations. */
    public void auditedVoid(String staffId, String action, Supplier<String> details, Runnable body) throws Exception {
        audited(staffId, action, details, () -> { body.run(); return null; });
    }
}
