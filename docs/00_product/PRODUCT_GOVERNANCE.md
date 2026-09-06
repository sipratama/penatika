# Penatika Product Governance and Decision Authority

> Canonical detailed source for `Q-07` / `OPD-006`. This document defines founder-led product ownership and requirement approval authority without creating a corporate RACI structure or assigning technical implementation ownership.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Approved Baseline |
| Version | `0.2` |
| Date | `2026-09-06` |
| Product Owner | `sipratama` |
| Requirement Approver | `sipratama` |
| Phase | Founder-led MVP / first-pilot preparation |

## 1. Purpose

This document resolves `Q-07` / `OPD-006` by naming the accountable Penatika Product Owner and Requirement Approver, defining their decision boundaries, and separating product authority from technical implementation authority.

Penatika must always have one clearly accountable Product Owner. Contributors, engineers, reviewers, pilot facilitators, teachers, AI agents, and repository automation may propose, review, or implement changes, but they do not implicitly become product authority.

Core rules:

```text
Proposal != approval
Implementation != product decision
Merge != product-policy authority without explicit accountable-owner approval
```

Product truth remains represented in the applicable canonical Penatika document.

## 2. Current Accountable Ownership

For the current founder-led and solo-project phase:

- **Product Owner:** `sipratama`
- **Requirement Approver:** `sipratama`

The repository identity `sipratama` is the current canonical accountable identity for both roles. This ownership remains effective until explicitly delegated or superseded through this document's governance change process.

One person may temporarily hold multiple roles in a small project, but Product Owner, Requirement Approver, architecture, engineering, security, and other responsibilities remain logically distinct. Another contributor performing work does not make either accountable role vacant or transfer authority implicitly.

## 3. Product Owner Responsibilities

The Product Owner is accountable for:

1. Product vision and target-user definition.
2. Product scope and MVP boundaries.
3. Product Brief decisions.
4. PRD requirements and product-wide rules.
5. Feature behavior that materially changes user experience or product scope.
6. Pilot scope and product interpretation of pilot evidence.
7. Product-level release, proceed, iterate, stop, and Stage B go/no-go decisions.
8. Data-lifecycle product policy.
9. Business model and commercialization decisions.
10. Product-level risk acceptance where acceptance is permitted.
11. Resolving conflicts between competing product requirements.
12. Deciding whether new product behavior belongs in MVP, later scope, or remains rejected.
13. Approving changes that intentionally supersede an existing product decision.

The Product Owner is not automatically the implementation authority for every technical detail and is not automatically the owner of every technical risk.

## 4. Requirement Approval Authority

The Requirement Approver determines whether a proposed product requirement is accepted into the canonical Penatika baseline. During the current phase, the Requirement Approver is `sipratama`, the same accountable identity as the Product Owner.

Requirement approval is required for:

- new PRD capabilities;
- material changes to accepted capabilities;
- new product-wide rules;
- material user-flow or user-visible semantic changes;
- changes to teacher-control policy;
- changes to data/privacy product policy;
- changes to pilot scope or success interpretation rules;
- product scope expansion;
- removal or weakening of accepted product constraints;
- business or product assumptions represented as decisions.

Minor editorial corrections that preserve meaning do not require a new product decision. Ambiguity must not be interpreted as approval.

## 5. Product vs Technical Authority

The Product Owner owns **what** Penatika does and **why** it does it. Architecture, engineering, security, and relevant technical ownership decide **how** to implement accepted product behavior within existing product constraints and accepted architecture decisions.

Technical contributors may select implementation details only when those choices do not materially change:

- product behavior or user-visible semantics;
- product or pilot scope;
- privacy commitments or data-lifecycle policy;
- teacher-control boundaries;
- Mathematics assurance or curriculum-provenance behavior;
- pilot commitments;
- commercial assumptions.

When a technical decision materially affects one of those areas, the contributor must identify the product impact, obtain Product Owner review, update the applicable canonical product source when needed, and follow the applicable technical decision process. A material architecture decision still requires an ADR; Product Owner review does not replace architecture authority.

Architecture and implementation must not override accepted product requirements for convenience.

## 6. AI Agent Authority Boundary

Codex, Claude, ChatGPT, and other AI agents may:

- analyze, propose, draft, and review;
- identify conflicts, omissions, and risks;
- update files when instructed;
- implement already approved requirements.

AI agents may not independently:

- approve or resolve a product decision without owner authorization;
- expand product or pilot scope;
- weaken an accepted safety, teacher-control, privacy, assurance, or authorization requirement;
- accept risk on behalf of the Product Owner;
- approve Stage B or commercial launch;
- reinterpret ambiguity as permission.

An AI-generated document is a draft until the accountable owner explicitly accepts the decision and it is represented in the canonical repository.

## 7. Decision Authority Matrix

| Decision Type | Accountable Authority | Additional Requirement |
|---|---|---|
| Product vision / ICP / user | Product Owner | Canonical Product Brief update |
| MVP scope | Product Owner | Product Brief / PRD update |
| Product requirement | Requirement Approver | Canonical PRD / feature update |
| Pilot scope / success interpretation | Product Owner | `PILOT_PLAN.md` rules apply |
| Stage B product go/no-go | Product Owner | All Stage B entry gates must already pass |
| Business model / payer / pricing direction | Product Owner | `Q-06` resolution |
| Data-retention product policy | Product Owner | `DATA_RETENTION_POLICY.md` applies |
| Feature implementation detail | Engineering / relevant technical owner | Must remain inside approved requirements |
| Material architecture decision | Architecture decision owner | ADR required; Product Owner review when product-impacting |
| Security/privacy implementation control | Relevant technical/security owner | Cannot weaken accepted product/privacy policy |
| Hard pilot safety gate | Objective evidence / policy | Product Owner cannot waive failure through ordinary approval |
| Release blocker | Product and relevant technical evidence | Must be resolved or explicitly superseded through the proper decision process |
| Legacy Pendago reuse | Product Owner / Requirement Approver | `PENDAGO_MIGRATION_REVIEW.md` rules apply |

## 8. Decision Approval Evidence

For the current solo-project phase, product approval is evidenced by the combination of:

1. an explicit decision from `sipratama`;
2. an update to the appropriate canonical document;
3. a version-controlled commit to the Penatika repository.

A generated draft, implementation, review comment, or merge by itself is not product approval. A future team may introduce formal pull-request reviewers, `CODEOWNERS`, approval workflows, or decision tooling, but those mechanisms are not required by the current baseline.

## 9. Stage B Go / No-Go Authority

`sipratama` is the named Product Owner accountable for the Stage B product go/no-go decision. `Q-07` resolves who makes that decision; it does not change what must pass before approval.

Stage B approval is valid only after all applicable entry evidence exists, including:

- Stage A hard safety/trust gates pass;
- `DATA_RETENTION_POLICY.md` is implemented and tested;
- privacy/consent review and notices are ready where applicable;
- deletion, export, and backup-expiry evidence exists;
- deployment and support readiness is accepted;
- the target device/browser/network baseline is reviewed;
- high-impact pilot risks have explicit disposition.

The Product Owner cannot approve Stage B while a required gate remains failed or unevidenced.

## 10. Risk and Hard-Gate Authority

The Product Owner may accept ordinary product uncertainty, prioritize mitigation, defer non-blocking product work, and decide permitted product trade-offs. The Product Owner may not silently accept risk in a way that contradicts a hard pilot gate, Product Release Blocker, Accepted ADR invariant, teacher-control policy, data-retention policy, or authorization/privacy boundary.

Ordinary approval cannot declare a failed hard safety/trust gate passed. Examples include teacher-private state leaking to the classroom display, unauthorized classroom control, publication of a `BLOCKED` proposal, known-invalid Mathematics becoming authoritative, violation of backend authority, or raw audio retention contrary to policy.

The required response is:

```text
failure → investigate → remediate → revalidate
```

Stage B, release, or expansion remains blocked until revalidation passes. Intentionally changing an underlying product or architecture policy requires an explicit superseding decision with impact and risk review; ordinary approval is insufficient.

## 11. Canonical Source Hierarchy

Product and implementation authority flows as follows:

```text
Product Owner decision
  → canonical product document
  → feature specification
  → architecture / technical design
  → contracts
  → implementation
```

Primary canonical product sources include:

- `PRODUCT_BRIEF.md`;
- `PRD.md`;
- `ROADMAP.md`;
- `BUSINESS_MODEL.md`;
- feature specifications where they own detailed behavior;
- approved product and delivery policies, including `PILOT_PLAN.md`, `DATA_RETENTION_POLICY.md`, and `PENDAGO_MIGRATION_REVIEW.md`.

Accepted ADRs remain authoritative for the architecture decisions they own. Technical implementation must not silently redefine product truth.

## 12. Delegation and Succession

Future delegation is allowed only through an explicit governance update that identifies:

- the delegated role;
- the accountable person or stable accountable identity;
- the scope of authority;
- the effective date;
- whether the delegation is temporary or permanent.

Until this canonical document is updated, `sipratama` remains the Product Owner and Requirement Approver. Authority is not transferred merely because another person or agent proposes, implements, reviews, or merges work.

## 13. Governance Change Process

A governance change must:

1. identify the role or decision boundary changing;
2. identify the current and successor accountable identities where applicable;
3. define scope, effective date, and temporary/permanent status;
4. assess impacts on open decisions, Stage B, release authority, and risk ownership;
5. update this canonical document and any directly affected product/delivery metadata;
6. receive explicit approval from the currently accountable Product Owner before the change takes effect;
7. be recorded in version control.

Governance changes must not silently reassign technical/domain risks, weaken hard gates, or replace an applicable ADR process.

## 14. Remaining Open Product Decisions

The initialized `Q-01`–`Q-07` / `OPD-001`–`OPD-007` product decision set has no remaining unresolved product decisions. Future product decisions may still arise through normal product governance.

The identity, architecture, provider, implementation, pricing-evidence, privacy-review, deployment, and Stage B evidence follow-ups remain unresolved where documented, but they are not unresolved product-ownership or business-model authority.

## 15. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.2` | `2026-09-06` | Record the resolved business-model baseline and close the initialized open-product-decision set | Codex |
| `0.1` | `2026-09-06` | Resolve `Q-07` / `OPD-006` with founder-led product ownership and requirement approval authority | Codex |
