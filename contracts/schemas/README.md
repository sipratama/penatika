# Penatika Reusable JSON Schemas

This directory will own standalone reusable Penatika wire schemas using JSON
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

No field-level JSON Schemas exist yet.

See [ADR-0010](../../docs/02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
and the [contract index](../README.md).

