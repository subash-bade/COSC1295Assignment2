```markdown
# Design Outline

## Architecture
- **Model**: Residents, Staff (Manager/Doctor/Nurse), Beds/Rooms/Wards, Roster, Prescriptions, Medication Orders, Administration Records.
- **Repo layer**: In-memory repositories (Serializable) for each aggregate.
- **Service layer**:
  - `AuthService`: role + roster checks (time-based).
  - `RosterService`: assign nurses to [MORNING|AFTERNOON] blocks; doctors to 1-hour slots.
  - `BedService`: assign/move with atomic validations.
  - `PrescriptionService`: doctor-only, roster-gated.
  - `MedicationService`: nurse-only, roster-gated.
  - `AuditService`: logs success/failure for all actions.
  - `CareHome`: `checkCompliance()` enforcing: nurse block constraints; 1hr doctor/day.

## Persistence
- `StateSerializer` serializes `CareHomeState` to `data/carehome-state.bin`.
- Backward-compatibility via `AppContext.migrateIfNeeded`.

## GUI
- JavaFX (no FXML). Screens: Login → Ward map.
- **Ward map**: tile buttons per bed (gender-colored), left action panel, right **Resident Info** panel.
- Top bar: **Compliance**, **Audit**, **Save**, **Roster/Staff**, **Discharge Selected**.
- Roster/Staff Dialog: add staff; roster nurses by day/block; roster doctors 1-hour.
- Actions use `AuditService` wrappers and throw on authorization/roster failures.

## Key Design Decisions
- **Auditing wrapper** ensures both success and failure get recorded, centralizing logging.
- **Roster as data**: stored per DayOfWeek to simplify lookups and compliance checks.
- **Serialization over DB**: keeps setup simple; meets assignment persistence requirement.
- **Optional time in UI/CLI** for doctor/nurse actions to make roster checks deterministic for demos.