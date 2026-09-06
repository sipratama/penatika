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
| R-001 | Teacher controller distracts rather than supports teaching | Core product hypothesis fails | Measure workflow completion, teacher-control confidence, teaching-flow fit, facilitator intervention, and observed interruption during staged pilot sessions | Owner open; validation required |
| R-002 | Classroom network or internet is too unreliable | Session or live adaptation becomes unusable | Exercise controlled ADR-0007 degradation/recovery scenarios and collect representative target-school network evidence | Owner open; evidence required |
| R-003 | AI latency interrupts classroom flow | Teacher abandons live adaptation | Collect semantic adaptation latency p50/p95 and observed teaching-flow interruption; keep numerical limits evidence-driven | Owner open; provider decision pending |
| R-004 | AI produces plausible but incorrect Mathematics | Student misinformation | Use a hard pilot gate: known deterministic `INVALID` content cannot be published; retain deterministic validation and a versioned corpus | Owner open; validator decision pending |
| R-005 | Selected curriculum sources are ingested incorrectly, superseded without control, or used beyond guidance licensing | False alignment claims, historical provenance mutation, or implementation block | Preserve layered authority and provenance; verify integrity/versioning; review guidance usage/licensing | Owner open; implementation decisions pending |
| R-006 | Private teacher information leaks to classroom display | Privacy and trust failure | Use a hard pilot gate requiring zero teacher-private leakage, supported by separate projection schemas, allow-lists, and threat tests | Owner open; release blocker |
| R-007 | Pairing or authorization can be abused | Unauthorized session access or control | Expiring purpose-bound pairing, server authorization, replay/rate controls | Owner open; identity design pending |
| R-008 | Multi-device state diverges | Wrong or lost classroom content | Use a hard pilot/recovery gate requiring no client authority divergence; exercise revision-aware reconciliation and idempotency | Owner open; protocol decision pending |
| R-009 | Structured content model is too narrow | Adaptation cannot express useful teaching changes | Start with MVP blocks; version schema; expand from validated needs | Owner open; design work required |
| R-010 | Raw audio or provider payload is retained unexpectedly | Sensitive-data exposure | Zero default raw-audio persistence; maximum `24-hour` diagnostic window for genuinely required raw AI working data; provider review, redaction, expiry, and non-retention tests | Owner open; release blocker |
| R-011 | Provider cost or rate limits make usage unsustainable | Outage or unplanned spend | Quotas, resource bounds, model comparison, cost telemetry | Owner open; business/provider decisions pending |
| R-012 | Product authority becomes ambiguous after delegation or team growth | Requirements and risk decisions could stall or be approved by an unclear authority | `sipratama` is the named Product Owner / Requirement Approver for the current phase; require explicit scoped, dated governance updates when authority is delegated or team structure changes | Resolved for current founder-led phase; monitor delegation/team growth |
| R-013 | Previous Pendago decisions conflict with new baseline | Scope drift or lost context | Allow only reviewed and mapped Pendago decisions to enter Penatika; Penatika canonical documents and Accepted ADRs win conflicts; legacy contracts remain reference-only until revalidated; update the migration review for any future legacy reuse | Mitigated for current baseline; monitor future legacy reuse |
| R-014 | Target device/browser/stylus behavior varies | Poor classroom usability | Collect target-device, browser, display, stylus, and network evidence during Stage A and Stage B; define the supported baseline before real-classroom use | Owner open; matrix pending |
| R-015 | Accessibility is deferred behind prototype speed | Excludes users and creates costly rework | Include accessibility in design, components, tests, and pilot gates | Owner open; conformance target pending |
| R-016 | Premature distributed architecture slows validation | Delivery and operations complexity | Modular-monolith baseline and ADR review before extraction | Mitigated by ADR-0001 |
| R-017 | Retention/deletion policy is defined, but implementation or operational failure retains pilot or teacher data longer than intended | Privacy and operational risk | Apply the approved `DATA_RETENTION_POLICY`; automate or operate expiry; test deletion and export authorization; collect Stage B evidence; complete applicable privacy review | Policy resolved; implementation evidence required before Stage B |
| R-018 | Directional pilot evidence is overgeneralized | A small pilot is misrepresented as educational efficacy or market proof | Record scope, sample context, metric definitions, limitations, and protocol deviations; prohibit statistical learning-outcome, nationwide demand, or efficacy claims | Owner open; reporting control required |

## Review Rules

- Review before each architecture or release milestone.
- High-impact unresolved risks require an owner and explicit acceptance, mitigation, avoidance, or transfer decision.
- New student data, providers, imports, deployments, or classroom pilots trigger risk and threat-model review.
- Do not close a risk without evidence.

## Related Documents

- [Product Brief](../00_product/PRODUCT_BRIEF.md)
- [Roadmap](../00_product/ROADMAP.md)
- [Product Governance and Decision Authority](../00_product/PRODUCT_GOVERNANCE.md)
- [MVP Pilot Plan](./PILOT_PLAN.md)
- [Data Retention, History, Export, and Deletion Policy](./DATA_RETENTION_POLICY.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)
- [Known Limitations](./KNOWN_LIMITATIONS.md)

## Change Log

| Date | Change | Author |
|---|---|---|
| `2026-09-06` | Resolve current product-owner ambiguity and retain delegation/team-growth monitoring | Codex |
| `2026-09-06` | Update retention risks for the approved policy while keeping implementation evidence as a Stage B requirement | Codex |
| `2026-09-06` | Align pilot risk responses and add evidence-overgeneralization risk | Codex |
| `2026-09-06` | Initial risk register | Codex |
