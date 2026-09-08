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
Contract Foundation Batch 2 defines only shared HTTP wire primitives and the
RFC 9457 Problem Details foundation. Application operations remain
intentionally absent and `paths` is empty until later batches define them.

ADR-0011 defines the conceptual identity, authentication, account,
participant, and pairing model. ADR-0012 defines synchronous HTTP commands
plus authorized SSE projection push and reconnect semantics. Later batches
will define their operations and payloads without inventing a separate
contract authority.

## Protected Security Baseline

Future teacher-protected HTTP operations will use:

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
endpoint paths remain deferred to later operation-contract batches. The
concrete OIDC provider is not an OpenAPI concern.

If later decomposition is justified, `openapi.yaml` remains the contract entry
point. Small contracts should not be fragmented preemptively.

See [ADR-0010](../../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md),
[ADR-0011](../../docs/02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md),
[ADR-0012](../../docs/02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md),
and the [contract index](../README.md).
