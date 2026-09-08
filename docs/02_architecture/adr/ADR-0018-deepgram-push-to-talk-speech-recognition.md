# ADR-0018 — Use Deepgram Nova-3 for Backend-Mediated Push-to-Talk Speech Recognition

| Field | Value |
|---|---|
| ADR | `ADR-0018` |
| Status | Accepted |
| Date | `2026-09-08` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-ADAPT-001`, `PR-020`, `PR-025`, `PR-028`, `PR-036`, `PR-059`, `PR-060`, `PR-063`, `PR-064`, `PR-065` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

OAD-007 selects the speech recognition strategy for push-to-talk (PTT) teacher commands. [ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md) already establishes execution classes (`PRIVATE_ONLY`, `DIRECT_ACTION`, `APPROVAL_REQUIRED`, `APPROVAL_WITH_WARNING`, `BLOCKED`); [ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md) already requires speech failure to disable only push-to-talk transcription while non-voice interaction remains available (`PR-020`); [ADR-0011](./ADR-0011-oidc-backend-managed-browser-sessions.md) already defines teacher identity, browser authentication, and participant authorization; [ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) establishes module-first Hexagonal Architecture; [ADR-0016](./ADR-0016-scoped-deterministic-mathematics-validation.md) establishes deterministic Mathematics validation independent of any provider; and [ADR-0017](./ADR-0017-openrouter-bounded-generation-and-usage-controls.md) establishes OpenRouter as the generative-AI gateway with a pre-provider scope/resource/allowance pipeline. None of these decisions selected a speech recognition provider, browser capture strategy, audio transport boundary, or the relationship between a speech transcript and Penatika's existing intent-routing/AI-gateway boundaries — that is this decision.

[live-ai-adaptation.md](../../01_features/live-ai-adaptation.md) already requires ephemeral audio processing (`FR-ADAPT-002`), bound request context (`FR-ADAPT-003`), non-voice fallback (`FR-ADAPT-018`), and exclusion of raw audio from generative provider payloads (`FR-ADAPT-032`, added by ADR-0017). [DATA_RETENTION_POLICY.md](../../06_delivery/DATA_RETENTION_POLICY.md) already requires that raw push-to-talk audio have zero default persistence (`PR-036`).

This ADR does not scaffold source, install a Deepgram SDK, create API keys, create OpenAPI/JSON Schema field contracts, create database schema/migrations, resolve deployment (OAD-010), activate a background queue (OAD-011), or create audio fixtures.

## Decision

**Voice is an input modality.** It is not application authority, authorization authority, AI-generation authority, curriculum authority, or Mathematics authority. Speech recognition produces an untrusted transcription result that must still pass Penatika-owned intent/capability/application boundaries.

```text
Teacher explicitly holds Push-To-Talk
        ↓
browser records bounded utterance
        ↓
teacher releases Push-To-Talk
        ↓
browser uploads bounded audio to Penatika Backend
        ↓
backend validates request/audio/resource policy
        ↓
Deepgram Speech Recognition
        ↓
SpeechRecognitionResult
        ↓
Penatika transcript normalization
        ↓
intent routing
        ├── deterministic DIRECT_ACTION
        │       ↓
        │   existing command validation
        │       ↓
        │   authoritative HTTP command
        │
        ├── supported semantic request
        │       ↓
        │   ADR-0017 scope/resource/allowance pipeline
        │       ↓
        │   OpenRouter FAST / QUALITY when permitted
        │
        └── unclear / low-confidence / unsupported
                ↓
            ask teacher to repeat / clarify
            NO authoritative action
```

Penatika selects **Deepgram Nova-3** (monolingual, `language=id`) as the initial Speech-to-Text provider, using Deepgram Speech-to-Text only.

## Speech Trust Boundary

A speech transcript is untrusted input, exactly like AI provider output (ADR-0004) and browser input generally. Recognition success does not imply: authorization to act, correctness of Mathematics/curriculum content, or permission to invoke generative AI. Every transcript must still pass Penatika's existing authorization, deterministic direct-action validation, or the ADR-0017 scope/resource/allowance pipeline before it can affect classroom state or consume generative capacity. The speech provider's responsibility ends at transcription; Deepgram does not decide Penatika product behavior (see "No Speech-Provider Intent API").

## Deepgram Provider Decision

Deepgram Speech-to-Text is selected for Nova-3's explicit Indonesian support, pre-recorded and streaming STT support, keyterm prompting, confidence metadata, common-browser-audio-format handling, regional endpoint support, and usage-based pricing suitable for bounded PTT usage. Penatika uses **Deepgram Speech-to-Text only**; Deepgram Voice Agent, Text-to-Speech, Intent Recognition, Topic Detection, Summarization, and Sentiment Analysis are not selected — Penatika owns downstream intent routing (see "No Speech-Provider Intent API").

Current public pay-as-you-go reference at decision time is approximately **USD 0.0043/minute** for Nova-3 monolingual pre-recorded audio. This is operational evidence, not an architecture invariant; today's price is not frozen into architecture.

## Regional / Privacy Boundary

The initial provider endpoint baseline is the **Deepgram Australia regional endpoint** (conceptually `api.au.deepgram.com`), because the Australia endpoint is generally available, Speech-to-Text is supported there, it is geographically appropriate for the Indonesia pilot, customer-content inference/storage can remain in Australia, the same Deepgram API surface is available, and it avoids defaulting to US-global processing when a closer approved regional path exists. Australia processing alone does not solve all privacy/legal requirements; Deepgram operational/billing metadata may still involve other regions. Provider privacy/data-processing review remains mandatory before pilot activation. If the selected model/language is not available or compliant on the approved regional endpoint at implementation time, Penatika does not silently fall back to the global endpoint — it requires explicit provider/architecture/privacy review.

Penatika requests `mip_opt_out=true` (or the current equivalent provider control) for all Penatika requests, so raw teacher audio does not enter an optional provider model-improvement/training program. Current Deepgram documentation states opted-out content is retained only for the duration necessary to process the request; this is recorded as current provider documentation, not treated as a permanent contractual guarantee. Current provider terms must be verified before pilot activation.

## Push-To-Talk Capture Strategy

Penatika selects **pre-recorded / completed-utterance STT** for MVP push-to-talk, not streaming WebSocket STT:

```text
press PTT
→ record locally
→ release PTT
→ finalize recording
→ upload utterance
→ transcribe
→ route result
```

Penatika uses explicit push-to-talk, not an always-listening voice agent; for short teacher commands, completed-utterance transcription avoids an unnecessary long-lived speech-provider WebSocket, browser-provider credentials, stream lifecycle complexity, partial-transcript races, endpointing complexity, and continuous audio exposure. Streaming may be reconsidered only if pilot latency evidence demonstrates that completed-utterance transcription disrupts classroom flow; a future switch requires explicit architecture review of latency evidence, privacy, reconnect behavior, duplicate/partial intent, cost, and transcript finality.

**No always-listening audio.** Penatika does not implement a continuously open transcription session, background microphone transcription, wake word, ambient classroom listening, or automatic student speech capture. The teacher must explicitly initiate each voice utterance. No student speech profile/identity is introduced.

## Browser Audio Format Strategy

Browser capture uses standard `MediaDevices.getUserMedia()` + `MediaRecorder`, negotiating an actually supported recording format via `MediaRecorder.isTypeSupported(...)` rather than assuming a fixed codec/container across browsers.

Preferred format is `audio/webm;codecs=opus` when supported, for efficient speech compression, common browser support, direct provider acceptance, and avoiding large uncompressed WAV uploads. Fallback is a browser-supported MP4/AAC audio format where WebM/Opus is unavailable (particularly for applicable Safari/Apple environments); an additional provider-tested Ogg/Opus format may be supported where useful. Penatika does not require one universal codec if the browser does not support it, and does not fake a `Content-Type` — the actual `MediaRecorder` output type is retained.

**No client-side transcoding baseline.** `ffmpeg.wasm`, client-side transcoding, and AudioWorklet-based custom encoders are not selected for MVP; the provider supports WebM, Opus, MP4, AAC, and other common audio formats, so accepted browser-native containerized audio is forwarded where possible. If later evidence shows a target browser/provider combination requires normalization/transcoding, that need is reviewed separately; FFmpeg is not added merely because transcoding is common.

Browser capture may request best-effort speech-oriented microphone constraints (echo cancellation, noise suppression, automatic gain control) where supported, but browser acceptance of these constraints is not an assurance guarantee, and the application remains usable when individual constraints are ignored by the browser/device. Proprietary client noise-reduction SDKs are not selected.

Microphone use remains explicit: no audio is transmitted before the teacher initiates PTT, and recording stops promptly after teacher release/cancel, preferring to release capture resources when no longer needed. Whether the `MediaStream` track is recreated per utterance or safely reused for a short active-controller period is an implementation/UX choice only if no recording or transmission occurs outside explicit PTT, the teacher sees truthful microphone state, and privacy behavior remains equivalent.

## Backend-Mediated Audio Transport

Penatika uses **Browser → Penatika Backend → Deepgram**, not Browser → Deepgram direct, for MVP. The backend must enforce teacher authentication, session authorization, utterance limits, request rate, provider credentials, privacy options, provider region, usage/cost accounting, correlation, and timeout/failure policy — none of which a direct browser-to-provider connection could enforce. Deepgram credentials remain server-side; Penatika does not create temporary provider credentials for browsers unless a future architecture decision proves direct streaming is required.

Audio upload conceptually uses authenticated HTTPS binary/multipart upload, not base64-encoded audio inside ordinary JSON (which adds unnecessary payload expansion and memory/copy overhead with no product value). The exact endpoint path and field contract remain field-level contract work.

## Audio Input / Resource Validation

Uploaded browser audio is untrusted binary input. Before provider invocation, the backend validates applicable authorization, supported content/container type, actual file/container signature where practical, byte-size bound, utterance-duration bound, the empty/no-audio condition, and per-teacher/session speech concurrency/rate policy. Filename or browser-provided MIME header alone is never trusted; malformed/unsupported media fails before provider invocation when detected.

Push-to-talk utterances must be bounded: architecture requires configurable limits for maximum recording duration, maximum upload bytes, request rate, and concurrent transcription requests. Exact numeric limits are not selected in this ADR — they are established through pilot/device/network evidence — but a teacher cannot hold PTT indefinitely and create unbounded provider cost.

## Raw Audio Retention

`PR-036` and [DATA_RETENTION_POLICY.md](../../06_delivery/DATA_RETENTION_POLICY.md) remain binding. Raw PTT audio must not become durable Penatika product data by default: it is not stored in PostgreSQL, object storage, lesson history, classroom session history, logs, analytics, or debugging traces. Provider-bound audio exists only as transient processing data.

Backend audio processing prefers bounded transient processing without writing raw audio to durable filesystem/object storage; Penatika does not architect an "upload → save permanent audio file → transcribe later" flow, because for MVP the request is synchronous/bounded enough to process immediately. If implementation requires a temporary file for technical reasons, it must be ephemeral, strictly bounded, not product history, and deleted immediately after processing/failure; this ADR does not select a temporary-file implementation.

Full transcription remains transient by default, consistent with existing policy: full voice transcripts are not persisted merely because they are convenient for debugging. Accepted downstream artifacts/commands follow their own canonical lifecycle — for example, the voice command "buat contoh pecahan lain" produces a transient full transcript, while any accepted generated classroom content follows the lesson/session lifecycle. Speech lifecycle metadata follows the privacy-minimized policy described in "Speech Cost / Usage Accounting."

## Indonesian Language Strategy

The initial recognition language is explicitly **Indonesian (`id`)**. Generic automatic-language detection is not enabled by default, because the target teachers are Indonesian, an explicit language reduces unnecessary ambiguity, and predictable language configuration improves evaluation. English Mathematics vocabulary/code-switching must be tested separately (see "Provider / Model Evaluation") rather than making full multilingual auto-detection the default.

## Mathematics Keyterm / Lexicon Strategy

Penatika uses Deepgram Nova-3 **keyterm prompting** for a bounded, controlled Mathematics lexicon. Example context-relevant terms conceptually include *pecahan*, *pembilang*, *penyebut*, *pecahan senilai*, *aljabar*, *variabel*, *koefisien*, *persamaan linear*, and supported English/code-switched Mathematics terminology where evaluation shows it helps; these examples are not the final dictionary, and no corpus/list file is created by this ADR.

Penatika does not send hundreds of unrelated terms on every transcription. The provider keyterm set is constructed from approved bounded sources: the stable Penatika Mathematics speech vocabulary, the current grade/topic, the current lesson, the current `CurriculumContextBundle`, and the supported direct-action vocabulary. Deepgram's current keyterm-prompting limits must be respected. Provider keyterms are recognition hints only — they never become curriculum authority.

A conceptual `SpeechLexiconVersion` is defined; the speech request should be traceable where useful to language, provider/model, lexicon version, and relevant topic/context source, to help evaluate recognition regressions when terminology configuration changes. Every teacher utterance is not made durable simply for lexicon traceability.

**Teacher free text must not become unrestricted provider keyterm configuration.** The application derives keyterms only from approved context; unrestricted keyterm injection could distort transcription, bypass scope behavior, or create unexpected provider behavior.

## Transcript and Confidence Semantics

A speech transcript is not guaranteed to represent what the teacher said. Provider confidence is advisory, not absolute truth; Penatika does not treat `confidence > X` as guaranteed correctness. Confidence may help choose whether to proceed, ask for confirmation, or ask the teacher to repeat — exact thresholds remain evaluation/configuration decisions.

When transcript quality is insufficient for safe routing, Penatika does not execute a `DIRECT_ACTION`, silently guess teacher intent, or send uncertain content to expensive generation as if certain. It returns a teacher-private `NEEDS_CLARIFICATION`/`RETRY_SPEECH`-equivalent state; the teacher can speak again, use text, or use deterministic UI controls.

## Spoken Mathematics Normalization Boundary

Speech provider output remains text transcription; the provider is not required to decide Penatika mathematical semantics. For example, spoken "dua per tiga" may transcribe as textual Indonesian. A Penatika-owned controlled normalization layer may later map supported spoken Mathematics expressions into structured Mathematics claims only where the grammar is unambiguous; that normalizer remains subordinate to [ADR-0016](./ADR-0016-scoped-deterministic-mathematics-validation.md). Penatika does not use an LLM to silently guess an exact mathematical expression when speech is ambiguous. Examples requiring care include "dua per tiga," "x pangkat dua," "dua x tambah tiga," "minus tiga per empat," and "x sama dengan dua"; the exact speech-math grammar belongs to field-level/implementation work.

Speech normalization may safely perform bounded operations such as Unicode/whitespace normalization, provider punctuation normalization where needed, recognized fixed `DIRECT_ACTION` phrase mapping, and controlled Mathematics spoken-form normalization where explicitly supported. This layer is not a generic semantic-rewriting LLM. The distinction between what the provider heard and what Penatika normalized/interpreted is preserved.

## Direct Action Routing

After successful transcription, Penatika first attempts a deterministic supported `DIRECT_ACTION` classification where the current product defines one:

```text
SpeechRecognitionResult
        ↓
controlled normalization
        ↓
DirectActionMatcher
        │
        ├── confident exact supported command
        │       ↓
        │   authorization
        │   revision validation
        │   command validation
        │       ↓
        │   synchronous HTTP application command
        │
        └── not a direct action
                ↓
            semantic pipeline
```

OpenRouter is not invoked for a recognized deterministic direct action. Voice alone does not bypass existing command security: a matched voice direct action still requires an authenticated teacher, valid controller/session authority, a supported `DIRECT_ACTION` class, current revision/context, and command validation, exactly as [ADR-0006](./ADR-0006-teacher-approval-ai-publication-policy.md) already requires. A transcript never creates a new direct-action class. Exact voice-command phrases remain implementation/UX contracts.

## Semantic AI Routing

If the transcript represents a semantic teaching request rather than a supported `DIRECT_ACTION`, Penatika passes the bounded transcript/normalized request into the [ADR-0017](./ADR-0017-openrouter-bounded-generation-and-usage-controls.md) pipeline, which still performs the hard resource guard, deterministic capability/scope guard, optional bounded `ROUTER`, AI allowance check, usage reservation, and model resolution. **Speech success does not imply AI generation permission.**

For example, a teacher saying "buatkan website React untuk belajar pecahan" may be correctly transcribed — correct transcription does not make it supported: STT → transcript → ADR-0017 capability guard → `OUT_OF_SCOPE` → no `FAST`/`QUALITY` generation. A speech-provider cost may exist even though no generative allowance is consumed.

## Speech Cost / Usage Accounting

Speech recognition usage is **not the same resource as generative AI usage**. Penatika does not decrement `AIAllowanceWindow` merely because an utterance was transcribed. For example: teacher says "halaman berikutnya" → speech provider used → deterministic `DIRECT_ACTION` → zero OpenRouter generation → zero generative AI allowance consumed, while a small speech-provider cost still exists and is tracked separately.

Penatika conceptually introduces `SpeechRecognitionRequest` and `SpeechUsageEvent`. A `SpeechUsageEvent` may retain privacy-minimized metadata such as: internal `TeacherAccount` reference; session reference where applicable; speech request identity; provider; speech model; provider region; audio duration; audio container/codec category; transcription outcome; latency; provider-request/correlation identifier where safe; billed/estimated speech cost; routing outcome (`DIRECT_ACTION`, `SEMANTIC_AI`, `CLARIFICATION`, `REJECTED`); and timestamp. Raw audio is never retained in the usage event, and a full transcript is not required to be retained.

Backend resource controls may enforce speech-specific duration, rate, concurrency, and daily/resource allowance if operational evidence requires them; no numeric daily speech quota is locked by this ADR, and speech usage is not invisibly merged into the generative AI allowance. If a teacher-facing speech allowance is introduced later, it must be clearly represented as a distinct speech/input resource or through an explicitly approved combined product-unit policy; core non-voice controls must not depend on available speech quota.

Authorized Penatika internal operations should be able to inspect privacy-minimized speech usage: aggregate teachers using PTT, transcription count, total audio duration, success/failure, provider latency, cost, low-confidence/clarification frequency, and `DIRECT_ACTION` vs. semantic routing ratio; per-teacher transcription count, duration, provider cost, and failures/resource rejections where operationally justified, with no raw audio/full transcript by default. This does not create a school-admin role.

## Timeout / Retry / Cancellation

Every transcription request has a bounded backend/provider timeout; exact duration remains configuration based on classroom evaluation. On timeout, Penatika returns a private speech-unavailable/retry state; it does not freeze the classroom session, discard reviewed content, disable text/direct UI controls, or automatically invoke generative AI. `PR-020` and [ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md) remain binding.

Penatika does not use uncontrolled automatic STT retries, because audio requests have cost and ambiguous network acknowledgement; for MVP, teacher-visible retry after failure is preferred. A narrowly bounded technical retry may occur only when implementation can establish that duplicate provider processing/cost is not likely. Penatika does not retry an unclear transcript in the hope that another response becomes the desired command.

The teacher must be able to cancel PTT before submission where technically possible. Cancelled audio is not sent to the provider, creates no semantic generation, and creates no authoritative command. Once audio has already been transmitted, cancellation must not falsely claim that upstream processing definitely did not occur.

## Degraded Behavior

`PR-020` remains binding: speech failure must not disable text input, deterministic UI controls, the current reviewed lesson, digital ink, or non-voice supported AI requests. Speech degradation is capability-specific; this ADR does not create a second conflicting degraded-mode architecture — [ADR-0007](./ADR-0007-graceful-degradation-without-offline-authority.md)'s existing degradation policy governs.

## Provider / Model Evaluation

Before pilot activation, Penatika evaluates the selected speech provider/model using a versioned speech evaluation corpus covering: Bahasa Indonesia teacher speech; different Indonesian accents where available; quiet-room and realistic classroom background-noise conditions; near-field smartphone microphone; Grade 5 fraction terminology; Grade 7 algebra terminology; numbers and fractions spoken naturally in Indonesian; Mathematics English loanwords/code-switching; supported `DIRECT_ACTION` phrases; ambiguous command phrases; silence/no speech; clipped PTT start/end; long utterance/resource-bound input; unsupported/general-purpose semantic requests; and low-confidence/error behavior. Conceptual measurements include word error rate where useful, Mathematics keyterm recall, direct-action routing accuracy, dangerous false-direct-action rate, latency, request failure rate, audio duration, and provider cost. This ADR does not invent pass thresholds; pilot activation requires evidence-driven approval.

## Model Drift / Policy Versioning

Provider speech models may change even when an alias stays stable. Penatika records provider, model, configured language, provider model/version metadata where available, `SpeechLexiconVersion`, and a conceptual `SpeechPolicyVersion`. If the provider offers stable immutable model versions, Penatika prefers an evaluated stable version for pilot; if only a rolling production alias is practical, provider model updates are treated as external behavior drift and relevant evaluation is rerun when changes are announced/detected. `nova-3` is not assumed to mean forever-identical behavior.

A conceptual `SpeechPolicyVersion` may bind provider, model, region, language, audio-format policy, lexicon version, resource bounds, confidence/clarification policy, and timeout/retry behavior. This remains application configuration/policy; no config file is created by this ADR.

## Security and Privacy

Deepgram is not used for Intent Recognition, Topic Detection, Summarization, or LLM/agent features to decide Penatika product behavior — Penatika already owns intent and scope policy, and speech provider responsibility ends at transcription-related functionality. Speaker diarization is not enabled for MVP PTT, because the teacher explicitly operates their own private controller microphone and Penatika does not need to identify classroom speakers from a shared recording. Provider profanity filtering or arbitrary text replacement is not enabled as the default transcription policy, so the teacher's utterance is preserved as faithfully as possible for downstream product/policy checks; provider-side rewriting could also accidentally distort domain terminology. Deepgram smart formatting is not relied upon as mathematical correctness authority; if enabled later for readability, the original provider transcript/controlled normalized representation must still preserve enough evidence to avoid silent semantic changes, and the initial baseline keeps mathematical normalization Penatika-owned.

Deepgram credentials are server-side secrets, never exposed to Teacher Web, Classroom Display, browser JavaScript, application responses, or logs. `guardrails-ai/guardrails`-style speech-provider guardrails, budgets, and workspace controls, where offered, may be used as defense-in-depth but do not replace Penatika's own authorization, resource, and routing boundaries.

## Consequences

### Positive

- Resolves OAD-007 with a speech-specialized provider that has explicit Indonesian support, keyterm prompting, confidence metadata, and an appropriate regional endpoint, without requiring a separate always-listening or streaming architecture the MVP does not need.
- Keeps raw audio out of durable storage and out of the generative-AI gateway, satisfying `PR-036` and ADR-0017's audio-exclusion requirement in one consistent boundary.
- Keeps speech cost/usage accounting cleanly separate from generative AI allowance, so a `DIRECT_ACTION` voice command never wastes generative capacity.
- Reuses existing execution-class (ADR-0006), degradation (ADR-0007), and identity/authorization (ADR-0011) boundaries rather than inventing a parallel voice-authority model.
- Keeps Deepgram a replaceable backend-mediated adapter; the browser never depends on provider-specific streaming/credential mechanics.

### Negative / Costs

- Requires building and maintaining a bounded browser capture → backend upload → Deepgram → normalization → routing pipeline rather than delegating directly to a browser API.
- Requires an explicit provider privacy/data-processing review (region, `mip_opt_out`, retention terms) before pilot activation.
- Requires a versioned speech evaluation corpus (accents, classroom noise, Mathematics vocabulary, direct-action phrases) before pilot activation, similar in scope to the ADR-0017 AI evaluation gate.
- Pre-recorded completed-utterance transcription adds a small latency cost relative to streaming, which must be validated against classroom teaching-flow evidence.
- Requires Penatika-owned direct-action matching and controlled Mathematics normalization rather than delegating semantics to the provider.

## Alternatives Considered

1. **Deepgram Nova-3 Indonesian** → **Selected**, for explicit Indonesian support, keyterm prompting, common browser-format support, confidence metadata, low usage-based cost, the AU regional endpoint, model-improvement opt-out, and the availability of both pre-recorded and streaming paths if future evidence requires it.
2. **OpenAI GPT-Transcribe / GPT-4o Transcribe** — a credible, strong general-transcription and multilingual/language-hint provider; not selected initially because Penatika already uses OpenRouter for generative AI without needing OpenRouter to become the raw-audio gateway, Deepgram provides a speech-specialized API with Indonesian Nova-3/keyterm prompting/AU endpoint/attractive short-utterance pricing, and direct OpenAI speech would require a separate provider privacy/retention path anyway. Kept as an evaluation/fallback candidate, not an automatic runtime fallback.
3. **Google Cloud Speech-to-Text** — a credible provider with strong Indonesian support, model adaptation, regional options, and production maturity; not selected because it introduces Google Cloud-specific speech integration before OAD-010, while Deepgram provides the required capability with less cloud-platform coupling.
4. **Browser Web Speech API** — rejected as the authoritative MVP baseline because cross-browser/provider behavior varies, the recognition backend may be browser/vendor-controlled, privacy/data processing is less explicit, and server-side provider/evaluation consistency is harder to enforce.
5. **Local/browser Whisper/WebAssembly model** — rejected for initial MVP due to model download/CPU/memory/battery impact, mobile-device variability, slower startup, and added frontend complexity. Revisit if offline/privacy requirements eventually justify on-device STT.
6. **Self-hosted Whisper/Python speech service** — rejected for initial MVP due to additional runtime/model/GPU/operations burden, with no current evidence that provider speech cost or privacy requires it.
7. **Streaming WebSocket STT** — a credible future option; not selected initially because explicit short push-to-talk completed utterances do not yet justify continuous streaming complexity.
8. **Browser → Deepgram direct** — rejected for MVP because it bypasses backend authorization, credential, privacy, usage, resource, and region control.
9. **OpenRouter as speech gateway** — not selected; ADR-0017 defines OpenRouter as Penatika's generative-model gateway, and raw speech recognition remains a separate bounded adapter so raw audio does not unnecessarily enter the generative AI gateway.

## Deferred Implementation Decisions

This ADR does not select or define: exact package/class names for the speech port/adapter; exact numeric duration/size/rate/concurrency limits; the exact upload endpoint path and field-level contract; the exact temporary-file (if any) implementation; exact confidence thresholds for `NEEDS_CLARIFICATION`/`RETRY_SPEECH`; the exact `DirectActionMatcher` phrase set and matching algorithm; the exact spoken-Mathematics normalization grammar; the exact Mathematics keyterm dictionary/corpus; physical schema/migrations for `SpeechRecognitionRequest` or `SpeechUsageEvent`; the exact secret-management mechanism or environment-variable names (OAD-010); and the speech evaluation corpus itself.

## Follow-Up Decisions

1. Perform the explicit Deepgram privacy/data-processing review (region, `mip_opt_out`, retention terms, subprocessors) before pilot activation.
2. Build the versioned speech evaluation corpus and process before activating pilot/production speech recognition.
3. Define field-level OpenAPI/JSON Schema for the audio-upload endpoint and speech-result/usage-visibility projections when field-level contract work begins.
4. Define the physical schema for `SpeechRecognitionRequest` and `SpeechUsageEvent` during source scaffolding, per ADR-0014's Hexagonal persistence boundary.
5. Select exact numeric duration/size/rate/concurrency limits and confidence thresholds from pilot/device/network evidence.
6. Resolve OAD-010 (deployment, secret management) to select the Deepgram credential storage mechanism.
7. Revisit streaming STT only if pilot latency evidence demonstrates completed-utterance transcription disrupts classroom flow.

## Related Requirements / ADRs

- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./ADR-0004-ai-assurance-boundary.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing](./ADR-0011-oidc-backend-managed-browser-sessions.md)
- [ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture](./ADR-0013-java21-module-first-hexagonal-backend.md)
- [ADR-0016 — Use Scoped Deterministic Mathematics Validators with Exact Arithmetic](./ADR-0016-scoped-deterministic-mathematics-validation.md)
- [ADR-0017 — Use OpenRouter for Bounded Generative AI with Scope, Quota, and Privacy Routing Controls](./ADR-0017-openrouter-bounded-generation-and-usage-controls.md)
- [Data Retention, History, Export, and Deletion Policy](../../06_delivery/DATA_RETENTION_POLICY.md)
- [Live AI Adaptation](../../01_features/live-ai-adaptation.md)
- [Threat Model](../../04_engineering/THREAT_MODEL.md)
- [Test Strategy](../../04_engineering/TEST_STRATEGY.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-08` | Accepted | Resolve OAD-007 with backend-mediated Deepgram Nova-3 Indonesian push-to-talk transcription, bounded browser audio capture, controlled Mathematics vocabulary, privacy-minimized speech usage accounting, and deterministic direct-action-first transcript routing |
