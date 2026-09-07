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

No OpenAPI document exists yet because field-level endpoints and protected
security semantics remain incomplete. OAD-004 must define identity,
authentication, and account semantics before final protected endpoint
security definitions are created.

If later decomposition is justified, `openapi.yaml` remains the contract entry
point. Small contracts should not be fragmented preemptively.

See [ADR-0010](../../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
and the [contract index](../README.md).

