# Risks — Penatika

> Active product, delivery, security, reliability, and architecture risk register.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Active |
| Last Updated | `2026-09-06` |

## Risk Register

| ID | Risk | Impact | Current Response | Owner / Status |
|---|---|---|---|---|
| R-001 | Teacher controller distracts rather than supports teaching | Core product hypothesis fails | Prototype realistic teaching flows; measure interruptions and teacher control | Owner open; validation required |
| R-002 | Classroom network or internet is too unreliable | Session or live adaptation becomes unusable | Backend authority, safe cached projection, visible degradation; define minimum degraded mode | Owner open; product decision pending |
| R-003 | AI latency interrupts classroom flow | Teacher abandons live adaptation | Measure separate latency stages; support progress, cancellation, timeout, and safe unchanged content | Owner open; provider decision pending |
| R-004 | AI produces plausible but incorrect Mathematics | Student misinformation | Deterministic validation, explicit unsupported state, teacher control, versioned corpus | Owner open; validator decision pending |
| R-005 | Selected curriculum sources are ingested incorrectly, superseded without control, or used beyond guidance licensing | False alignment claims, historical provenance mutation, or implementation block | Preserve layered authority and provenance; verify integrity/versioning; review guidance usage/licensing | Owner open; implementation decisions pending |
| R-006 | Private teacher information leaks to classroom display | Privacy and trust failure | Separate projection schemas, allow-lists, threat tests, architecture invariant | Owner open; release blocker |
| R-007 | Pairing or authorization can be abused | Unauthorized session access or control | Expiring purpose-bound pairing, server authorization, replay/rate controls | Owner open; identity design pending |
| R-008 | Multi-device state diverges | Wrong or lost classroom content | Backend authority, revision-aware commands, reconciliation, idempotency | Owner open; protocol decision pending |
| R-009 | Structured content model is too narrow | Adaptation cannot express useful teaching changes | Start with MVP blocks; version schema; expand from validated needs | Owner open; design work required |
| R-010 | Raw audio or provider payload is retained unexpectedly | Sensitive-data exposure | Ephemeral audio baseline, provider review, redaction and non-retention tests | Owner open; release blocker |
| R-011 | Provider cost or rate limits make usage unsustainable | Outage or unplanned spend | Quotas, resource bounds, model comparison, cost telemetry | Owner open; business/provider decisions pending |
| R-012 | No clear product owner or approver | Requirements and risk decisions stall | Assign ownership before locking requirements or pilot scope | Open |
| R-013 | Previous Pendago decisions conflict with new baseline | Scope drift or lost context | Import only traceable, reviewed decisions into canonical Penatika docs | Open; source material not inspected |
| R-014 | Target device/browser/stylus behavior varies | Poor classroom usability | Define compatibility matrix and test real devices/displays | Owner open; matrix pending |
| R-015 | Accessibility is deferred behind prototype speed | Excludes users and creates costly rework | Include accessibility in design, components, tests, and pilot gates | Owner open; conformance target pending |
| R-016 | Premature distributed architecture slows validation | Delivery and operations complexity | Modular-monolith baseline and ADR review before extraction | Mitigated by ADR-0001 |
| R-017 | Retention and deletion remain undefined before real use | Privacy and operational risk | Resolve before production/pilot data collection | Owner open; blocking for real data |

## Review Rules

- Review before each architecture or release milestone.
- High-impact unresolved risks require an owner and explicit acceptance, mitigation, avoidance, or transfer decision.
- New student data, providers, imports, deployments, or classroom pilots trigger risk and threat-model review.
- Do not close a risk without evidence.

## Related Documents

- [Product Brief](../00_product/PRODUCT_BRIEF.md)
- [Roadmap](../00_product/ROADMAP.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)
- [Known Limitations](./KNOWN_LIMITATIONS.md)

## Change Log

| Date | Change | Author |
|---|---|---|
| `2026-09-06` | Initial risk register | Codex |
