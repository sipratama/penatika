# Release Checklist — Penatika

> **Status: Conditional.** This baseline becomes active when an executable build and target release environment exist.

## Activation Trigger

Activate before distributing an MVP build to users or deploying a classroom pilot.

## Product and Requirements

- Required PRD capabilities and feature acceptance criteria are satisfied.
- Product owner and release approver are identified.
- Open product decisions that block the release are resolved.
- Release scope and known limitations are communicated.

## Architecture and Contracts

- Implementation follows architecture invariants and accepted ADRs.
- Cross-component contracts match implementation and supported versions.
- Physical schema changes have reviewed migrations and rollout strategy.
- No unapproved material architecture change exists only in code.

## Security and Privacy

- Authorization, pairing, projection, injection, prompt-injection, and secret tests pass.
- Raw audio non-retention is verified.
- Provider data use and retention are reviewed.
- Retention, deletion, export, and incident behavior are approved for the release context.
- No critical threat or release blocker remains unresolved.

## Quality and Classroom Readiness

- Mathematics validation corpus passes for supported scope.
- Curriculum authority level, source/version, phase/scope, provenance, and saved-lesson historical stability are verified.
- Multi-device reconnect and degraded-mode scenarios pass.
- Target device, browser, display, input, and accessibility evidence exists.
- AI evaluation results meet the release threshold selected before testing.

## Operations

- Configuration and secrets are managed safely.
- Deployment, rollback, health checks, dashboards, alerts, and runbooks are ready for the release maturity.
- Database backup and recovery are verified if persistent production data exists.
- Support and incident owners are identified.

## Evidence

Record exact version, commit, environment, executed commands, results, unresolved items, approvers, and rollback plan. Do not mark an item complete without evidence.

## Open Decisions

- Release versioning and changelog policy.
- CI/CD platform and required checks.
- Pilot support and operational release process.
- Product Stage B go/no-go authority is already defined by `PRODUCT_GOVERNANCE.md` and `PILOT_PLAN.md`.
- Quantitative AI, reliability, performance, and accessibility thresholds.

## Last Reviewed

`2026-09-06`
