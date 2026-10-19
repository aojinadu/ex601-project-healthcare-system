# The A's Health Record System

Boston University OMSE EX601 course project (Aleksei Glebov, Ayotola Jinadu).

Our system helps patients, their trusted representatives, and clinicians book appointments and keep
visit notes in one place, so that scheduling is conflict-free and every clinical record stays accountable.

## Build and run

Requires JDK 17+ and Maven 3.9+.

```bash
mvn clean package                                   # compiles and runs the JUnit 5 tests
java -jar target/health-record-system-0.1.0.jar     # runs the Checkpoint 1 console walkthrough
```

The console walkthrough publishes a slot, has a representative request it for a patient, has the
patient approve it, then records, finalizes, and amends a visit note.

## Layout

| Layer | Package | Contents |
|---|---|---|
| Presentation | `edu.bu.ex601.health.presentation` | Use-case controllers, console entry point |
| Domain | `edu.bu.ex601.health.domain` | Entities, enums, repository and audit interfaces |
| Data | `edu.bu.ex601.health.data` | In-memory repository implementations (JDBC/H2 in Checkpoint 2) |

The domain package imports nothing from the other two layers.

## Workflow

See [CONTRIBUTING.md](CONTRIBUTING.md). Design decisions live in [docs/adr](docs/adr).
