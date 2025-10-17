# COSC1295Assignment2  — Care Home System
 COSC1295 Assignment 2: Java app with nurse/doctor rostering, bed allocation, prescriptions & meds admin, audit log, save/load state, JavaFX UI.


 Java implementation of a small care home app with:
- Nurse/Doctor **rostering** (with compliance: nurse blocks 08–16 or 14–22; max 1 block/day; ≥1h doctor daily)
- **Beds** (admit/move residents atomically)
- **Prescriptions** (doctor-only, roster-gated)
- **Medication administrations** (nurse-only, roster-gated)
- **Audit log** of all actions
- **Persistence** via serialization to `data/carehome-state.bin`
- Simple **CLI**; JavaFX optional later

## Requirements
- JDK 17+ (modules enabled)
- Eclipse (JUnit 5 library added)
- (Optional) JavaFX for a future GUI

## How to run
1. Open in Eclipse → run `au.edu.rmit.cosc1295.carehome.app.Main`
2. Use CLI commands below.

## GitHub Repo
https://github.com/subash-bade/COSC1295Assignment2.git

## CLI Commands
help
exit
save # save state immediately

seed # demo ward/rooms/beds + one resident
listbeds # show all beds and occupancy
listres # list residents (id, name, gender, dob)
liststaff # list staff (id, role, name, username)

assign <residentId> <bedId> # place resident in vacant bed
move <fromBedId> <toBedId> # move resident between beds

makedoctor <id> <name> <user> # create a doctor
makenurse <id> <name> <user> # create a nurse
rosternurse <nurseId> <DAY> <MORNING|AFTERNOON>
rosterdoc <docId> <DAY> HH:mm
 # exactly 1 hour coverage at start time

addrx <docId> <residentId> <drug> <dose> <route> [DAY HH:mm]

Adds a prescription. If DAY HH:mm provided, uses that moment for roster check; else now.

listorders <residentId> # list prescriptions & medication orders

admin <nurseId> <residentId> <medOrderId> <doseGiven> [notes...] [DAY HH:mm]

Records an administration at the specified moment (or now).

audit # show audit log
compliance # run roster compliance checks

discharge <residentId>          # Discharge resident and export snapshot to /archive/<residentId>-<timestamp>/

openarchive <residentId>        # prints path of most recent snapshot for that resident


## Typical Demo flow
seed
makedoctor D1 DrDee drd
makenurse N2 NurseNina nn
rosterdoc D1 MONDAY 10:00
rosternurse N2 MONDAY MORNING

listres
listbeds

addrx D1 <residentId> paracetamol 500mg PO MONDAY 10:30
listorders <residentId>

admin N2 <residentId> <orderId> 500mg ok MONDAY 09:00

audit
compliance
save
exit

## Project layout
- `au.edu.rmit.cosc1295.carehome.model` — domain entities
- `au.edu.rmit.cosc1295.carehome.repo` — in-memory repositories (Serializable)
- `au.edu.rmit.cosc1295.carehome.service` — business logic, auth, auditing, compliance
- `au.edu.rmit.cosc1295.carehome.app` — `Main`, state, serializer, services registry
- `au.edu.rmit.cosc1295.carehome.ui` — CLI

## Notes
- If you see a NullPointerException on startup after pulling new code, delete `data/carehome-state.bin` (old schema) or rely on the built-in migration in `AppContext`.
- JUnit 5 is configured via module path: `requires org.junit.jupiter.api;` in `module-info.java`.