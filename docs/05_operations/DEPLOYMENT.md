# Deployment — Penatika

> **Status: Conditional.** Deployment architecture is intentionally not selected during project initialization.

## Why This Document Is Conditional

Penatika requires a cloud-mediated backend and provider integrations, but the cloud provider, hosting model, regions, environment topology, database, identity system, realtime transport, and secret-management solution remain open.

Creating deployment instructions now would invent architecture.

## Activation Trigger

Activate and expand this document after:

- client and backend technologies are selected;
- persistence and realtime architecture are selected;
- pilot or production environment requirements are defined;
- privacy, retention, regional, and provider constraints are reviewed;
- deployment topology is accepted through an ADR if material.

## Known Deployment Requirements

- Protected transport for production traffic.
- Server-side secret storage and least-privilege credentials.
- Support for backend-authoritative classroom sessions across different networks.
- Observability for session, provider, validation, and recovery behavior.
- Safe configuration and migration rollout.
- Ability to disable or contain failing external providers without corrupting classroom state.
- Environment separation appropriate to development, evaluation, pilot, and production maturity.

## Open Decisions

- Cloud or hosting provider.
- Region and data residency requirements.
- Compute and network topology.
- Database and backup strategy.
- Realtime infrastructure.
- Identity and secret-management integration.
- Deployment automation, rollback, and promotion workflow.
- Production availability, recovery, and cost objectives.

## Last Reviewed

`2026-09-06`
