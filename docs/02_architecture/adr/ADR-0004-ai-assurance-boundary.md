# ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority

| Field | Value |
|---|---|
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |

## Context

AI output may be plausible but mathematically incorrect, curriculum claims may be unsupported, and provider confidence is not evidence. Penatika's classroom use requires explicit trust boundaries and must remain provider-independent.

## Decision

AI providers produce untrusted structured proposals. Independent application modules own schema/policy checks, deterministic Mathematics validation, and controlled curriculum source/version provenance. AI self-evaluation cannot mark content mathematically valid or curriculum-authoritative.

## Consequences

### Positive

- Makes correctness and curriculum trust independent from model vendor claims.
- Supports provider replacement and comparative evaluation.
- Provides explicit valid, invalid, unsupported, inconclusive, and error states.
- Improves traceability for teacher decisions and content changes.

### Negative

- Requires validator coverage, curriculum data governance, and provenance storage.
- Some content cannot be deterministically validated and must remain explicitly unsupported or inconclusive.
- Additional latency and complexity must be managed during live adaptation.

## Alternatives Considered

- **Trust provider confidence or self-critique:** rejected because it is non-deterministic and not authoritative.
- **Manual teacher review only:** rejected as the sole mechanism because supported mathematical errors can be caught systematically.
- **Single combined AI/validation service:** rejected because it blurs ownership and replacement boundaries.

## Revisit When

Evaluation evidence shows another assurance mechanism provides equal or stronger reproducibility and traceability for a defined content type.
