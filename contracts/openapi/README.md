# Penatika OpenAPI Contract

This directory owns the authoritative synchronous Penatika HTTP API
contract between Teacher Web, Classroom Display Web, and the Penatika Backend.

## Baseline

- Specification family: OpenAPI 3.1.x.
- Current baseline: OpenAPI 3.1.2.
- Preferred authoring format: YAML 1.2.
- Preferred entry file: `openapi.yaml`.
- Default transport/media: HTTPS + JSON.
- HTTP error baseline: RFC 9457 Problem Details using
  `application/problem+json`.

[`openapi.yaml`](./openapi.yaml) is active as the authoritative contract root.
It currently defines shared HTTP wire primitives and the RFC 9457 Problem
Details foundation, an authenticated Teacher session bootstrap, and the
minimum operation for starting a Classroom Session from an existing
classroom-ready `LessonVersion`. It also defines role-bound PairingGrant
issuance and revocation, separate Controller and Display redemption
operations, the resulting participant browser-session boundary, and the first
deterministic revision-aware classroom command: `DIRECT_ACTION: NEXT`.
Classroom-safe projections, authoritative snapshots, and SSE remain for later
contract slices.

ADR-0011 defines the conceptual identity, authentication, account,
participant, and pairing model. ADR-0012 defines synchronous HTTP commands
plus authorized SSE projection push and reconnect semantics. Later batches
will define their operations and payloads without inventing a separate
contract authority.

## Protected Security Baseline

Teacher-protected HTTP operations use:

- the `TeacherBrowserSession` OpenAPI security scheme with the host-only
  `__Host-penatika-session` opaque backend-session cookie;
- the session-bound `X-Penatika-CSRF` header for state-changing operations,
  with CSRF material obtained from the authenticated
  `GET /api/teacher-session` bootstrap operation;
- backend object, ownership, classroom-session, participant, and revision
  authorization as applicable.

Classroom Display operations use a distinct session-scoped display participant
session and receive no teacher-account privilege. Establishing a Controller
participant requires both the authenticated Teacher boundary and a valid
role-bound PairingGrant; establishing a Display participant requires its valid
role-bound PairingGrant without Teacher authentication. Controller classroom
mutation requires both security schemes in one OpenAPI security-requirement
object, the active `TEACHER_CONTROLLER` participant authorization for the path
Classroom Session, and the existing CSRF header.

The cookie and CSRF token are distinct opaque values; neither is a business
identifier or client-supplied authorization decision. OAuth/OIDC access,
refresh, and ID tokens remain outside browser-visible application contracts.
The concrete OIDC provider is not an OpenAPI concern.

If later decomposition is justified, `openapi.yaml` remains the contract entry
point. Small contracts should not be fragmented preemptively.

See [ADR-0010](../../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md),
[ADR-0011](../../docs/02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md),
[ADR-0012](../../docs/02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md),
and the [contract index](../README.md).
