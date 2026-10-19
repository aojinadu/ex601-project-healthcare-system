# ADR-001: Proxy bookings hold the slot until the patient approves

Status: Accepted    Date: 2026-10-18

## Context
Representatives (family members, caregivers) book for patients who struggle to book themselves.
A booking made by someone else must not silently commit the patient, but the slot also must not be
taken by another patient while the request waits.

## Decision
A proxy request creates an Appointment in PENDING_APPROVAL and moves the AvailabilitySlot to HELD.
Only the patient can approve (slot becomes BOOKED) or decline (slot returns to OPEN). The slot owns
its own state transitions (Information Expert); the controller only checks the ProxyAuthorization.

## Consequences
+ No double booking while a request is pending; the patient keeps the final say.
- A slot can sit HELD if the patient never responds; an expiry rule is deferred to Checkpoint 2.
