# Penatika OpenAPI Contract

This directory will own the authoritative synchronous Penatika HTTP API
contract between Teacher Web, Classroom Display Web, and the Penatika Backend.

## Baseline

- Specification family: OpenAPI 3.1.x.
- Current baseline: OpenAPI 3.1.2.
- Preferred authoring format: YAML 1.2.
- Preferred entry file: `openapi.yaml`.
- Default transport/media: HTTPS + JSON.
- HTTP error baseline: RFC 9457 Problem Details using
  `application/problem+json`.

No OpenAPI document exists yet because field-level endpoints and realtime
semantics remain incomplete. OAD-004 now defines the conceptual identity,
authentication, account, participant, and pairing model, but this task does not
invent endpoint paths, security-scheme names, cookie names, CSRF header names,
or field schemas.

## Protected Security Baseline

Future teacher-protected HTTP operations use:

- a backend-managed opaque teacher browser session cookie;
- explicit CSRF protection for state-changing operations;
- backend object, ownership, classroom-session, participant, and revision
  authorization as applicable.

Classroom Display operations use a distinct session-scoped display participant
session and receive no teacher-account privilege. Controller classroom
mutation requires both an authenticated teacher browser session and the active
`TEACHER_CONTROLLER` participant authorization for the classroom session.

Future OpenAPI contracts must document the applicable cookie security scheme
and mutation anti-CSRF requirement without exposing OAuth/OIDC access, refresh,
or ID tokens to browser clients. Exact cookie names, CSRF header names, and
endpoint paths remain deferred to field-level contract creation. The concrete
OIDC provider is not an OpenAPI concern.

If later decomposition is justified, `openapi.yaml` remains the contract entry
point. Small contracts should not be fragmented preemptively.

See [ADR-0010](../../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md),
[ADR-0011](../../docs/02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md),
and the [contract index](../README.md).
