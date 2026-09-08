# Deployment — Penatika

> **Status: Conditional.** Deployment architecture is now selected (ADR-0019), but no real deployment execution exists yet.

## Why This Document Is Conditional

The MVP/pilot deployment architecture is selected: a portable single-Linux-VPS baseline (Ubuntu Server 24.04 LTS, Docker Engine + Docker Compose, Caddy for HTTPS, colocated PostgreSQL for `PILOT`, an off-host backup boundary, and a `LOCAL`/`PILOT`/`PROD` environment model), initially on Tencent Cloud Lighthouse, Jakarta, as a replaceable provider — see [ADR-0019](../02_architecture/adr/ADR-0019-portable-linux-vps-mvp-pilot-deployment.md). Client, backend, persistence, identity, and realtime architecture are also selected (ADR-0008, ADR-0013, ADR-0014, ADR-0011, ADR-0012).

This document remains Conditional because no VPS is provisioned, no Dockerfile/Compose/Caddy configuration exists, no CI/CD pipeline exists, and no exact deployment commands can be written yet. Writing concrete deployment instructions now would invent implementation evidence that does not exist.

## Activation Trigger

Activate and expand this document with concrete, executed instructions after:

- the initial Tencent Cloud Lighthouse Jakarta VPS is provisioned;
- Dockerfile(s), `compose.yaml`, and Caddyfile exist for the backend and reverse proxy;
- the automated PostgreSQL backup and off-host destination are implemented and exercised;
- the GitHub Actions CI/CD pipeline and protected `PILOT` GitHub Environment exist;
- a domain/hostname is selected and HTTPS is activated;
- the concrete OIDC provider deployment configuration is resolved.

## Known Deployment Requirements

- Protected transport (HTTPS) for production/pilot traffic.
- Server-side secret storage and least-privilege credentials.
- Support for backend-authoritative classroom sessions across different networks.
- Observability for session, provider, validation, and recovery behavior.
- Safe configuration and migration rollout.
- Ability to disable or contain failing external providers without corrupting classroom state.
- Environment separation appropriate to `LOCAL`, `PILOT`, and future `PROD` maturity.

## Open Decisions (Implementation / Evidence, Not Architecture)

- Exact Tencent commercial SKU, price, and real domain/hostname.
- Dockerfile, `compose.yaml`, and Caddyfile contents.
- Off-host backup tool, object-storage vendor, schedule, and RPO/RTO.
- GitHub Actions workflow files and deployment SSH identity provisioning.
- Concrete OIDC provider deployment configuration.
- Production (`PROD`) availability, recovery, and cost objectives, which are not defined by the MVP/pilot baseline.

## Last Reviewed

`2026-09-08`
