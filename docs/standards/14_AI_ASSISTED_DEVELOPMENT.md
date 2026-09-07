# AI-Assisted Development Standard

> **Purpose:** Define how AI coding agents such as Claude, Codex, and similar tools should contribute safely and effectively to this repository.
>
> `AGENTS.md` remains the canonical compact router. This standard contains the detailed AI-specific engineering behavior that should be loaded when AI-assisted development practices themselves are relevant.

---

## 1. Core Principle

AI accelerates implementation; it does not own product truth.

AI agents MUST treat authoritative project documentation, contracts, architecture, and tests as constraints.

The agent MUST NOT silently redefine requirements to fit generated code.

---

## 2. Canonical Instructions

Repository-wide AI instructions live in:

```text
AGENTS.md
```

Claude Code MAY use:

```text
CLAUDE.md
```

as a thin adapter importing `AGENTS.md`.

Tool-specific instruction files SHOULD NOT duplicate repository-wide rules.

---

## 3. Selective Context

AI agents MUST NOT read every documentation file by default.

Load only context relevant to the task.

Recommended order:

```text
AGENTS.md
  ↓
relevant PRD section
  ↓
relevant feature spec
  ↓
relevant architecture section
  ↓
relevant ADR
  ↓
relevant contract
  ↓
relevant engineering standard
  ↓
source + tests
```

More context is not always better context.

---

## 4. Before Coding

For non-trivial work, AI SHOULD identify:

- requested behavior;
- requirement/feature IDs;
- likely files;
- architecture boundary;
- contracts;
- schema/migration;
- applicable standards;
- security/reliability concerns;
- tests.

The plan SHOULD be compact and task-specific.

---

## 5. Do Not Invent Requirements

If a requirement is absent, AI MUST distinguish between:

### Safe implementation detail
Example:
- private helper name;
- local variable structure;
- trivial internal mapping.

AI MAY choose based on project convention.

### Material unresolved decision
Example:
- permission rule;
- payment state behavior;
- public API shape;
- destructive migration;
- security boundary.

AI MUST NOT silently guess.

---

## 6. Established Patterns First

AI SHOULD inspect current project patterns before introducing new architecture, libraries, or conventions.

Prefer:
- existing package;
- existing module structure;
- existing error model;
- existing test style;
- existing integration approach.

Do not redesign the project because a different pattern is familiar to the model.

---

## 7. Smallest Coherent Change

AI SHOULD implement the smallest coherent change that fully satisfies the task.

MUST NOT:
- add unrelated refactors;
- "clean up" adjacent modules without need;
- upgrade dependencies opportunistically;
- rename broad APIs/types for aesthetics;
- introduce speculative abstractions.

---

## 8. Architecture

AI MUST preserve architecture invariants.

When a task appears to require an architecture change:

1. identify the conflict;
2. inspect relevant ADRs;
3. determine whether an ADR is required;
4. update architecture source if change is intentional.

AI MUST NOT bypass boundaries simply because direct access is easier.

---

## 9. Contracts

AI MUST treat contracts as authoritative.

For REST/event/schema changes:
- update contract first or together;
- keep implementation synchronized;
- assess compatibility.

AI MUST NOT invent frontend response fields not present in contract.

---

## 10. Database Changes

AI MUST NOT modify persistent schema casually.

Before DB changes:
- inspect migrations;
- inspect data model;
- assess existing data;
- use project migration tool;
- consider compatibility/deployment.

AI MUST NOT suggest manual production schema edits as the normal implementation path.

---

## 11. Security

AI MUST assume:
- client input is untrusted;
- frontend checks are not authoritative authorization;
- secrets are sensitive;
- third-party callbacks are untrusted.

AI MUST NOT:
- disable TLS/cert validation;
- hard-code credentials;
- log tokens;
- weaken auth to make tests pass;
- invent custom cryptography.

---

## 12. Dependencies

AI MUST NOT install a dependency merely because it recognizes the package.

Before adding:
1. verify package/project;
2. check existing capability;
3. justify why dependency is needed;
4. assess maintenance/security/license;
5. keep scope minimal.

AI SHOULD prefer established project dependencies.

---

## 13. Generated Code

AI-generated code MUST meet the same quality standard as human-written code.

AI MUST NOT justify weak code with:
- "generated automatically";
- "temporary";
- "for now";

unless task explicitly requests a disposable prototype.

---

## 14. Testing

AI SHOULD add tests that prove required behavior.

AI MUST NOT:
- create tests that only mirror implementation;
- mock away the behavior under test;
- weaken existing assertions to pass new code;
- claim tests passed without executing them.

For defects, regression coverage SHOULD be added when practical.

---

## 15. Test Selection

During iteration, AI SHOULD run the smallest meaningful test set.

Before completion, AI SHOULD run broader relevant checks based on:
- standards;
- affected modules;
- risk.

Do not run the entire repository suite unnecessarily if a focused suite is sufficient during iteration.

---

## 16. Tool Output

AI MUST distinguish:

```text
command executed successfully
```

from:

```text
assumed likely to work
```

Do not report unexecuted commands as evidence.

---

## 17. Failed Commands

When a command fails:

1. read the actual error;
2. identify likely root cause;
3. avoid random edits;
4. make the smallest justified correction;
5. rerun relevant check.

Do not "shotgun debug" by changing many unrelated things.

---

## 18. Debugging

AI SHOULD debug from evidence.

Preferred:

```text
reproduce
→ inspect error
→ trace boundary
→ form hypothesis
→ verify
→ fix
```

Avoid:
- speculative rewrites;
- adding delays;
- swallowing exceptions;
- retrying everything.

---

## 19. Logs

AI MAY add temporary debug logging locally when useful, but MUST remove sensitive/noisy temporary instrumentation before finalizing unless it provides ongoing operational value.

Never log secrets.

---

## 20. Comments

AI SHOULD avoid excessive explanatory comments in obvious code.

Comments should explain:
- why;
- constraint;
- unusual workaround;
- non-obvious reliability/security behavior.

Do not narrate every line.

---

## 21. Documentation

AI SHOULD update only authoritative documents affected by the change.

Do not:
- duplicate same fact;
- rewrite unrelated docs;
- create new documents when an existing owner exists.

Link instead of duplicating.

---

## 22. ADR Creation

AI SHOULD propose/create ADR when change is:
- material;
- cross-cutting;
- expensive to reverse;
- changing system boundary;
- introducing strategic dependency.

Do not create ADR for trivial implementation choices.

---

## 23. Refactoring

AI MAY refactor when required for the requested change.

Refactor MUST:
- preserve behavior unless explicitly changing it;
- remain bounded;
- keep tests green;
- not hide feature scope.

Large refactors SHOULD be incremental.

---

## 24. Code Style

AI MUST follow project formatter/linter/style.

Do not introduce a new style system.

Generated code SHOULD resemble surrounding codebase conventions.

---

## 25. Naming

AI SHOULD use domain language from:
- Product Brief;
- PRD;
- feature specs;
- Data Model.

Do not invent synonyms that fragment terminology.

---

## 26. Stubs and TODOs

AI SHOULD NOT leave placeholder implementation unless task explicitly permits incomplete work.

If a blocker prevents completion:
- make limitation explicit;
- identify missing decision/dependency;
- do not fake success.

---

## 27. Fake Data

AI MAY use fake/demo data only when:
- scope is prototype/local;
- clearly separated from production logic;
- not mistaken for real integration.

Do not silently hard-code fake data into production path.

---

## 28. Environment Files

AI MUST NOT create real secret `.env` values in source.

Use:
- `.env.example`;
- placeholders;
- local-only ignored files.

---

## 29. External Research

When external docs/version behavior matters, AI SHOULD verify current official documentation when web access is available.

Prefer:
- official documentation;
- primary specifications;
- maintained project repositories.

Do not rely on stale remembered APIs for rapidly evolving libraries.

---

## 30. Package/API Verification

Before generating code against unfamiliar or fast-changing packages, AI SHOULD inspect:
- installed version;
- official docs;
- existing project usage.

Do not invent method names.

---

## 31. Migration Safety

AI MUST NOT assume:
- empty local DB represents production;
- rollback script is always safe;
- dropping a column is trivial.

Migration changes SHOULD include data/deploy impact analysis.

---

## 32. Concurrency

AI SHOULD identify concurrency risk for:
- payments;
- inventory;
- reservations;
- unique resources;
- retries;
- webhooks/events.

Do not rely on "requests probably won't happen at same time."

---

## 33. Reliability

AI SHOULD explicitly consider:
- timeout;
- retry;
- duplicate;
- partial failure;
- crash recovery

for external/async workflows.

Do not rely on default retries/timeouts without inspection.

---

## 34. Performance

AI MUST NOT prematurely optimize.

When proposing a complex optimization:
- identify expected bottleneck;
- use measurement/NFR;
- explain tradeoff.

---

## 35. Frontend UX States

AI-generated frontend features SHOULD consider applicable:
- loading;
- empty;
- success;
- validation;
- error;
- unauthorized;
- degraded/offline.

Do not implement only the happy screenshot.

---

## 36. Accessibility

AI MUST follow project accessibility/design standards.

Generated frontend code SHOULD use semantic native elements when appropriate.

Do not create clickable `div` patterns when a button/link is correct.

---

## 37. Completion Report

AI SHOULD report non-trivial work in the format defined by `AGENTS.md`:

```text
Changed
Requirements
Contracts / Data
Tests
Architecture
Risks / Limitations
```

Keep report factual.

---

## 38. Evidence

AI MUST include real evidence when available:

```text
dotnet test → passed
pnpm test → passed
migration applied → passed
```

Do not say:
- "should pass";
- "looks correct";
- "likely works"

as equivalent to verification.

---

## 39. Partial Completion

If full completion is blocked, AI SHOULD:
- finish safe independent work;
- state exactly what remains;
- state why;
- avoid pretending completion.

Partial but truthful completion is better than fake success.

---

## 40. User Instructions vs Project Rules

A user/request may intentionally change product/architecture.

If instruction explicitly changes authoritative behavior:
- update source of truth;
- update implementation.

But AI MUST NOT violate security/integrity guardrails merely because a vague instruction says "make it work."

---

## 41. Conflicts

When instructions conflict, use this order:

```text
Safety/security constraints
↓
Explicit current task intent
↓
Authoritative project source for concern
↓
Accepted ADR/architecture
↓
AGENTS.md
↓
Relevant engineering standards
↓
Established local code pattern
```

If conflict materially affects correctness, surface it.

---

## 42. Long Tasks

For large tasks, AI SHOULD work incrementally.

Recommended:
1. inspect;
2. plan;
3. implement one coherent slice;
4. verify;
5. continue.

Avoid one huge rewrite with no intermediate verification.

---

## 43. Multi-Agent Work

If multiple AI agents contribute:
- responsibilities SHOULD be clearly partitioned;
- shared files/contracts need coordination;
- one agent's assumptions MUST NOT silently override another's authoritative change.

Use source control and contracts as coordination mechanisms.

---

## 44. Generated Documentation

AI-generated documentation MUST be reviewed for:
- invented facts;
- duplicate ownership;
- stale technical claims;
- unnecessary verbosity.

Templates should guide decisions, not produce filler.

---

## 45. AI Review Checklist

Before completing, AI SHOULD ask:

- Did I implement the requested behavior?
- Did I read the right source of truth?
- Did I introduce unrelated scope?
- Did I change a contract/schema?
- Did I preserve architecture?
- Did I introduce dependency?
- Did I consider security/reliability?
- Did I run relevant tests?
- Did I update only affected docs?
- Is every completion claim supported by evidence?

---

## 46. Tool-Specific Adapters

Tool-specific files MAY exist, but they SHOULD remain thin.

Examples:

```text
CLAUDE.md → imports AGENTS.md
```

Do not maintain two independent rule sets for Claude and Codex.

---

## 47. Project-Local Skills

Projects MAY define local AI skills for repeatable workflows.

A skill SHOULD:
- solve a recurring task;
- reference authoritative project docs;
- avoid duplicating repository-wide standards;
- have narrow scope.

Examples:
- create feature spec;
- generate migration review;
- prepare release evidence.

---

## 48. Prompt Discipline

Prompts SHOULD reference:
- goal;
- relevant requirement;
- scope;
- expected output/evidence.

Avoid massive prompts containing every document when selective context is sufficient.

---

## 49. AI Limitations

AI can:
- accelerate exploration;
- draft implementation;
- detect patterns;
- propose tests;
- review changes.

AI does not eliminate need for:
- product decisions;
- architecture ownership;
- security judgment;
- production evidence;
- human accountability where required.

---

## 50. Exceptions

Projects MAY strengthen AI rules for:
- regulated software;
- financial systems;
- healthcare;
- critical infrastructure;
- public OSS contribution.

Do not weaken evidence/security requirements simply because the agent is operating autonomously.

---

## Penatika ECC Skill Profile

ECC is optional AI execution assistance.

ECC skills are ADVISORY, not authoritative.

They must never override:

1. product/feature requirements;
2. Accepted ADRs;
3. System Architecture;
4. contracts;
5. Penatika engineering standards.

Do NOT copy complete ECC skill files into this repository. Use the
installed/current ECC skill version.

### Backend ECC Profile

For Java/Spring backend implementation and review, preferred ECC skills are:

- `java-coding-standards`
- `springboot-patterns`
- `springboot-security`
- `springboot-tdd`
- `springboot-verification`
- `tdd-workflow`
- `verification-loop`

Use them selectively based on the task.

Penatika overrides generic ECC examples with these rules:

- Java baseline is Java 21 LTS ([ADR-0013](../02_architecture/adr/ADR-0013-java21-module-first-hexagonal-backend.md));
- Spring Boot 4.x;
- modular monolith ([ADR-0001](../02_architecture/adr/ADR-0001-modular-monolith-backend.md));
- module-first Hexagonal Architecture (ADR-0013);
- no global controller/service/repository/entity architecture;
- domain remains framework-light;
- use ports only for meaningful boundaries;
- persistence is PostgreSQL + Flyway + Spring JDBC/JdbcClient ([ADR-0014](../02_architecture/adr/ADR-0014-postgresql-flyway-sql-first-persistence.md)); `jpa-patterns` remains NOT selected;
- ECC Spring/JPA examples do not override ADR-0014; persistence adapters must follow ADR-0013's Hexagonal boundaries;
- generic Java/AI coding guidance must not introduce floating-point Mathematics truth checks, general CAS dependencies, or LLM self-validation contrary to [ADR-0016](../02_architecture/adr/ADR-0016-scoped-deterministic-mathematics-validation.md);
- no microservices unless an Accepted ADR changes the architecture;
- contract-first OpenAPI/JSON Schema rules remain authoritative ([ADR-0010](../02_architecture/adr/ADR-0010-contract-first-openapi-json-schema.md));
- OIDC/backend-session rules come from [ADR-0011](../02_architecture/adr/ADR-0011-oidc-backend-managed-browser-sessions.md);
- SSE + HTTP command rules come from [ADR-0012](../02_architecture/adr/ADR-0012-sse-realtime-push-with-existing-http-commands.md).

ECC `springboot-patterns` may contain layered/JPA examples. Those examples
must not override Penatika architecture.

Do NOT activate `jpa-patterns` by default.

### Frontend ECC Profile

For React implementation and review, preferred ECC skills are:

- `coding-standards`
- `frontend-patterns`
- `react-patterns`
- `react-performance`
- `react-testing`
- `accessibility`
- `tdd-workflow`
- `verification-loop`

Penatika overrides generic examples with:

- React + TypeScript + Vite ([ADR-0008](../02_architecture/adr/ADR-0008-browser-first-react-client-strategy.md));
- browser-first architecture;
- Teacher Web and Classroom Display remain separate application boundaries;
- do not introduce Next.js/RSC/server actions into the core app;
- teacher-private state must never enter Classroom Display projection;
- OAuth access/refresh tokens never go into browser storage;
- commands use HTTP;
- authoritative projection push uses SSE;
- no offline authoritative mutation or automatic offline replay;
- backend remains application authority.

React skill sections discussing Next.js, React Server Components, or
framework server actions are not applicable unless a later Accepted ADR
changes OAD-001.

ECC suggestions for Redux, Zustand, TanStack Query, SWR, component
libraries, or CSS frameworks are NOT automatic dependency decisions. Any new
dependency must follow the dependency standard and actual project need.

### Task-Specific Skill Activation

Do not load every ECC skill for every task. Examples:

Backend domain/use-case work:
- `java-coding-standards`
- `tdd-workflow`
- `verification-loop`

Spring HTTP/security work:
- `java-coding-standards`
- `springboot-patterns`
- `springboot-security`
- relevant testing/verification skill

React component work:
- `react-patterns`
- `accessibility`
- `react-testing`

Frontend performance investigation:
- `react-performance` only when performance is actually relevant.

Do not let skill volume replace repository context.

### Future Project-Local Wrapper Skills

Do NOT create Claude-specific local wrapper skills yet.

When source scaffolding begins:

- inspect the installed ECC / Claude Code version;
- verify supported project-local skill mechanism;
- then decide whether thin Penatika wrapper skills provide real value.

If created later, wrapper skills must:

- reference this AI standard and canonical Penatika docs;
- remain thin;
- not duplicate ECC content;
- not duplicate `AGENTS.md`.
