# Refactoring Report (highlights)

## 1) Introduced `AuditService`
- **Before**: scattered console prints or missing failure logs.
- **After**: single wrapper captures success/failure + context; improved traceability.
- **Why**: cross-cutting concern (logging) centralized; simpler tests.

## 2) Extracted `Services` registry
- **Before**: constructors wired per UI/CLI entry.
- **After**: one composition root; easier to inject/migrate dependencies.

## 3) CLI → GUI Atomic Actions
- **Before**: some validation only in services; UI had minimal feedback.
- **After**: try/catch surfaces exact validation messages; improves UX without leaking domain.

## 4) Persistence Migration
- **Before**: NPE when loading older saves missing new repos.
- **After**: `migrateIfNeeded` supplies defaults; no data-loss + forwards compatibility.

## 5) GUI Resident Details Pane
- **Before**: IDs shown only; nurses had to copy IDs manually.
- **After**: side pane lists Rx/orders; “Copy orderId” button; reduces error risk.

## 6) Test Coverage Improvements
- Added auditing tests, serialization round-trip, and compliance tests covering:
  - invalid nurse double-booking,
  - custom-hour nurse shift,
  - missing doctor daily coverage.