# ADR-0016 — Use Scoped Deterministic Mathematics Validators with Exact Arithmetic

| Field | Value |
|---|---|
| ADR | `ADR-0016` |
| Status | Accepted |
| Date | `2026-09-07` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-MATH-001`, `PR-006`, `PR-018`, `FR-MATH-001`, `FR-MATH-002`, `FR-MATH-003`, `FR-MATH-004`, `FR-MATH-005`, `FR-MATH-007`, `FR-MATH-008`, `FR-MATH-009` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

OAD-008 selects the Mathematics validator approach per content type.
[ADR-0004](./ADR-0004-ai-assurance-boundary.md) already establishes that AI
self-evaluation cannot mark content mathematically valid, and that
independent application modules own deterministic Mathematics validation
and curriculum provenance. [Mathematics Assurance](../../01_features/mathematics-assurance.md)
(`FR-MATH-001`–`FR-MATH-009`) already defines the required result states
(`VALID`, `INVALID`, `UNSUPPORTED`, `INCONCLUSIVE`, `ERROR`), the
requirement that a deterministic validator record input, normalized
representation, result, and validator version, and the requirement that
content edits invalidate prior results (`FR-MATH-007`). Neither source
selected the actual validation strategy or arithmetic model — that is this
decision.

[ADR-0015](./ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
already separates mathematical correctness from curriculum grounding as
independent assurance dimensions; this ADR does not change that boundary.
[ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) establishes
module-first Hexagonal Architecture and the rule that ports exist only for
meaningful boundaries. [ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md)
establishes that a known deterministic Mathematics `INVALID` result is a
non-overridable `BLOCKED` condition regardless of teacher approval.

The pilot Mathematics scope is bounded: Grade 5 Fractions and Grade 7 Basic
Algebra / Linear Equations. This decision must define a validation strategy
sized to that bounded scope — exact, reproducible, and independent of any
AI provider or network dependency — without adopting a general-purpose
Computer Algebra System (CAS) that the current scope does not justify.

This ADR does not scaffold source code, create contracts, create physical
schema, install dependencies, select an AI provider (OAD-006), use an LLM
as a Mathematics validator, resolve OAD-006, or broaden the pilot
Mathematics scope beyond Grade 5 Fractions and Grade 7 Basic Algebra /
Linear Equations.

## Decision

Penatika uses **scoped deterministic validators**, not general AI
self-evaluation:

```text
Supported mathematical claim
        ↓
normalize into controlled mathematical representation
        ↓
select deterministic validator by claim type
        ↓
execute exact validation
        ↓
produce versioned assurance result
```

For unsupported content, the system returns `UNSUPPORTED` or
`INCONCLUSIVE` rather than asking an AI model to pretend deterministic
certainty. The initial validator families are `EXACT_RATIONAL` (Grade 5
fractions), `AFFINE_EXPRESSION`, and `LINEAR_EQUATION` (Grade 7 basic
algebra / linear equations). Other Mathematics content remains
`UNSUPPORTED` unless a future validator family is explicitly added with
validation evidence. No general-purpose CAS, external mathematical
service, or LLM is selected as the validation engine.

## Validation Status Semantics

The existing canonical states are unchanged:

- `VALID` — a supported validator executed and proved the scoped claim
  valid.
- `INVALID` — a supported validator executed successfully and proved the
  scoped claim mathematically false.
- `UNSUPPORTED` — the content/operation lies outside the currently
  implemented deterministic validator scope.
- `INCONCLUSIVE` — content belongs broadly to a supported area, but
  available structured input or bounded rules cannot safely determine the
  result.
- `ERROR` — technical validator failure.

`UNSUPPORTED`, `INCONCLUSIVE`, and `ERROR` are never converted to `VALID`
because an AI provider expresses confidence.

## Supported MVP Content Types

The first implementation supports exactly the existing pilot priority:
Grade 5 Fractions and Grade 7 Basic Algebra / Linear Equations, via three
conceptual validator families: `EXACT_RATIONAL`, `AFFINE_EXPRESSION`, and
`LINEAR_EQUATION`.

## Exact Rational Validator

Grade 5 fraction Mathematics uses exact rational arithmetic. Supported
conceptual operations include rational-number normalization, fraction
reduction, equality/equivalence, comparison, addition, subtraction,
multiplication, division where the denominator is non-zero, supported
mixed-number conversion, and supported finite-decimal/rational equivalence
where normalization is exact.

Binary floating-point equality is never used as correctness authority.
`double`, `float`, and epsilon-based equality are not used as the
mathematical truth model for correctness-sensitive fraction calculations.

## Exact Arithmetic Primitive

Apache Commons Numbers `BigFraction` is the initial exact-rational
arithmetic primitive (current verified library line at architecture-decision
time: Apache Commons Numbers 1.3). This ADR does not pin an exact dependency
version; at source scaffolding, the current compatible Apache Commons
Numbers release is verified and the tested version is pinned through normal
dependency governance (see [12_DEPENDENCY_SUPPLY_CHAIN.md](../../standards/12_DEPENDENCY_SUPPLY_CHAIN.md)).

`BigFraction` remains a module-local implementation detail of Mathematics
Assurance. Its Java type is not exposed in OpenAPI contracts, JSON Schema,
other module APIs, or domain-neutral cross-component contracts. Penatika
owns its own Mathematics semantics; the library provides arithmetic
primitives only.

Conversion from a user fraction to `double` and then to `BigFraction` is
avoided for exact curriculum Mathematics. Exact literals are parsed from
their controlled representation: a value such as `1/3` remains
mathematically `1/3`, not the binary floating-point approximation of one
third. Finite decimal support preserves exact decimal semantics where
implemented.

## Restricted Algebra Grammar

For the pilot Grade 7 scope, Penatika uses a **restricted mathematical
expression grammar** and normalizes supported expressions into a canonical
affine form — not a general Computer Algebra System.

## Affine Canonicalization

The conceptual affine representation is `a*x + b`, where `a` and `b` are
exact rational values. The restricted validator may support integer
constants, rational constants, supported finite decimals, one variable,
unary plus/minus, addition, subtraction, parentheses, multiplication by a
constant, and division by a non-zero constant. For example, `2 * (x + 3)`
normalizes to `2*x + 6`, and `x/2 + 3` normalizes to `(1/2)*x + 3`.

The validator is not silently generalized to multiple variables, `x*x`,
`x^2` or higher powers, variable denominators, roots, arbitrary
exponentiation, trigonometry, logarithms, calculus, matrices, arbitrary
functions, or programming/evaluation expressions. These remain
`UNSUPPORTED` unless a future validator family explicitly adds them.

## Linear Equation Validation

For one-variable linear equations, both sides are normalized so that
`LHS = RHS` becomes `a*x + b = 0`. The result is classified into
`UNIQUE_SOLUTION`, `IDENTITY` / `ALL_SUPPORTED_VALUES`, or `CONTRADICTION` /
`NO_SOLUTION`:

- when `a != 0`, the exact solution is `x = -b/a` using exact rational
  arithmetic;
- when `a = 0` and `b = 0`, the equation is an identity;
- when `a = 0` and `b != 0`, the equation has no solution.

For supported one-variable linear equations, equivalence may be determined
through exact solution-set semantics — for example, `2x + 2 = 4` and
`x + 1 = 2` share the supported solution set `x = 1`. A transformation step
may therefore be validated for mathematical equivalence without relying on
textual explanation; equivalence does not imply the explanation is
pedagogically good.

For structured algebra steps within the restricted grammar, Penatika may
validate whether a step preserves the equation's solution set, produces the
claimed exact result, or is mathematically equivalent to the previous
supported expression/equation. The validator does not need to infer the
teacher's intended named operation (for example, "divide both sides by
two") unless that operation is explicitly represented in a future
structured contract.

## Structured Claim Boundary

Deterministic Mathematics validation applies to structured mathematical
claims, not unrestricted natural-language prose. "The explanation is easy
to understand" and "this method is the best way to solve it" are not
Mathematics correctness claims and are not deterministically provable by
this validator. A word problem or explanation may contain mathematical
claims; only claims that can be represented safely in a supported
structured Mathematics form receive deterministic `VALID`/`INVALID` status.
Other semantic/pedagogical content remains `UNSUPPORTED`/`INCONCLUSIVE` for
deterministic validation as applicable.

Supported deterministic validation must operate on a controlled normalized
Mathematics representation; free-text → arbitrary expression parsing is not
the sole correctness boundary. Future AI output contracts should expose the
mathematical assertions needed for deterministic checking in structured
form where relevant. Exact JSON field names and schemas remain deferred to
field-level contract work; no JSON Schema is created by this ADR.

## Validator Routing

Mathematics Assurance owns validator selection:

```text
MathClaim
   │
   ├── rational arithmetic
   │       → ExactRationalValidator
   │
   ├── affine expression
   │       → AffineExpressionValidator
   │
   ├── linear equation
   │       → LinearEquationValidator
   │
   └── unsupported
           → UNSUPPORTED
```

The AI provider does not select which validator is authoritative. The
browser client does not choose a validator to obtain a preferred result.

## Hexagonal / Dependency Boundary

[ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) remains
binding. Mathematics Assurance is one backend module/capability boundary.
The exact-rational library and expression parser are Mathematics-module
implementation details; Apache Commons Numbers classes, parser AST
implementation, and any future CAS library classes do not leak into
unrelated modules. A port is not created merely around `BigFraction`
because Hexagonal Architecture says every dependency needs an interface —
ports exist only for meaningful boundaries.

## AI Independence

MVP deterministic Mathematics validation executes inside the Penatika
backend and does not require an external AI provider, a network CAS, a
Python service, the Wolfram API, a SymPy service, or another external
mathematical API. This is important for deterministic behavior, provider
independence, degraded mode, latency, and reproducibility.

[ADR-0004](./ADR-0004-ai-assurance-boundary.md) and
[ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md) remain
binding: AI may generate Mathematics proposals, propose a correction, or
explain deterministic validator results. AI may not convert `INVALID` to
`VALID`, validate its own answer as authoritative, decide that
`UNSUPPORTED` means correct, bypass the deterministic validator, or alter
validator rules at runtime. If AI proposes a correction after `INVALID`,
the correction is a new proposal and must run through validation again.
Teacher approval (ADR-0006) does not convert a known `INVALID` result into
publishable valid content; existing Q-02 policy continues to decide
publication classes and is not redefined by this ADR.

## Curriculum Independence

[ADR-0015](./ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
remains binding: mathematical correctness and curriculum grounding are
separate assurance dimensions. `Mathematics VALID` + `Curriculum UNGROUNDED`
and `Mathematics INVALID` + curriculum correctly grounded are both possible
states; neither assurance dimension replaces the other.

## Parser and Resource Safety

The mathematical parser is data-only. It never evaluates Java, invokes
script engines, executes code, performs reflection, accesses the
filesystem, accesses the network, or resolves arbitrary functions. It uses
a strict allow-listed mathematical grammar.

Even deterministic input can be adversarial. The implementation must bound
expression/token length, nesting depth, numeric literal digit count, number
of operations, and normalization complexity. A pathological expression
fails safely rather than consuming unbounded CPU or memory. Exact numerical
limits are implementation/configuration decisions established through
tests, not fixed by this ADR.

## Validator Versioning

Every deterministic validation result must be reproducible against a
versioned validator/ruleset. It conceptually retains validator family,
validator/rule version, normalized input or a sufficiently reproducible
representation, result, bounded diagnostic, timestamp, and content
identity/version. A bare `"validated = true"` without validator/version
evidence is not sufficient.

## Content-Edit Invalidation

`FR-MATH-007` remains binding: if validated content changes, prior affected
validation is stale and cannot continue to prove the new content valid.
Relevant validation must be rerun against the changed version.

## Diagnostics

Validator diagnostics are bounded and teacher-useful — for example: the
expected exact rational result differs; an equation step does not preserve
the solution set; a denominator is zero; or an expression contains an
unsupported nonlinear operation. Diagnostics do not expose stack traces,
internal parser dumps, dependency implementation details, or excessive
internal expression state.

## Test / Evaluation Evidence

Versioned deterministic test corpora are required. Grade 5 fraction cases
must include at least: equivalent fractions; non-equivalent fractions
(for example, `2/3 = 4/5` must be `INVALID`); reduction/simplification;
addition; subtraction; multiplication; division; negative values where
supported; zero numerator; zero-denominator rejection; mixed-number cases
where supported; and large exact values within configured limits.

Grade 7 test corpus must include: affine expression equivalence;
distributive simplification; exact rational coefficients; a valid linear
equation solution; an invalid solution; equivalent equation transformations;
an identity case; a contradiction/no-solution case; malformed expressions;
nonlinear expressions; multiple variables; a variable denominator;
unsupported functions; and extreme/nested input resource bounds.

Property-based/generated tests should be used where useful — for example,
for random valid rationals `(a + b) - b = a`; for supported nonzero values
`(a / b) * b = a`; and for generated affine equations with a known exact
solution, the validator returns that exact solution. This ADR does not
select a particular property-test library; that remains
source-scaffolding/dependency work.

## Consequences

### Positive

- Resolves OAD-008 with a validation strategy sized to the actual pilot
  scope (Grade 5 Fractions; Grade 7 Basic Algebra / Linear Equations)
  rather than a general-purpose CAS.
- Keeps Mathematics correctness reproducible, exact, and independent of
  any AI provider, model, or network dependency.
- Avoids floating-point rounding as a source of false correctness/
  incorrectness for curriculum-critical fraction/algebra claims.
- Preserves ADR-0004/ADR-0006's AI-independence and non-overridable
  `INVALID`/`BLOCKED` rules at the architecture level.
- Keeps the validator boundary Hexagonal and library-agnostic:
  `BigFraction` and the parser AST never leak into unrelated modules or
  contracts.

### Negative / Costs

- The restricted affine/linear-equation grammar cannot validate
  multi-variable, nonlinear, or higher-grade algebra; such content is
  `UNSUPPORTED` until a future validator family is added with evidence.
- Requires building and maintaining a small custom parser/normalizer
  rather than delegating to an existing general CAS.
- Resource-bound enforcement (token/nesting/digit limits) adds
  implementation and test surface beyond a naive parser.
- Validator/ruleset versioning and content-edit invalidation add
  bookkeeping that a "just re-run the AI" approach would not require.

## Alternatives Considered

1. **AI self-validation / LLM-as-judge for Mathematics correctness.**
   Rejected as a correctness authority; it may later assist
   explanation/evaluation research but never provides deterministic
   `VALID` status.
2. **General-purpose SymPy microservice.** Acknowledged as mature symbolic
   capability; rejected for the initial MVP because it requires a
   Python/service/runtime boundary, its broad capability exceeds pilot
   scope, and it creates additional network/failure/deployment complexity.
3. **Symja / general Java CAS.** Acknowledged as Java-native symbolic
   capability; rejected as the initial baseline because the pilot scope is
   narrow, broad CAS semantics/syntax are unnecessary, and it has a larger
   dependency/licensing/review surface than the bounded validator needs.
   Revisit if scope expands.
4. **Floating-point arithmetic.** Rejected as the authoritative
   fraction/algebra correctness basis.
5. **Handwritten rational arithmetic.** Acknowledged as simple for a tiny
   scope; a verified exact-rational library primitive is preferred over
   reimplementing core rational arithmetic without product benefit.
6. **External mathematical API.** Rejected because deterministic classroom
   assurance should not depend on network availability or third-party
   runtime authority for this MVP scope.

## Deferred Scope

This ADR does not scaffold source code, create contracts, create physical
schema, install dependencies, or select an AI provider. It does not define:
exact package/class names; the exact parser grammar (BNF/PEG) or its test
suite; the exact Apache Commons Numbers version pin; exact resource-limit
numeric values; exact JSON field names/JSON Schema for Mathematics claims
or results; or the property-based-testing library. Expanding validator
scope beyond `EXACT_RATIONAL`, `AFFINE_EXPRESSION`, and `LINEAR_EQUATION`
requires a future decision with validation evidence, not silent
generalization.

## Follow-Up Decisions

1. Verify the current compatible Apache Commons Numbers release and pin it
   through normal dependency governance during source scaffolding.
2. Define the exact parser grammar, package structure, and resource-limit
   values during source scaffolding, per ADR-0013's Hexagonal boundary.
3. Define field-level OpenAPI/JSON Schema for Mathematics claims and
   validation results when field-level contract work begins.
4. Build the versioned deterministic test corpora described above.
5. Resolve OAD-006 (AI provider/model), which now operates subordinate to
   this deterministic trust boundary rather than defining it.
6. Revisit whether a general CAS is justified only if supported
   Mathematics scope expands beyond the bounded validator families with
   measured evidence.

## Related Requirements / ADRs

- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./ADR-0004-ai-assurance-boundary.md)
- [ADR-0005 — Use Layered Curriculum Authority and Versioned Provenance](./ADR-0005-layered-curriculum-authority.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](./ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture](./ADR-0013-java21-module-first-hexagonal-backend.md)
- [ADR-0015 — Use a Versioned Controlled Curriculum Corpus with Deterministic Retrieval](./ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
- [Mathematics Assurance and Curriculum Grounding](../../01_features/mathematics-assurance.md)
- [Test Strategy](../../04_engineering/TEST_STRATEGY.md)
- [Dependency and Supply Chain Standard](../../standards/12_DEPENDENCY_SUPPLY_CHAIN.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-07` | Accepted | Resolve OAD-008 with scoped deterministic exact-rational and restricted affine/linear-equation validation |
