# ADR-003: In-memory repositories behind domain interfaces for Checkpoint 1

Status: Accepted    Date: 2026-10-18

## Context
Both slices need persistence, but Checkpoint 1 is about a merged design and a working baseline.
Picking a database now would pull schema work into a design checkpoint.

## Decision
The domain declares repository and AuditLog interfaces; the data layer provides in-memory
implementations now. JDBC implementations backed by H2 replace them in Checkpoint 2 without any
domain change (Dependency Inversion).

## Consequences
+ The full flow runs and is unit-testable today with no setup.
- Data is lost when the program exits until the H2 implementations land.
