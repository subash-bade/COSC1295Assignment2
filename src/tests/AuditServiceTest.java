package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import au.edu.rmit.cosc1295.carehome.repo.AuditRepository;
import au.edu.rmit.cosc1295.carehome.service.AuditService;

public class AuditServiceTest {

    @Test
    void success_writes_one_audit_entry() throws Exception {
        var auditRepo = new AuditRepository();
        var audit = new AuditService(auditRepo);

        String out = audit.audited("S1", "TEST_SUCCESS", () -> "ok-details", () -> "RESULT");
        assertEquals("RESULT", out);
        assertEquals(1, auditRepo.all().size());
        var entry = auditRepo.all().get(0);
        assertEquals("S1", entry.staffId);
        assertEquals("TEST_SUCCESS", entry.action);
        assertTrue(entry.details.contains("ok-details"));
    }

    @Test
    void failure_writes_failed_entry_and_rethrows() {
        var auditRepo = new AuditRepository();
        var audit = new AuditService(auditRepo);

        RuntimeException boom = new RuntimeException("boom");
        var ex = assertThrows(RuntimeException.class, () ->
            audit.audited("S2", "TEST_FAIL", () -> "ctx", () -> { throw boom; })
        );

        assertSame(boom, ex);                          // same exception rethrown
        assertEquals(1, auditRepo.all().size());       // one audit entry recorded
        var entry = auditRepo.all().get(0);
        assertEquals("S2", entry.staffId);
        assertEquals("TEST_FAIL:FAILED", entry.action);
        assertTrue(entry.details.contains("ctx"));
        assertTrue(entry.details.toLowerCase().contains("runtimeexception"));
        assertTrue(entry.details.toLowerCase().contains("boom"));
    }
}
