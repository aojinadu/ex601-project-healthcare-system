# ADR-002: Keep correction history inside VisitNote

Status: Accepted    Date: 2026-10-04

## Context
An amendment in clinical records is a separate entry that states which note it corrects. No
requirement reads a note's full revision history as one unit.

## Decision
Do not extract a NoteHistory class. Correction history stays as the `amends` reference on VisitNote,
created only through `VisitNote.amend()`. Revisit if a requirement appears to display or compare a
note's full revision history.

## Consequences
+ One source of truth for the correction chain; the append-only rule stays in one class.
- Reading a full chain means following references one note at a time.
