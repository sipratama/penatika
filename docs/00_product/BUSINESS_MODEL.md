# Penatika Business Model and Commercial Path

> Canonical detailed source for `Q-06` / `OPD-007`. This document defines the Penatika teacher-first freemium SaaS commercial baseline and its evidence-gated commercialization path. It is not an investor pitch or financial forecast.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Approved Baseline |
| Version | `0.1` |
| Date | `2026-09-06` |
| Owner | `sipratama — Product Owner` |
| Initial Market | Indonesia |
| Model | Teacher-first freemium SaaS |
| Initial Paid Offer | Teacher Pro subscription |
| Future Path | Institutional licensing |

## 1. Purpose

This document resolves `Q-06` / `OPD-007` by defining what Penatika sells, who pays, how the free and paid tiers relate to the product, and what evidence must exist before teachers are charged. Numeric pricing, payment implementation, billing systems, invoicing, and tax logic remain later commercial parameters and are not resolved here.

## 2. Business Model Decision

Penatika adopts a **teacher-first freemium SaaS** model:

1. meaningful free teacher access;
2. optional paid Teacher Pro subscription;
3. later institutional or school licensing after teacher adoption and institutional needs are validated.

The commercial motion is teacher-first adoption, optional individual paid upgrade, evidence of repeated usage, then school or institutional expansion. Penatika does not require school procurement for the initial teacher experience.

## 3. Primary User and Payer

The primary user remains the Indonesian Mathematics teacher.

The initial commercial payer hypothesis is an individual teacher purchasing an optional Teacher Pro subscription. A future secondary payer is a school or educational institution purchasing access for multiple teachers.

The future institutional payer does not change the current teacher-first product definition, and school administrators are not added to the MVP merely because institutional licensing is a future commercial path.

## 4. Teacher-First Adoption Principle

Teacher usefulness and adoption come before institutional workflow expansion. The initial product, pilot, and commercialization path serve individual teachers first. Institutional scale may be considered only after teacher-first value and repeated usage are evidenced.

## 5. Pilot Commercial Policy

Stage A and Stage B pilot access is free. The first pilot must not require subscription payment, payment cards, billing workflows, paid AI credits, institutional purchase orders, or school procurement.

The first pilot evaluates teaching-flow fit, teacher control, live-adaptation usefulness, trust, safety, and reliability. It is not a pricing or revenue experiment.

Willingness-to-pay research may be conducted separately after a teacher has experienced the product, but it must not alter the `Q-04` pilot success classification or its metrics.

## 6. Free Tier

The free tier must remain a meaningful teacher product and preserve the core teaching experience, including where applicable:

- preparing and reviewing supported lessons;
- starting a classroom session;
- the classroom canvas and navigation;
- supported `DIRECT_ACTION` controls;
- digital ink;
- teacher-controlled classroom display;
- bounded AI generation and adaptation usage.

The exact numeric AI quota is not decided here. Free access may use bounded AI usage, fair-use limits, resource quotas, and rate limits to control AI cost.

Quota exhaustion must not corrupt or terminate an active classroom session. When new AI capacity is unavailable because a quota is exhausted, the product follows the existing graceful-degradation semantics:

- current reviewed content remains usable;
- supported deterministic classroom controls remain usable;
- digital ink remains usable when dependencies are healthy;
- new AI generation may be unavailable until quota resets or access is upgraded.

No second degraded-mode policy is created; `Q-03` / `ADR-0007` semantics apply where relevant.

## 7. Teacher Pro

Teacher Pro is the first paid commercial offer. The commercial unit is one teacher subscription. Expected billing cadence is monthly, with optional annual billing later or at commercial launch. The initial market currency is IDR.

Teacher Pro primarily monetizes:

- higher AI usage allowance;
- higher resource limits;
- future productivity or convenience capabilities only when separately approved into product scope.

Penatika does not invent specific premium features solely to justify pricing. Teacher Pro does not introduce longer unsafe retention, weaker privacy, different assurance quality, different Mathematics correctness, or a different teacher-control policy.

## 8. Institutional / School Path

Institutional licensing is a later commercial path, not MVP scope. A potential future payer is a school, educational institution, or school network.

Potential institutional value may include separately approved capabilities such as centralized licensing, multi-teacher billing, procurement-friendly contracts, administrative support, deployment and support commitments, institution-specific configuration, local curriculum context, and privacy-appropriate institutional reporting. These are not automatically added to the MVP.

School workflows are revisited only after the teacher-first core succeeds, institutional demand is evidenced, administrator needs are validated, and privacy and identity implications are understood. Future institutional pricing may use per-teacher seats or a negotiated school/institution license; the exact packaging and price remain future commercial design.

## 9. Pricing Strategy

The exact IDR price point is intentionally not locked during the product baseline. Numeric pricing must be evidence-driven from AI and infrastructure cost telemetry, usage distribution, willingness-to-pay research, pilot or early-access evidence, expected support cost, and sustainable unit economics.

The Product Owner must explicitly approve the numeric price before Penatika charges teachers. The resolved product decision is the subscription model, payer, tiering principle, commercial path, and pricing-evidence process; the exact numeric price is a later commercial parameter.

## 10. AI Cost and Quota Strategy

AI usage must be resource-bounded so that free adoption does not create unbounded provider cost. Bounded AI usage, fair-use limits, resource quotas, and rate limits are acceptable controls.

Quota or resource exhaustion must degrade new AI generation without bypassing teacher control, assurance, provenance, authorization, privacy, or recovery policy. Cost controls must not weaken the free product to a non-functional demo.

## 11. Safety and Trust Across Tiers

Penatika must never monetize by weakening safety. All commercial tiers preserve the same applicable baseline for:

- teacher publication approval;
- `BLOCKED` content behavior;
- Mathematics assurance;
- curriculum authority and provenance;
- backend-authoritative classroom state;
- authorization;
- privacy boundaries;
- structured-content validation;
- data deletion and export rights required by the product baseline;
- graceful degradation;
- hard safety/trust gates.

Prohibited examples include making Mathematics validation paid-only, making teacher approval paid-only, or making data deletion or export paid-only. Safety, correctness, privacy, and ownership rights are not premium features.

## 12. Data Monetization Boundary

Penatika does not use advertising as the primary business model for the teacher-first product and does not monetize by selling teacher personal data, selling student data, student profiling, advertising targeted from classroom content, selling raw prompts or classroom interactions, or selling raw audio.

Product data may be used only according to approved product/privacy purposes and the retention policy. A future materially different data use requires an explicit new product and privacy decision.

## 13. Commercial Phases

### Phase 0 — MVP / Pilot Validation

- Access: free or controlled pilot.
- Billing: none.
- Objective: prove teacher value, workflow, trust, and reliability.
- Required evidence: `Q-04` pilot rules.

### Phase 1 — Post-Pilot Early Access

Entry condition: pilot outcome supports `PROCEED`, or the Product Owner explicitly begins a bounded non-commercial pricing-research experiment after hard gates pass.

The free core remains. Commercial exploration measures real AI/resource usage, models cost per active teacher/session, conducts willingness-to-pay research, tests quota design and proposed Teacher Pro packaging, and validates whether teachers understand the Free versus Pro distinction. Users are not silently charged.

### Phase 2 — Individual Teacher Commercial Launch

Offer: Free plus Teacher Pro subscription. Primary payer: individual teacher. Billing: monthly subscription with optional annual billing. The exact IDR price requires Product Owner approval before charging, and commercial launch requires the launch gates defined below.

### Phase 3 — Institutional Expansion

Only after evidence supports it. Potential model: school or institution licensing. Penatika must not assume school procurement is superior to teacher-first adoption and must not add institution workflows until separately validated and approved.

## 14. Commercial Launch Gates

Individual paid commercial launch must not occur until:

1. pilot hard safety/trust gates pass;
2. pilot outcome supports product continuation;
3. the Product Owner approves commercial launch;
4. AI and infrastructure cost per active teacher/session is measured sufficiently to model unit economics;
5. proposed free-tier AI/resource limits are tested;
6. willingness-to-pay evidence exists;
7. numeric pricing is explicitly approved by the Product Owner;
8. billing and payment implementation is tested and truthful;
9. applicable refund, cancellation, and subscription behavior is defined;
10. applicable privacy, legal, and commercial notices are reviewed;
11. `DATA_RETENTION_POLICY.md` implementation remains compliant;
12. support and incident-handling expectations appropriate to paying users are defined;
13. security and Product Release Blockers required for commercial operation are resolved.

This document does not select the payment implementation.

## 15. Commercial Metrics

After pilot and during commercial validation, measure diagnostic commercial evidence without assigning arbitrary success thresholds yet.

Candidate categories include:

- **Adoption:** activated teachers; teachers completing a first classroom session; repeat teachers; classroom sessions per active teacher.
- **Value:** repeated live-adaptation usage; teacher reuse intent; frequency of supported classroom use.
- **Economics:** AI cost per classroom session; AI cost per active teacher; infrastructure cost per active teacher; free-tier quota consumption; quota-exhaustion frequency; paid-plan usage distribution; measurable support cost.
- **Monetization:** Free-to-Teacher Pro conversion; paid retention and cancellation; willingness-to-pay distribution; monthly versus annual preference; revenue per paying teacher; gross-margin model.
- **Institutional signal:** teacher requests for school purchase; inbound school interest; number of teachers from the same institution; observed procurement and support requirements.

These commercial diagnostics remain separate from `Q-04` first-pilot pass/fail thresholds.

## 16. Business Model Risks

- Commercial tiering could damage teacher trust or make the free product unusable. Penatika mitigates this through the meaningful-free principle, monetizing capacity or convenience rather than safety, observing quota exhaustion and teacher behavior, and requiring Product Owner review before tier changes.
- Provider cost or rate limits could make free usage unsustainable. Penatika mitigates this through bounded AI and free-tier quotas, usage and cost telemetry, cost-per-session and cost-per-active-teacher evidence, provider/architecture decisions later, and pricing and unit-economics validation before paid launch.

## 17. Explicit Non-Scope

Commercial billing, payment processing, checkout, subscription API, pricing page, invoice logic, tax logic, financial forecasting, and an exact price point are not defined by this baseline. Institutional workflows, school administration, procurement products, and administrative reporting are deferred and not added to the MVP.

## 18. Product Owner Approval Points

The Product Owner must explicitly approve:

- entry into Phase 1 pricing research where a bounded non-commercial experiment is used;
- numeric IDR pricing before any charge;
- paid Teacher Pro commercial launch;
- material free-tier or Teacher Pro tier changes;
- any future institutional expansion decision;
- any material data-use change;
- any change that would treat safety, correctness, privacy, or ownership rights as tiered features.

## 19. Future Commercial Experiments

Any future pricing, packaging, willingness-to-pay, or quota experiment must be bounded, communicated truthfully, and must not alter first-pilot safety classification or become silent billing. Commercial experiments require Product Owner approval and follow the applicable privacy, retention, and notice requirements.

## 20. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.1` | `2026-09-06` | Resolve `Q-06` / `OPD-007` with the teacher-first freemium SaaS and staged commercialization baseline | Codex |
