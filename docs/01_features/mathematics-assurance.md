# Feature Specification — Mathematics Assurance and Curriculum Grounding

> Defines how Penatika distinguishes generated content, deterministic Mathematics validation, and controlled curriculum claims.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.2` |
| Last Updated | `2026-09-06` |
| PRD Capability | `CAP-MATH-001` |

## 1. Feature Intent

### Problem Addressed

Generated Mathematics content and curriculum claims can be plausible but wrong. Treating model output as authoritative creates unacceptable classroom risk.

### Desired Outcome

Teachers can see whether relevant content is validated, invalid, unsupported, or missing curriculum grounding before deciding to use it.

## 2. Scope

### In Scope

- Identify supported mathematical claims in generated or edited content.
- Validate supported claims using deterministic methods where possible.
- Record validation result and validator version.
- Associate curriculum claims with authority level, controlled source, version, phase/scope, and provenance.
- Distinguish national normative authority, official interpretive guidance, and local school/teacher context.
- Show actionable teacher status without claiming unsupported certainty.
- Prioritize Grade 5 fractions and Grade 7 basic algebra or linear equations.

### Out of Scope

- Proof of correctness for every possible mathematical statement.
- Automatic authority based on AI self-evaluation.
- Full national curriculum ingestion beyond the MVP Mathematics scope.
- Copying or redistribution of official guidance without explicit usage/licensing review.
- Advanced graphing or 3D mathematical visualization.

## 3. Functional Requirements

### FR-MATH-001 — Classify Assurance Needs

The system shall identify content requiring Mathematics validation, curriculum grounding, both, or neither according to versioned rules.

### FR-MATH-002 — Validate Supported Mathematics Deterministically

For supported claims, the system shall use a deterministic validator and record the input, normalized representation, result, and validator version needed for reproducibility.

### FR-MATH-003 — Distinguish Result States

The system shall distinguish at least `VALID`, `INVALID`, `UNSUPPORTED`, `INCONCLUSIVE`, and `ERROR`. Only `VALID` indicates successful deterministic validation.

### FR-MATH-004 — Block Invalid Content from Silent Acceptance

Content with an `INVALID` result shall not silently become classroom-ready. The teacher shall receive an actionable private warning and supported correction path.

### FR-MATH-005 — Expose Unsupported Validation

Unsupported or inconclusive validation shall be visible and shall not be presented as correct merely because AI generated it.

### FR-MATH-006 — Ground Curriculum Claims

Curriculum-alignment claims shall reference an authority level, controlled source identifier, source version, relevant scope/phase, and retrieval or matching provenance.

### FR-MATH-007 — Detect Stale Assurance Results

Editing content after validation shall invalidate or recalculate affected assurance results.

### FR-MATH-008 — Support Initial Validation Corpus

The first validation corpus shall cover selected Grade 5 fraction and Grade 7 basic algebra or linear-equation cases, including valid, invalid, ambiguous, and unsupported examples.

### FR-MATH-009 — Keep Assurance Independent of AI Provider

Changing the AI provider shall not redefine deterministic validation rules or authoritative curriculum data.

### FR-MATH-010 — Distinguish Curriculum Authority Levels

The system shall distinguish at least `NORMATIVE`, `OFFICIAL_GUIDANCE`, and `LOCAL_CONTEXT` curriculum authority levels. Retrieval rank, AI confidence, or semantic similarity shall not promote one authority level into another.

### FR-MATH-011 — Apply the MVP Mathematics Normative Source

For MVP Mathematics curriculum claims, `NORMATIVE` grounding shall resolve to Keputusan Kepala BSKAP No. 046/H/KR/2025 unless Penatika explicitly activates a later official superseding source.

### FR-MATH-012 — Preserve Local Sequencing as Context

School/teacher ATP, KSP/KOSP, lesson plans, or explicit teacher decisions may supply local grade/topic sequencing, but local context shall not be represented as national normative curriculum truth.

## 4. Business Rules

- `BR-MATH-001`: AI confidence is not correctness evidence.
- `BR-MATH-002`: Every assurance result is tied to content and rule/validator version.
- `BR-MATH-003`: Content changes invalidate affected prior results.
- `BR-MATH-004`: Curriculum grounding and mathematical correctness are separate concerns.
- `BR-MATH-005`: Teacher override policy for warnings remains explicit and auditable where required.
- `BR-MATH-006`: AI/model knowledge is never sufficient evidence for curriculum authority.
- `BR-MATH-007`: Official guidance is interpretive unless the issuing authority explicitly defines the referenced content as normative.
- `BR-MATH-008`: A saved lesson retains the curriculum provenance/version used when that lesson version was created; later source updates do not silently rewrite historical provenance.

## 5. State Model

```text
NOT_EVALUATED → EVALUATING → VALID
                         ├→ INVALID
                         ├→ UNSUPPORTED
                         ├→ INCONCLUSIVE
                         └→ ERROR

Any content edit → NOT_EVALUATED
```

## 6. Data Requirements

- Content identity and version.
- Normalized mathematical representation where applicable.
- Validation rule or engine version.
- Validation status and bounded diagnostic information.
- Curriculum authority level.
- Curriculum source identifier, title, version/decision number, jurisdiction, subject, phase/scope, and relevant reference.
- Retrieval or matching provenance.
- Local-context identifier/version when local sequencing affects the claim.
- Teacher decision when policy allows continuation after a warning.

Diagnostics shown to the teacher should be useful without exposing internal secrets or provider internals.

## 7. Failure and Edge Cases

- Validator cannot parse an expression.
- Multiple mathematically equivalent forms.
- Ambiguous natural-language problem statement.
- Curriculum source has no matching entry or conflicting versions.
- A newer official source supersedes the source used by a saved lesson.
- Guidance and normative sources both match but imply different levels of authority.
- Local teacher/school context conflicts with or extends sequencing beyond the national phase-level claim.
- Validator service or local engine fails.
- Content changes while validation is running.

The system must prefer an explicit unsupported/inconclusive state over a fabricated success.

## 8. Minimum Test and Evaluation Scenarios

- Correct and incorrect Grade 5 fraction operations and word problems.
- Correct and incorrect Grade 7 algebra or linear-equation transformations.
- Equivalent forms and simplification cases.
- Malformed, ambiguous, and unsupported content.
- Content edit invalidates prior validation.
- Curriculum source/version is present for grounded claims.
- Normative Mathematics claim resolves to BSKAP 046/H/KR/2025 for the MVP baseline.
- Official guidance cannot be surfaced as a national normative requirement.
- Local sequencing remains `LOCAL_CONTEXT` and cannot overwrite normative provenance.
- Saved lesson provenance remains stable after a newer curriculum source version is introduced.
- AI provider assertion cannot mark content valid.
- Property-based or generated deterministic test cases where suitable.

## 9. Open Questions

- Which validator approach covers each MVP content type?
- What teacher override behavior is allowed for invalid, unsupported, or inconclusive states?
- Which explanation-quality checks are deterministic versus AI-evaluated?
- What evidence threshold is required before adding another topic or grade?
- What ingestion, normalization, integrity-verification, and retrieval approach should be used for BSKAP 046/H/KR/2025?
- What usage/licensing policy applies before storing or redistributing substantial official guidance content?
- How should school/teacher local curriculum context be created and versioned?

## 10. Definition of Done

- Assurance state and provenance are represented in versioned contracts.
- Initial validation corpus passes against the selected deterministic implementation.
- Invalid and unsupported states are reflected correctly in teacher and classroom workflows.
- Curriculum authority level, source, version, and provenance policy are represented for implemented claims.

## 11. Related Decisions

- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](../02_architecture/adr/ADR-0004-ai-assurance-boundary.md)
- [ADR-0005 — Use Layered Curriculum Authority and Versioned Provenance](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md)
