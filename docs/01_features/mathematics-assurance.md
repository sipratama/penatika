# Feature Specification — Mathematics Assurance and Curriculum Grounding

> Defines how Penatika distinguishes generated content, deterministic Mathematics validation, and controlled curriculum claims.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.4` |
| Last Updated | `2026-09-07` |
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

## 9a. Architecture Resolution (OAD-009)

[ADR-0015](../02_architecture/adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
resolves curriculum ingestion, normalization, integrity/versioning,
local-context modeling, and retrieval architecture for this feature without
changing the authority rules already established above or in ADR-0005.

- **Controlled Source Registry**: a stable `Curriculum Source` identity is
  registered independently of any specific version; registration is
  metadata acquisition, not authority activation.
- **Source-Version vs. Corpus-Version distinction**: the official
  `Curriculum Source Version` (for example, 046/H/KR/2025) is versioned
  separately from Penatika's own normalized `Curriculum Corpus Version`. A
  normalization correction creates a new corpus version and never mutates
  an activated one in place; official source supersession is a separate,
  explicit review-and-activate step that does not rewrite existing saved
  provenance.
- **Deterministic metadata-first retrieval**: runtime grounding resolves
  through authority level, subject, grade → phase mapping, and curated
  topic bindings against explicitly activated corpus data — not through
  embedding similarity or AI model memory.
- **Curated topic binding**: the initial pilot corpus supports controlled
  topic identifiers for at least `FRACTIONS`, `BASIC_ALGEBRA`, and
  `LINEAR_EQUATIONS` as reviewable, versioned data, not logic hidden in a
  prompt.
- **Separate local-context overlay**: a `LocalCurriculumContextVersion` is
  teacher-owned, versioned, and traceable; it may add sequencing context
  alongside a `NORMATIVE` claim but can never become or override one. A
  genuine contradiction (not mere narrowing/sequencing) produces an
  explicit conflict/warning state instead of a silent merge.
- **Explicit `NO_MATCH`/`UNGROUNDED` behavior**: when controlled retrieval
  finds no adequate grounding, the system returns an explicit ungrounded
  state rather than presenting unverified AI knowledge as grounded.
- **No vector-retrieval baseline**: pgvector, embeddings providers, and
  similarity-based reranking are not selected for MVP; similarity never
  redefines authority level even if evaluated later.
- **Official Guidance activation pending review**: `Panduan Mata Pelajaran
  Matematika 2025` remains `OFFICIAL_GUIDANCE`; its substantial content
  remains disabled/not activated in the default runtime corpus until an
  explicit licensing/usage/legal review approves compliant use (the source
  repository currently marks it CC BY-NC 4.0).

## 9c. Architecture Resolution (OAD-008)

[ADR-0016](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md)
resolves the Mathematics validator approach for this feature's MVP scope
without changing the result-state semantics or business rules already
established above.

- **Scoped deterministic validators, not general AI self-evaluation**: the
  MVP supports exactly three validator families — `EXACT_RATIONAL` (Grade 5
  fractions), `AFFINE_EXPRESSION` and `LINEAR_EQUATION` (Grade 7 basic
  algebra / linear equations). Other Mathematics content remains
  `UNSUPPORTED` until a future validator family is added with evidence.
- **Exact rational arithmetic**: fraction correctness uses exact rational
  arithmetic (Apache Commons Numbers `BigFraction` as the initial
  primitive); binary floating-point equality is never the correctness
  authority.
- **Restricted affine/linear-equation grammar**: Grade 7 content is
  normalized into a canonical affine form (`a*x + b`) and classified into
  `UNIQUE_SOLUTION`, `IDENTITY`, or `CONTRADICTION`/`NO_SOLUTION`; this is a
  restricted grammar, not a general Computer Algebra System, and nonlinear,
  multi-variable, or higher-power expressions remain `UNSUPPORTED`.
- **AI independence**: deterministic validation runs inside the backend
  with no AI provider, network CAS, or external mathematical service
  dependency; AI cannot convert `INVALID`/`UNSUPPORTED` to `VALID`, and a
  post-`INVALID` AI correction is a new proposal that must be revalidated.
- **Curriculum independence unchanged**: mathematical correctness and
  curriculum grounding (ADR-0015) remain separate assurance dimensions.
- **No general CAS baseline**: SymPy, Symja, and the Wolfram API are not
  selected for MVP; a CAS may be reconsidered only if supported scope
  expands beyond the bounded validator families with evidence.

## 9d. Open Questions

- Exact teacher UX for entering/selecting local curriculum context.
- Exact field-level OpenAPI/JSON Schema contracts for the curriculum
  context bundle, curriculum references, local-context records, and
  Mathematics claims/results.
- Outcome of the applicable legal/licensing review for Official Guidance
  substantial-content usage.
- Exact curriculum corpus normalization data schema and topic taxonomy.
- Exact parser grammar, resource-limit values, and Apache Commons Numbers
  version pin (deferred to source scaffolding).
- What teacher override behavior is allowed for invalid, unsupported, or inconclusive states?
- Which explanation-quality checks are deterministic versus AI-evaluated?
- What evidence threshold is required before adding another topic, grade, or validator family?

## 10. Definition of Done

- Assurance state and provenance are represented in versioned contracts.
- Initial validation corpus passes against the selected deterministic implementation.
- Invalid and unsupported states are reflected correctly in teacher and classroom workflows.
- Curriculum authority level, source, version, and provenance policy are represented for implemented claims.

## 11. Related Decisions

- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](../02_architecture/adr/ADR-0004-ai-assurance-boundary.md)
- [ADR-0005 — Use Layered Curriculum Authority and Versioned Provenance](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md)
- [ADR-0015 — Use a Versioned Controlled Curriculum Corpus with Deterministic Retrieval](../02_architecture/adr/ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
- [ADR-0016 — Use Scoped Deterministic Mathematics Validators with Exact Arithmetic](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md)

## 12. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.4` | `2026-09-07` | Record OAD-008 architecture resolution (ADR-0016); remove resolved validator-approach question from Open Questions | Claude |
| `0.3` | `2026-09-07` | Record OAD-009 architecture resolution (ADR-0015); remove resolved ingestion/retrieval/local-context questions from Open Questions | Claude |
| `0.2` | `2026-09-06` | (see prior repository history) |
| `0.1` | `2026-09-06` | Initial Mathematics assurance and curriculum grounding feature specification |
