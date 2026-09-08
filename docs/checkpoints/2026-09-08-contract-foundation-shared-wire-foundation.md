# Contract Foundation Shared Wire and Problem Foundation

> **Document role:** This is a non-authoritative engineering handoff snapshot.
> It is subordinate to the PRD, feature specifications, System Architecture,
> Accepted ADRs, and machine-readable contracts. The authoritative field-level
> definitions created by this batch are in
> [`contracts/openapi/openapi.yaml`](../../contracts/openapi/openapi.yaml).

## 1. Purpose

This checkpoint records Contract Foundation Batch 2: the smallest shared HTTP
wire primitives and RFC 9457 Problem Details foundation required before the
first application operations are authored.

No Lesson, Classroom Session, Pairing, command, snapshot, or SSE operation is
defined. No source scaffolding begins in this batch.

## 2. Snapshot Metadata

| Field | Value |
|---|---|
| Date | `2026-09-08` |
| Branch | `feat/contract-foundation` |
| Starting HEAD | `17afa456361c01ed6aeca1a1aac4b67c5a1b1b26` |
| Initial working tree | Clean |
| Contract baseline | OpenAPI 3.1.2 / YAML 1.2 |
| Implementation state | Pre-source / Pre-scaffolding |

## 3. Inputs Reviewed

- [ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md)
  for contract family, ownership, OpenAPI, error, nullability, compatibility,
  and tooling rules;
- [System Architecture section 12](../02_architecture/SYSTEM_ARCHITECTURE.md)
  for the active contract boundary and RFC 9457 baseline, plus section 13 for
  correlation semantics;
- [Contract index](../../contracts/README.md),
  [OpenAPI README](../../contracts/openapi/README.md), and
  [standalone schema README](../../contracts/schemas/README.md);
- [API and Integration Standard](../standards/06_API_INTEGRATION_STANDARD.md)
  for naming, validation, idempotency distinction, errors, compatibility, and
  sensitive-data minimization;
- relevant [Security Standard](../standards/08_SECURITY_STANDARD.md) rules for
  input bounds, public error safety, credentials, and private diagnostics;
- the Penatika ECC profile in
  [AI-Assisted Development Standard](../standards/14_AI_ASSISTED_DEVELOPMENT.md);
- [Classroom Session](../01_features/classroom-session.md),
  [ADR-0002](../02_architecture/adr/ADR-0002-backend-authoritative-session-state.md),
  [ADR-0007](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md),
  [ADR-0011](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md),
  and [ADR-0012](../02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md)
  only to confirm identifier, command, revision, authorization, and transport
  semantics needed by this batch;
- the prior
  [Batch 1 scope freeze](./2026-09-08-contract-foundation-scope-freeze.md) as a
  non-authoritative handoff snapshot.

## 4. Schema Ownership Audit

| Candidate | Consumers | Why shared now? | OpenAPI-only? | Cross-contract / independently consumed? | Safety-critical? | Independent lifecycle? | Canonical owner | Decision |
|---|---|---|:---:|:---:|:---:|:---:|---|---|
| `LessonVersionId` | Future lesson selection and session-start HTTP operations | Batch 3 needs one stable opaque lesson-version reference | Yes | Not yet | No | No | OpenAPI component | Create |
| `ClassroomSessionId` | Future session-start, command, snapshot, and pairing HTTP operations | The frozen slice shares one session identity across its HTTP steps | Yes for current evidence | Future projection reuse is not authored yet | Authorization-sensitive context, not authority itself | No | OpenAPI component | Create |
| `ParticipantId` | Possible future pairing/participant operations | No current browser consumer is proven to need the participant's internal identity | Undecided | Not demonstrated | Authorization-sensitive | Not demonstrated | None yet | Defer until Pairing contract |
| `CommandId` | Future command submission and uncertain-acknowledgement reconciliation | Batch 1 requires stable logical command identity distinct from correlation | Yes for current evidence | Not independently consumed yet | Consistency-sensitive | No | OpenAPI component | Create |
| `Revision` | Future session-start, command, snapshot, and synchronization HTTP shapes | Backend authority, stale detection, and reconciliation already share this meaning | Yes for current evidence | Future projection reuse will be reassessed with that schema | Consistency-critical | No separate lifecycle yet | OpenAPI component | Create |
| UTC timestamp | Future expiry and lifecycle responses | No Batch 2 shape requires a timestamp | Undecided | Not demonstrated | No | Not demonstrated | None yet | Defer until an operation requires it |
| `CorrelationId` | HTTP problems and future request/workflow diagnostics | Problem responses need a privacy-safe operational reference distinct from business IDs | Yes | No independent consumer | Security/privacy-sensitive | No | OpenAPI component | Create |
| `ProblemDetails` | Every future failing HTTP operation | ADR-0010 requires one RFC 9457 HTTP error foundation | Yes | HTTP-specific | Security-sensitive public boundary | Evolves with HTTP API | OpenAPI component | Create |
| `FieldError` | Validation problems in future HTTP operations | ADR-0010 permits bounded safe field validation details | Yes | HTTP request validation only | Security/privacy-sensitive | No | OpenAPI component | Create as closed nested structure |
| `ProblemResponse` | Reusable response reference for future HTTP operations | Binds `ProblemDetails` to `application/problem+json` and owns validated examples | Yes | HTTP-specific | Security-sensitive public boundary | Follows `ProblemDetails` | OpenAPI response component | Create |

No candidate currently crosses the ADR-0010 threshold for a standalone JSON
Schema. Batch 2 therefore creates zero production files under
`contracts/schemas/`.

## 5. Machine-Readable Artifacts Created

- [`contracts/openapi/openapi.yaml`](../../contracts/openapi/openapi.yaml) is
  the authoritative OpenAPI 3.1.2 root.
- `paths` is intentionally empty; no placeholder or application endpoint is
  present.
- Shared definitions live under OpenAPI `components.schemas`; the reusable
  RFC 9457 media-type binding and examples live under
  `components.responses.ProblemResponse`.

## 6. Shared Primitive Decisions

- `LessonVersionId`, `ClassroomSessionId`, and `CommandId` are distinct opaque
  non-empty strings bounded to 128 characters with whitespace excluded. No
  UUID, database key, sequence, ordering, ownership, or authorization meaning
  is encoded.
- `CommandId` identifies one logical mutation across retry/reconciliation. It
  is not `CorrelationId`, does not authorize a mutation, and does not by itself
  guarantee idempotency. Placement, equivalence, conflicting reuse, storage,
  and deduplication duration remain deferred.
- `Revision` is a non-negative integer, monotonic only within its Classroom
  Session, and used for conflict detection and synchronization. It does not
  imply the initial revision, global uniqueness, or equality with event count.
  Its maximum is JavaScript's largest exactly represented integer so browser
  consumers cannot silently lose revision precision.
- `CorrelationId` is a bounded non-empty operational reference. It is not a
  business ID, credential, authorization input, command identity, or
  idempotency guarantee.
- UTC timestamp and `ParticipantId` are deliberately absent until an operation
  proves a wire consumer and required semantics.

All primitive schemas reject `null`; absence is decided by the containing
operation or object, not by weakening the primitive.

## 7. RFC 9457 Decisions

`ProblemDetails` preserves the RFC members `type`, `title`, `status`, `detail`,
and `instance`, and adds Penatika extensions `code`, `correlationId`, and
`fieldErrors`.

- `type`, `title`, `status`, and `code` are required for every Penatika problem
  response. `about:blank` is used when no more specific registered type applies.
- `detail`, `instance`, `correlationId`, and `fieldErrors` are optional and are
  absent rather than `null` when unavailable or inapplicable.
- `code` is a required bounded machine-stable discriminator. It is not an enum
  in Batch 2; later operation contracts introduce the domain error catalog.
- `correlationId` is optional because a safe public correlation reference may
  be unavailable or intentionally withheld. It must never contain a secret,
  credential, private trace topology, or authorization meaning.
- `fieldErrors` is present only for request-validation detail, contains between
  one and 20 entries, and may be a safe subset if more fields fail. It never
  includes rejected values, raw bodies, credentials, stack traces, exception
  names, SQL, provider payloads, or private diagnostics.
- Each `FieldError` requires `field`, `code`, and `message`. The entry is closed
  to unknown properties; `message` is human-readable and must not be parsed for
  client logic.
- The reusable response component fixes the media type as
  `application/problem+json` and provides generic and validation examples.
  Example code values are illustrative strings, not a frozen domain catalog.

## 8. Compatibility Decisions

| Schema | Compatible additive change | Breaking or compatibility-sensitive change | Unknown fields | Null | Bounds rationale |
|---|---|---|---|---|---|
| Domain identifiers / `CommandId` | Description clarification that does not change semantics | Representation/type change, lowering bounds, adding UUID/pattern semantics, or changing identity meaning | N/A | Rejected | 128 characters limits abuse while leaving implementation opaque |
| `Revision` | Semantic clarification within the frozen meaning | Changing to string/float, allowing negative or unsafe integers, making it global, or equating it with event count | N/A | Rejected | Safe-integer maximum preserves exact browser comparisons |
| `CorrelationId` | Non-semantic documentation clarification | Treating it as command/idempotency/authorization identity or lowering bounds | N/A | Rejected | 128 characters supports common opaque correlation formats without trace topology |
| `ProblemDetails` | Adding optional RFC/Penatika extension members | Removing/renaming members, changing types/nullability, making optional members required, or changing RFC semantics | Tolerated | Rejected | Text and URI members are bounded to constrain unsafe/error amplification |
| `FieldError` | No field addition is automatically compatible for strict old consumers; review is required | Removing/renaming fields, changing types, opening sensitive arbitrary data, or changing requiredness | Rejected | Rejected | Field 256, code 128, message 512, list 20; sufficient diagnostics without unbounded payloads |
| `ProblemResponse` | Adding named examples without changing schema semantics | Changing media type or canonical problem schema | N/A | N/A | One HTTP response owner avoids competing error envelopes |

Browser and backend deployments are not assumed atomic. Consumers must accept
unknown top-level Problem Details extension members, while the bounded nested
`FieldError` remains deliberately closed.

## 9. Explicitly Deferred Field-Level Work

- all HTTP paths and operations, including authenticated Teacher, Lesson,
  Classroom Session, Pairing/redemption, `NEXT`, snapshot, and SSE;
- security-scheme, cookie, CSRF header/token, login, callback, and concrete OIDC
  provider definitions;
- `ParticipantId`, pairing-grant secrets, QR/human-code shapes, and pairing
  rate-limit contracts;
- command request/response shapes, expected-revision placement, idempotency
  header/body placement, and stale-revision status mapping;
- Display projection, scene/content, SSE envelope/event names, and
  `Last-Event-ID` field-level contract;
- domain-specific error code catalog and problem-type URI strategy beyond the
  RFC-standard `about:blank` baseline;
- standalone JSON Schemas, source code, persistence, migrations, Docker, and
  CI/CD.

## 10. Validation Evidence

- `git diff --check` - passed before and after status/documentation sync.
- `python3 scripts/validate_template.py --project-mode` -
  `Validation passed (project mode).`
- Redocly CLI version - `2.51.2`, obtained with
  `npx --yes @redocly/cli@latest --version`.
- OpenAPI lint -
  `npx --yes @redocly/cli@2.51.2 lint contracts/openapi/openapi.yaml
  --extends=recommended --skip-rule=no-empty-servers
  --skip-rule=no-unused-components --skip-rule=info-license` - passed with no
  findings. The three skipped rules are intentionally inapplicable because
  Batch 2 forbids invented servers, has no operations that can reference the
  new components yet, and does not establish a licensing policy.
- An initial unmodified `recommended` lint run reported only the expected
  `no-empty-servers` error plus unused-component and missing-license warnings.
  No server was added because the batch explicitly forbids inventing one.
- Redocly's recommended example validation remained enabled; both generic and
  validation Problem Details examples passed against the referenced schema.
- Structural scope checks confirmed `paths: {}`, no application paths,
  security schemes, server hostnames, domain error catalog, AsyncAPI directory,
  or standalone production JSON Schema.
- Ajv was not required because the ownership audit created zero standalone
  JSON Schemas.
- Source-boundary checks confirmed no Java/React source, application build
  files, migrations, Docker/Compose/Caddy, or CI/CD scaffold.
- Final `git status`, diff statistics, and complete diff were reviewed; only
  the intended Batch 2 contract and documentation files changed.

## 11. Risks and Open Items

- The exact syntax for nested `FieldError.field` paths is intentionally not
  frozen until real request shapes exist; operation contracts must document
  the client-visible field/path values they emit.
- Future Display projection/SSE schemas must reassess whether `Revision` or
  other primitives require standalone ownership. They must reference or
  relocate the canonical definition rather than duplicate it.
- Exact error codes, problem types, and status mappings remain operation-level
  decisions.

No product or architecture decision changed in Batch 2.

## 12. Exit Criteria

- OpenAPI 3.1.2 root exists and validates.
- `paths` is intentionally empty and no application endpoint exists.
- Only justified shared primitives and one RFC 9457 response foundation exist.
- Identifier, command, revision, correlation, nullability, error safety, and
  compatibility semantics are explicit.
- Examples validate against `ProblemDetails`.
- No standalone schema, AsyncAPI contract, source scaffold, migration, or
  deployment artifact is created.
- Contract and project routing documentation is synchronized.

## 13. Next Batch

Batch 3 should define the authenticated Teacher boundary plus the minimum
`LessonVersion` selection/reference and Classroom Session start HTTP contract
required by the frozen first vertical slice. It must not expand into Pairing,
`NEXT`, snapshot, or SSE operations unless their dependency becomes unavoidable
and is documented first.
