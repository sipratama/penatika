# Penatika Reusable JSON Schemas

This directory owns standalone reusable Penatika wire schemas using JSON
Schema Draft 2020-12.

## Baseline

- Preferred file format: JSON.
- Every standalone schema must declare the Draft 2020-12 dialect through
  `$schema`.
- A standalone schema is justified when a structure is reused by multiple
  contracts/transports, has an independent compatibility lifecycle, is
  safety-critical enough to require an explicit boundary, or is consumed
  independently of one HTTP operation.

Each wire structure must have one canonical schema owner. OpenAPI or a future
AsyncAPI document should reference a standalone schema rather than redefine
the same shape independently.

Safety-critical structured classroom or AI content may intentionally use
closed schemas that reject unknown element types or unsupported fields.
Ordinary additive API evolution may use a different explicit unknown-field
policy.

The active production schemas are:

- [`wire-primitives.schema.json`](./wire-primitives.schema.json) — canonical
  owner of `ClassroomSessionId` and `Revision`, which are shared by HTTP and
  reusable classroom projection contracts. HTTP-only concepts such as
  `LessonVersionId`, `CommandId`, CSRF material, pairing secrets, and Problem
  Details remain owned by OpenAPI.
- [`classroom-display-projection.schema.json`](./classroom-display-projection.schema.json)
  — canonical owner of the complete classroom-safe Display projection used by
  both the authoritative HTTP snapshot and each state-bearing Display SSE
  event's JSON `data` payload.

OpenAPI references these standalone owners and must not redefine their shapes.
The shared-primitives file uses only a repository-local filename `$id` so Ajv
can register its reusable definitions; it does not claim a public schema
registry or production hostname. The Display projection uses a relative
file `$ref` to that owner.
The Display projection is a closed positive allow-list: unknown fields and
unsupported block types fail validation. Projection `schemaVersion` is the
evolution boundary; because old Display clients validate strictly, adding a
field or block type is compatibility-sensitive and requires deliberate schema
version and consumer rollout planning. Browser and backend deployment must not
be assumed atomic.

See [ADR-0010](../../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
and the [contract index](../README.md).
