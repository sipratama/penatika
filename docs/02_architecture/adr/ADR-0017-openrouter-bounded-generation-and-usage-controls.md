# ADR-0017 — Use OpenRouter for Bounded Generative AI with Scope, Quota, and Privacy Routing Controls

| Field | Value |
|---|---|
| ADR | `ADR-0017` |
| Status | Accepted |
| Date | `2026-09-07` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-LESSON-001`, `CAP-ADAPT-001`, `CAP-MATH-001`, `PR-005`, `PR-015`, `PR-016`, `PR-018`, `PR-019`, `PR-025`, `PR-053`, `PR-054`, `PR-059`, `PR-060`, `PR-061`, `PR-062` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

OAD-006 selects the AI provider/model strategy and fallback approach. This decision also resolves a planning concern already recorded against OAD-006 in `docs/PROJECT_STATUS.md`: the pre-provider request-scope and budget-control boundary, AI quota, AI resource/cost control, and per-teacher usage accounting.

[ADR-0004](./ADR-0004-ai-assurance-boundary.md) already establishes that AI providers produce untrusted proposals and that independent modules own schema/policy, Mathematics, and curriculum authority. [ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md) already separates generation authorization from publication authorization through five execution classes. [ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md) already defines how AI-provider failure degrades only the affected capability. [ADR-0010](./ADR-0010-contract-first-openapi-json-schema.md) establishes contract-first boundaries without yet defining AI proposal fields. [ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) establishes module-first Hexagonal Architecture and the rule that ports exist only for meaningful boundaries. [ADR-0015](./ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md) and [ADR-0016](./ADR-0016-scoped-deterministic-mathematics-validation.md) already establish that curriculum grounding and Mathematics correctness are independent of any AI provider. None of these decisions selected a concrete generative-model provider/gateway, a model-routing strategy, or the pre-provider scope/resource/allowance boundary that must exist before an expensive model call — that is this decision.

The Product Owner has explicitly required that Penatika AI must not become a general-purpose chatbot, that unsupported requests must not consume expensive model generation, that every teacher's AI usage can be bounded, that teachers can understand their available AI allowance, and that authorized Penatika operations can understand aggregate and per-teacher AI usage and provider cost. [BUSINESS_MODEL.md](../../00_product/BUSINESS_MODEL.md) already requires bounded free-tier AI usage that degrades gracefully rather than corrupting a session (`PR-053`) and requires that safety/trust controls never become paid-only (`PR-054`); this decision defines the architecture that makes those product rules enforceable.

This ADR does not scaffold source, install an AI SDK, create API keys, create database schema, create Flyway migrations, create OpenAPI/JSON Schema field contracts, create prompt files, call OpenRouter, resolve OAD-007 (speech recognition), resolve OAD-010 (deployment), or activate OAD-011 (a background queue).

## Decision

Penatika selects **OpenRouter** as its initial controlled generative-model gateway. Penatika does not depend directly on one model provider API. The application depends on a Penatika-owned port; an adapter translates to OpenRouter.

```text
Lesson / AI Orchestration
        ↓
Penatika-owned GenerativeModelPort
        ↑
OpenRouterGenerationAdapter
        ↓
OpenRouter
        ↓
explicit approved model
        ↓
explicit approved provider route
```

OpenRouter is an outbound infrastructure adapter, a model/provider gateway, and a billing/routing control plane. OpenRouter is **not** application authority, curriculum authority, Mathematics authority, authorization authority, product scope authority, or teacher-publication authority. Every one of those authorities remains owned by the modules ADR-0004/ADR-0006/ADR-0015/ADR-0016 already establish.

Before any expensive model call, a Penatika-owned pipeline enforces authentication, authorization, entitlement, hard resource bounds, capability/scope classification, per-teacher allowance, and concurrency/idempotency — all subordinate to, and independent of, the OpenRouter gateway itself.

## Bounded AI Product Boundary

Penatika AI is a **bounded Mathematics-teaching capability**, not a general-purpose chatbot, coding assistant, website builder, software generator, arbitrary research assistant, or generic content factory. Subject relevance alone does not establish capability support.

| Request | Classification |
|---|---|
| "Create five equivalent-fraction examples for Grade 5" | `IN_SCOPE` when other conditions pass |
| "Explain this current linear-equation example more simply" | `IN_SCOPE` when supported |
| "Create a website for learning fractions" | `OUT_OF_SCOPE` / `UNSUPPORTED_CAPABILITY` |
| "Generate React code for an algebra game" | `OUT_OF_SCOPE` |
| "Write a Java application for Mathematics" | `OUT_OF_SCOPE` |
| "Explain the Majapahit kingdom" | `OUT_OF_SCOPE` |
| "Generate 10,000 fraction questions" | in-domain capability, but `RESOURCE_LIMIT_EXCEEDED` |

Scope and quota are separate, independent questions:

- **Scope:** "Is this task a supported Penatika capability?"
- **Quota/resource:** "May this teacher perform this supported task at this size, right now?"

A request can pass scope and fail resource (10,000 fraction questions), or fail scope regardless of remaining allowance (a React Mathematics app consumes no main-generation quota). This separation exists because valid quota must not be wasted on unrelated work, and because a generous allowance must never become a general-purpose-chatbot allowance.

## OpenRouter Gateway

OpenRouter provides one model gateway, a unified API surface, unified inference billing, provider/model flexibility, provider routing controls, provider allowlists, Zero Data Retention (ZDR) filtering, data-collection filtering, price controls, parameter-capability filtering, API-key spending controls, and usage/cost visibility. This lets Penatika evaluate and replace approved models without making the application/domain layer provider-specific. The additional gateway/data-processor/network-hop cost is accepted because it provides materially useful routing, governance, privacy, evaluation, and cost controls for the current small Penatika team.

The initial integration baseline is the **OpenRouter OpenAI-compatible Chat Completions API** (conceptually `POST /api/v1/chat/completions`), chosen for broad model/provider compatibility, structured-output support, and model portability across OpenRouter, without making OpenAI's native Responses API a Penatika architecture dependency.

OpenRouter API request/response types are not exposed outside the adapter. The application layer does not depend on OpenRouter SDK models, OpenAI `ChatCompletion` classes, or other provider-specific DTOs. The exact Java HTTP/SDK implementation remains source-scaffolding work; **Spring AI, LangChain4j, and the OpenAI Java SDK are not selected** by this ADR (see "Guardrails AI Decision" and "Alternatives Considered" below for the framework rationale).

## Hexagonal Gateway Boundary

[ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) remains binding:

```text
AI application use case
        ↓
GenerativeModelPort
        ↑
OpenRouterGenerationAdapter
        ↓
OpenRouter API
```

The application port expresses Penatika semantics. It must not expose raw model slugs, OpenRouter provider objects, token API details, OpenRouter headers, or gateway error classes. Provider/gateway-specific translation belongs inside the adapter. Penatika already owns the Hexagonal `GenerativeModelPort`; a framework (Spring AI, LangChain4j) is not adopted as the canonical provider abstraction, and may be reconsidered later only if it produces clear implementation value without replacing Penatika-owned contracts, scope policy, quota policy, generation policy, or the assurance boundary.

## Model Profiles

Penatika owns semantic model profiles, not raw model selection. Initial profiles are `ROUTER`, `FAST`, and `QUALITY`. Initial model bindings at decision time:

| Profile | Initial Binding | Purpose |
|---|---|---|
| `ROUTER` | `openai/gpt-5.6-luna` | Genuinely ambiguous free-text capability classification only, after deterministic rules cannot decide; minimal context; never generates lesson content |
| `FAST` | `openai/gpt-5.6-luna` | Bounded low-complexity live generation where evaluation shows the model is adequate (another similar example, change numbers, shorter explanation, another bounded practice question, simple supported adaptation) |
| `QUALITY` | `openai/gpt-5.6-terra` | Larger semantic generation (initial lesson generation, substantial lesson-section generation, complex explanation restructuring) |

These slugs are current OpenRouter catalog identifiers at decision time. They are initial evaluation/implementation bindings, not permanent architecture identities (see "Model Change Governance"). Model selection is never exposed to users.

Penatika does not select `openrouter/auto-beta` or another automatic task-to-model router, and does not use `openrouter/free` for pilot or production behavior. Model selection affects quality, cost, evaluation evidence, reproducibility, and assurance expectations; the gateway must not silently choose a model Penatika has not evaluated, and a random/changing free-model selection is incompatible with controlled model evaluation, version traceability, reproducibility, and predictable quality. `openrouter/free` may be useful for developer experimentation outside authoritative Penatika evaluation, but is never a pilot/production generation path.

## Server-Controlled Model Routing

Browser/teacher input must not choose model, provider, provider route, reasoning level, output-token limit, temperature, tools, web search, file search, or code execution. The Penatika backend resolves the applicable `GenerationPolicy` for a supported use case. Penatika does not expose a generic API such as:

```json
POST /ai
{ "model": "...", "prompt": "...", "maxTokens": ... }
```

Public/application APIs represent supported Penatika use cases, not a generic model-invocation surface.

## Pre-Provider Request Pipeline

Every generation request follows this conceptual sequence:

```text
Teacher Request
        ↓
Authentication
        ↓
Authorization
        ↓
Entitlement / pilot / tier policy
        ↓
Hard Resource Guard
        ↓
Deterministic Capability Guard
        ↓
Optional bounded ROUTER classification
        ↓
Scope Decision
        ↓
Per-Teacher AI Allowance Check
        ↓
Atomic Usage/Budget Reservation
        ↓
Concurrency / Idempotency Check
        ↓
Model Profile Resolution
        ↓
Approved OpenRouter Route Resolution
        ↓
Context Minimization
        ↓
OpenRouter
        ↓
Structured Proposal
        ↓
Canonical JSON Schema Validation
        ↓
Curriculum Assurance
        ↓
Mathematics Assurance
        ↓
Publication Policy
        ↓
Teacher-private Preview
        ↓
Teacher Approval
        ↓
Authoritative Application Command
```

An expensive `FAST`/`QUALITY` call must not occur before all applicable authorization, scope, resource, allowance, and concurrency checks have passed.

## Scope / Capability Guard

Penatika owns a supported AI capability policy. The scope decision considers the requested action, requested artifact, current product use case, Mathematics teaching context, and supported Penatika structured output types. A substring check such as `contains("mathematics") → allowed` is not a policy, and a blacklist alone is not a policy; the policy is capability-oriented.

When a request is clearly unsupported before model invocation, Penatika rejects it locally. `ROUTER` is not invoked merely to produce a polite rejection, and `FAST`/`QUALITY` are not invoked. The product may provide a deterministic helpful redirect, for example: "This request is outside Penatika's supported teaching capabilities. You can ask me to create a supported classroom example, question, explanation, or visual instead." Exact UX copy is deferred to implementation.

## Ambiguous Intent Router

Only when deterministic scope policy cannot safely classify free text does Penatika invoke `ROUTER` (initial binding `openai/gpt-5.6-luna`). `ROUTER` receives only the minimal input needed for classification — never the entire lesson, full classroom history, full curriculum corpus, teacher profile, raw audio, or provider tools. Its conceptual output is one of `SUPPORTED`, `OUT_OF_SCOPE`, `UNSUPPORTED_CAPABILITY`, or `NEEDS_CLARIFICATION`, returned as strict structured output with a small generation/token budget (exact limit remains configuration). `ROUTER` is a cost-bearing call, but it protects much larger `FAST`/`QUALITY` spend.

`ROUTER` does not decide authorization, quota, Mathematics correctness, curriculum truth, publication, teacher approval, or security policy — it assists only with ambiguous capability classification. Every other boundary in this ADR and in ADR-0004/ADR-0006/ADR-0015/ADR-0016 remains binding even if `ROUTER` misclassifies a request.

## Hard Resource Guard

Before the expensive model call, deterministic controls must be able to reject requests based on categories such as teacher-input length, bounded context size, requested generated-item quantity, unsupported artifact type, request rate, concurrent generation count, remaining AI allowance, and application-level cost/resource ceiling. Exact numeric thresholds are not selected here — they are evidence-driven configuration — but the enforcement architecture itself is mandatory and sits before capability classification and provider invocation in the pipeline above.

## Per-Teacher AI Allowance

Penatika owns per-teacher usage enforcement. An OpenRouter API key per teacher is not the product quota model.

```text
TeacherAccount
        ↓
AIAllowanceWindow
        ↓
AIUsageReservation
        ↓
AIUsageEvent
```

Penatika supports a **daily** teacher AI allowance window. The exact numeric allowance is not selected by this ADR. Raw provider tokens are not the required teacher-facing product unit, because provider token cost differs substantially between models and capabilities; Penatika may expose internal AI allowance units, capability-specific counts, or another simple approved product representation, with different resource weight for different capabilities (for example, a `FAST` request carries a lower resource weight than a `QUALITY` lesson generation). Exact UX representation and numeric weighting remain a later product/configuration decision; this ADR does not lock numeric weights.

## Teacher Usage Visibility

Product behavior must support teacher-visible AI allowance. A teacher must be able to understand, without needing to understand provider-token billing: allowance used, allowance remaining, exhausted/not-exhausted state, and the next reset time/window for the current daily window. OpenRouter credit balance, raw API-key limits, and provider internals are not exposed as the product quota.

Penatika must also support privacy-minimized operational visibility for the Product Owner / authorized internal operations. Aggregate visibility conceptually includes active teachers using AI, AI requests, successful generations, scope rejections, quota/resource rejections, failed generations, input/output tokens, provider cost, and cost by capability/profile. Per-teacher visibility conceptually includes the internal `TeacherAccount` identity/reference, allowance used/remaining, request count, model-profile usage, token usage, provider-cost attribution, and rejection/failure categories. This does not create a school-administrator product role, and it does not expose teacher-private prompt/content unnecessarily. Exact internal admin authorization/UI/tooling remains deferred.

## Usage Reservation / Concurrency

Before an expensive generation call, Penatika atomically reserves sufficient AI allowance:

```text
check allowance
        ↓
reserve bounded allowance
        ↓
call provider
        ↓
receive actual usage/cost
        ↓
reconcile reservation
```

This prevents concurrent requests from observing the same remaining allowance. [ADR-0014](./ADR-0014-postgresql-flyway-sql-first-persistence.md) PostgreSQL remains the eventual authoritative persistence boundary for this reservation state; no physical tables are created by this ADR.

If a request is rejected before provider invocation, no expensive-generation allowance is consumed. If a reserved request definitively fails before provider usage, the reservation may be released according to policy. If provider usage outcome is uncertain, Penatika does not blindly assume zero cost — it records a pending/conservative state until usage can be reconciled where possible. The exact reconciliation state machine remains implementation work.

Backend-enforced concurrency bounds are mandatory: a teacher must not be able to open many browser tabs or rapidly repeat actions to create unlimited simultaneous expensive requests, and repeated taps/voice requests during active classroom generation must not create uncontrolled spend, stale competing proposals, or duplicate provider calls. Exact concurrency count remains configuration.

Each expensive generation attempt has application-owned identity. A network or browser retry must not automatically create a second expensive provider request for the same accepted request while the existing generation is still known/in-flight. The provider request ID is not treated as the application generation identity. Exact idempotency contract remains field-level design work.

## Usage Ledger / Reconciliation

Penatika conceptually defines `AIAllowanceWindow`, `AIUsageReservation`, and `AIUsageEvent`. An `AIUsageEvent` may retain privacy-minimized metadata such as teacher/account internal reference, generation identity, capability, model profile, model slug, actual provider route where available, status, input token count, output token count, gateway-reported cost, reservation/reconciliation result, retry state, rejection category, and timestamp. Full prompt, full provider response, and raw audio are not required to be stored. Applicable metadata follows [DATA_RETENTION_POLICY.md](../../06_delivery/DATA_RETENTION_POLICY.md).

OpenRouter Activity/usage information is an infrastructure reconciliation source, not the product source of truth. Penatika's internal usage ledger is the product source of truth for `TeacherAccount` quota, capability usage, and daily allowance; OpenRouter is the provider/gateway source for billed token usage, model/provider usage, and gateway spend:

```text
Penatika AI Usage Ledger
        ↕ reconcile
OpenRouter Usage / Activity
```

Large unexplained differences should become an operational investigation signal. Reconciliation implementation is deferred.

## OpenRouter Route Policy

OpenRouter's default provider routing is not accepted implicitly. Every production/pilot generation profile must resolve to an explicit Penatika-approved route policy conceptually including: model slug; approved provider endpoint/provider allowlist; ZDR requirement; data-collection policy; fallback behavior; required parameter support; and price ceiling. No provider becomes approved simply because OpenRouter can route to it.

Penatika uses an explicit provider allowlist/order. Exact provider endpoint identities are activated only after privacy review, data-retention review, structured-output compatibility review, model evaluation, latency review, and cost review; "any provider serving this model" is not an acceptable policy. The active provider route must be visible in configuration/evaluation evidence.

OpenRouter's generic automatic provider fallback is not used for MVP; unapproved fallback behavior is explicitly disabled (conceptually `allow_fallbacks = false`). A failed Provider A never silently routes to a never-approved Provider B. The primary fallback policy remains graceful degradation per [ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md). A future architecture revision may allow multiple approved provider endpoints for the same evaluated model only if all candidate routes have independently passed privacy/data-processing review, structured-output compatibility, quality/evaluation requirements, cost limits, and operational review; this is not enabled now.

Penatika uses OpenRouter routing price controls as defense-in-depth. Each generation profile should eventually have an accepted maximum provider price ceiling (conceptually `max_price` or an equivalent current routing control). Exact numeric ceiling remains configuration derived from current model pricing, unit economics, and pilot evidence; today's token prices are not frozen into architecture. If all eligible routes exceed the configured price ceiling, generation fails/degrades rather than silently using an unexpectedly expensive route.

## Provider Privacy Routing

For Penatika generative traffic, the initial privacy routing baseline requires `zdr = true` and `data_collection = deny`. Requests that depend on structured-output/schema parameters additionally require `require_parameters = true`. If no provider endpoint satisfies the active requirements, the generation fails; Penatika does not silently weaken ZDR, data-collection policy, or structured-output capability to obtain a response. Provider data policy is part of route eligibility, not an optional filter.

`zdr = true` does not equal complete compliance. OpenRouter is an additional data processor/gateway. Before pilot/production activation, review OpenRouter's terms/privacy, the selected upstream provider, the provider route, data retention, data collection/training behavior, relevant processing region, applicable subprocessors, and Penatika's own retention/privacy policy. If no route satisfies applicable Penatika policy, AI generation activation is blocked; architecture cannot silently weaken product policy.

OpenRouter prompt/response logging remains disabled by default. Penatika does not opt in merely for easier debugging; synthetic tests, sanitized evaluation fixtures, metadata, and bounded diagnostics are used instead. Any future prompt logging requires explicit privacy/retention review.

## Structured Output

Supported semantic generation uses structured output with JSON Schema. OpenRouter/provider schema configuration is derived from the Penatika canonical JSON Schema per [ADR-0010](./ADR-0010-contract-first-openapi-json-schema.md); the canonical schema remains Penatika-owned. If the gateway/provider supports only a subset, Penatika derives an adapter-compatible representation. After generation, Penatika always performs canonical schema validation again — gateway-level structured output is defense-in-depth, not final validation authority. For structured generation routes, Penatika uses the OpenRouter capability filter equivalent to `require_parameters = true` so a route that cannot honor required structured-output parameters is not silently selected; this does not replace application-side canonical schema validation.

AI output contracts represent only supported Penatika structured content. Penatika does not introduce generic fields for arbitrary HTML, JavaScript, React, Java, code, executable script, iframes, or external interactive applications. If an out-of-scope code request reaches the model due to a classification error, the output contract still provides no way to turn that request into executable classroom content.

Penatika does not enable gateway/provider tools for MVP generative paths, including web search, file search, web browsing, code execution, computer use, MCP, or arbitrary tools/plugins. Penatika already owns curriculum retrieval, Mathematics validation, and supported application actions; provider tools would expand product scope, cost, attack surface, data-processing surface, and prompt-injection surface without product benefit.

## Context Minimization

Provider calls receive the minimum necessary context: normalized teacher intent, grade/topic, relevant lesson section, relevant current scene subset, relevant `CurriculumContextBundle`, supported output schema, and generation policy. Penatika does not automatically send the entire lesson history, every previous teacher interaction, complete session history, the entire curriculum corpus, unrelated teacher local context, the complete account profile, or raw audio. Context minimization protects cost, latency, privacy, and prompt-injection surface.

Raw push-to-talk audio is not sent to OpenRouter generative models. OAD-007 will define Speech Recognition; the future conceptual flow is raw audio → speech recognition → bounded transcript/normalized request → Penatika scope/resource guards → OpenRouter generation if permitted. This ADR does not resolve the speech provider.

Penatika owns application/session context and does not rely on gateway/model conversation memory as authoritative state. Each semantic generation is reconstructable from explicit bounded Penatika context; Penatika does not create provider-hosted agent threads or autonomous memory.

## Prompt / Generation Policy Versioning

Prompt behavior is Penatika-owned and versioned as a conceptual `PromptPolicyVersion` (for example, `lesson-generation/v1`, `live-adaptation/v1`, `simplify-explanation/v1`, `scope-router/v1`). No actual prompt files are created by this ADR; important prompts must not be scattered as arbitrary strings in controllers.

A conceptual `GenerationPolicyVersion` may bind supported capability, model profile, context-selection strategy, `PromptPolicyVersion`, output-schema version, output bounds, routing policy, retry policy, and evaluation status — prompt text alone is not the entire generation policy.

Where required for evaluation/operations, a generation can be traced to: Penatika generation identity; supported capability; model profile; OpenRouter model slug; actual upstream provider/route when returned; `PromptPolicyVersion`; `GenerationPolicyVersion`; output schema version; curriculum corpus version where applicable; Mathematics validator version where applicable; and usage/cost metadata. Full prompt/response bodies are not persisted merely for traceability.

Model profile bindings are configuration governed by evaluation. Changing `openai/gpt-5.6-luna` or `openai/gpt-5.6-terra` to another approved model does not necessarily require a new architecture ADR when OpenRouter remains the gateway, the capability boundary is unchanged, privacy routing stays compliant, the candidate passes required Penatika evaluation, and the model/profile change is recorded. Material changes — gateway replacement, autonomous agent architecture, new provider tools, automatic unreviewed model routing, or multi-provider automatic fallback — require architecture/security review.

## Curriculum Authority Boundary

[ADR-0015](./ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md) remains binding. OpenRouter/model knowledge is never curriculum authority; the model receives only controlled relevant curriculum context supplied by the Curriculum module. A curriculum `NO_MATCH`/`UNGROUNDED` result does not trigger "ask the model from memory."

## Mathematics Authority Boundary

[ADR-0016](./ADR-0016-scoped-deterministic-mathematics-validation.md) remains binding. OpenRouter/model output cannot self-validate Mathematics. A known `INVALID` result is `BLOCKED`; `UNSUPPORTED`/`INCONCLUSIVE` follow existing warning/policy semantics. Penatika does not retry different models until one produces an answer that passes deterministic validation — that is validation shopping, and it is prohibited.

## Publication Boundary

[ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md) remains binding. Generation authorization does not mean publication authorization. OpenRouter output remains a non-authoritative proposal and must pass applicable canonical schema, policy, curriculum, and Mathematics checks before teacher-private preview/publication handling.

## Retry / Fallback / Degradation

Retries must be bounded, transient-failure-specific, duplicate-aware, and cost-aware. Penatika does not retry because a request is `OUT_OF_SCOPE`, capability is unsupported, allowance is exhausted, Mathematics is `INVALID`, curriculum is `NO_MATCH`, policy is `BLOCKED`, or the teacher rejected a proposal — these are not provider-availability failures, and uncontrolled retry loops are not created.

The initial fallback policy is **graceful degradation**: there is no automatic OpenAI model → Gemini → Claude → another model routing, and no automatic Provider A → unapproved Provider B routing for MVP. Current reviewed classroom content and deterministic teaching controls remain usable according to [ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md).

## Model / Route Evaluation

Before a model/profile/provider route is activated for pilot/production, it is evaluated using Penatika's versioned evaluation corpus, covering Bahasa Indonesia teaching quality, grade appropriateness, topic appropriateness, structured-output adherence, capability adherence, out-of-scope refusal/classification, prompt-injection resistance, hallucinated curriculum claims, Mathematics mistakes caught/missed by deterministic assurance, latency, input tokens, output tokens, provider cost, refusal/failure behavior, and provider-route behavior. `ROUTER` additionally requires classification evaluation. This ADR does not invent arbitrary global pass scores.

## Cost / Usage Telemetry

OpenRouter API-key spending limits may be used as a second-line cost circuit breaker; they are not the teacher quota source of truth. Penatika's per-teacher allowance is the product authority, while an OpenRouter API-key limit is an infrastructure safety ceiling — separate keys/limits by environment or workload are preferred only when operationally useful, and one OpenRouter key per teacher is not created for MVP. Exact API-key topology belongs to OAD-010/implementation configuration.

Application architecture must not assume unlimited OpenRouter credit. For development/pilot, operational policy should be capable of using a finite prepaid balance, disabled or explicitly bounded auto-recharge, and gateway spend alerts/limits. Exact purchased credit amount and recharge policy are operational/commercial configuration, not architecture. Aggregate and per-teacher cost telemetry follow "Teacher Usage Visibility" and "Usage Ledger / Reconciliation" above.

## Gateway Secret / Operational Controls

OpenRouter API credentials are server-side secrets and are never exposed to Teacher Web, Classroom Display, browser JavaScript, application API responses, or logs. The exact secret-management system belongs to OAD-010; no credentials or environment-variable names are created by this ADR.

OpenRouter/workspace/key-level guardrails, budgets, provider restrictions, or similar gateway controls may be used when available as defense-in-depth. They do not replace Penatika authorization, per-teacher quota, the capability guard, canonical validation, curriculum assurance, Mathematics assurance, or teacher approval. Penatika must remain correct even if a gateway administrative control is changed incorrectly.

## Guardrails AI Decision

`guardrails-ai/guardrails` is not adopted as an MVP runtime dependency. It is acknowledged as a credible guard framework providing concepts such as input guards, output guards, validators, and structured generation. It is not selected because Penatika is Java 21/Spring Boot while Guardrails AI is Python-based; adopting it would add a second runtime/service or Python boundary; Penatika's scope/quota policy is domain-specific; OpenRouter already provides useful gateway-level routing/privacy/spend controls; and Penatika already owns canonical schema, curriculum assurance, and deterministic Mathematics validation. Penatika borrows the **input guard** + **output guard** concepts and implements Penatika-owned application policies instead, as described throughout this ADR.

## Consequences

### Positive

- Resolves OAD-006 with a gateway that provides multi-model flexibility, provider allowlisting, ZDR/data-collection filtering, and cost controls without making the application layer provider-specific.
- Keeps Penatika AI a bounded teaching capability rather than a general-purpose chatbot, protecting cost, trust, and product identity.
- Establishes a pre-provider pipeline that rejects unsupported and over-scale requests before expensive generation, protecting unit economics and enabling `PR-053`'s free-tier resource controls.
- Gives every teacher an understandable, product-owned daily allowance independent of provider token accounting.
- Gives authorized internal operations privacy-minimized aggregate and per-teacher usage/cost visibility for abuse detection and unit-economics analysis.
- Keeps model routing, privacy routing, and provider fallback server-controlled and evaluation-gated rather than implicit gateway defaults.
- Preserves ADR-0004/ADR-0006/ADR-0015/ADR-0016's AI-independence and non-overridable rules; OpenRouter cannot become curriculum, Mathematics, authorization, or publication authority.

### Negative / Costs

- Adds an additional network hop, gateway dependency, and data processor beyond calling a model provider directly.
- Requires building and maintaining Penatika-owned scope/resource/allowance/reservation/idempotency logic rather than relying on gateway defaults.
- Requires an explicit provider-route review/approval process before any route is used, adding operational overhead before pilot.
- Requires a versioned AI evaluation corpus and process before activating any model/profile/route change for pilot/production.
- Requires reconciliation logic between Penatika's internal usage ledger and OpenRouter's own usage/activity data.

## Alternatives Considered

1. **OpenRouter** → **Selected.**
2. **OpenAI Direct** — technically simpler and avoids an intermediary; not selected because OpenRouter provides materially useful multi-model, routing, cost, and privacy control while Penatika still retains its own Hexagonal abstraction.
3. **Command Code Provider API** — a credible gateway/evaluation option; not selected as primary architecture because OpenRouter has a broader production-oriented routing/provider/control ecosystem for Penatika's current needs.
4. **Google Gemini Direct** — a credible provider; not selected as the gateway architecture.
5. **Anthropic Claude Direct** — a credible provider; not selected as the gateway architecture.
6. **AWS Bedrock** — a credible managed model gateway; not selected because AWS deployment is not selected and cloud-specific coupling is not yet justified.
7. **Multi-provider direct adapters** — possible, but unnecessarily increases integration/evaluation/credential complexity for the MVP.
8. **OpenRouter Auto Router** — rejected for controlled production behavior because Penatika owns model routing/evaluation.
9. **OpenRouter Free Router** — rejected for controlled pilot/production because model identity may vary.
10. **Generic chatbot endpoint + system prompt restriction** — rejected; a system prompt is not a hard scope/resource/allowance boundary.
11. **Keyword-only scope filter** — rejected as the sole guard; substring matching on "mathematics" is not a capability policy.
12. **Quota-only protection with no scope guard** — rejected because valid quota could still be wasted on unrelated work.
13. **Guardrails AI runtime** — credible but not selected (see "Guardrails AI Decision").
14. **Spring AI** — a credible framework but not required; Penatika already owns the Hexagonal `GenerativeModelPort`.
15. **LangChain4j** — a credible framework but not required, for the same reason.
16. **Automatic provider/model fallback** — rejected for MVP; graceful degradation is the fallback policy instead.

## Deferred Implementation Decisions

This ADR does not select or define: exact package/class names for the port/adapter; the exact OpenRouter provider allowlist entries and their review evidence; exact numeric resource limits, allowance quotas, capability resource weights, concurrency limits, or price ceilings; the exact reservation/reconciliation state machine and idempotency contract; field-level OpenAPI/JSON Schema for AI proposals, generation requests, or usage/allowance projections; physical schema/migrations for `AIAllowanceWindow`, `AIUsageReservation`, or `AIUsageEvent`; the exact secret-management mechanism or environment-variable names (OAD-010); the exact internal operational usage-visibility tooling/UI; actual prompt file contents; and the AI evaluation corpus itself.

## Follow-Up Decisions

1. Resolve OAD-007 (Speech Recognition Strategy), reusing this ADR's scope/resource pipeline for the resulting bounded transcript.
2. Resolve OAD-010 (deployment, secret management) to select the OpenRouter credential storage mechanism and API-key topology.
3. Define field-level OpenAPI/JSON Schema for generation requests, AI proposals, and teacher/internal usage-visibility projections when field-level contract work begins.
4. Define the physical schema for `AIAllowanceWindow`, `AIUsageReservation`, and `AIUsageEvent` during source scaffolding, per ADR-0014's Hexagonal persistence boundary.
5. Perform the explicit provider-route review (privacy, data-retention, structured-output compatibility, latency, cost) before activating any concrete OpenRouter provider endpoint.
6. Build the versioned AI evaluation corpus and process before activating any model/profile/route for pilot/production.
7. Select exact numeric resource limits, allowance quotas, and capability resource weights from pilot/cost evidence.

## Related Requirements / ADRs

- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./ADR-0004-ai-assurance-boundary.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](./ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture](./ADR-0013-java21-module-first-hexagonal-backend.md)
- [ADR-0014 — Use PostgreSQL with Flyway and SQL-First Hexagonal Persistence](./ADR-0014-postgresql-flyway-sql-first-persistence.md)
- [ADR-0015 — Use a Versioned Controlled Curriculum Corpus with Deterministic Retrieval](./ADR-0015-versioned-curriculum-corpus-and-deterministic-retrieval.md)
- [ADR-0016 — Use Scoped Deterministic Mathematics Validators with Exact Arithmetic](./ADR-0016-scoped-deterministic-mathematics-validation.md)
- [Business Model and Commercial Path](../../00_product/BUSINESS_MODEL.md)
- [Data Retention, History, Export, and Deletion Policy](../../06_delivery/DATA_RETENTION_POLICY.md)
- [Threat Model](../../04_engineering/THREAT_MODEL.md)
- [Test Strategy](../../04_engineering/TEST_STRATEGY.md)
- [Lesson Preparation](../../01_features/lesson-preparation.md)
- [Live AI Adaptation](../../01_features/live-ai-adaptation.md)
- [Mathematics Assurance and Curriculum Grounding](../../01_features/mathematics-assurance.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-07` | Accepted | Resolve OAD-006 with OpenRouter, bounded model profiles, pre-provider scope/resource controls, per-teacher AI allowances, privacy-constrained provider routing, and graceful degradation |
