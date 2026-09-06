# Product Requirements Document (PRD) — Penatika

> **Peran dokumen:** Source of truth untuk capability, cross-feature behavior, product rules, dan release scope Penatika.
>
> Product purpose dan product-level assumptions berada di [Product Brief](./PRODUCT_BRIEF.md). Detailed behavior berada di feature specifications. Technical design berada di architecture, ADR, dan contracts bila sudah dipilih.

---

## Metadata Dokumen

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.9` |
| Owner | `sipratama — Product Owner / Requirement Approver` |
| Last Updated | `2026-09-06` |
| Target Phase | MVP baseline dan classroom pilot preparation |

---

## 1. Product Summary

Penatika menyediakan teacher-controlled workflow untuk menyiapkan, menyajikan, dan mengadaptasi pelajaran Matematika pada classroom display. Guru menggunakan preparation workspace dan private smartphone controller, sementara backend menjaga authoritative classroom session state dan AI menghasilkan structured proposals yang tunduk pada approval, mathematics validation, dan curriculum grounding.

### Product Brief Reference

- [Product Brief — Penatika](./PRODUCT_BRIEF.md)

---

## 2. Actors

| Actor | Goal | MVP Scope |
|---|---|---|
| Teacher | Prepare, control, adapt, annotate, and save a lesson | Primary authenticated or authorized actor; identity mechanism belum dipilih |
| Classroom Viewer | View appropriate teaching content on the classroom display | Display participant; tidak mendapat private controls atau AI working state |
| Student | Receive the classroom experience | Beneficiary; tidak menggunakan student device pada MVP |
| External AI Capability | Produce lesson or adaptation proposals | Untrusted integration, bukan decision-maker |
| Curriculum Source | Supply controlled curriculum reference data | Normative Mathematics authority selected: BSKAP 046/H/KR/2025; guidance/local overlays retain distinct authority levels |

---

## 3. Product Scope

### In Scope

- AI-assisted lesson preparation dan teacher review.
- Curriculum-aware content context.
- Cloud-mediated classroom session dan device pairing.
- Separate teacher-private dan student-facing surfaces.
- Structured classroom scene dan content elements.
- Live adaptation melalui voice atau direct controls.
- Mouse, touch, stylus, dan digital ink interaction.
- Deterministic mathematics validation bila memungkinkan.
- Session saving dan recovery baseline.
- Graceful degradation untuk dependency failure yang dapat ditoleransi.

### Out of Scope

- Autonomous teaching.
- Student-device participation.
- PDF atau PowerPoint import.
- Attendance dan school administration.
- Advanced graphing atau 3D visualization.
- Proprietary-hardware dependency.
- Non-Mathematics subject support.

---

## 4. Product Capabilities

### CAP-LESSON-001 — Prepare an AI-Assisted Lesson

**Description:** Guru membuat initial lesson draft menggunakan grade, topic, learning intent, dan controlled curriculum context, kemudian meninjau serta mengubahnya sebelum digunakan.

**User Outcome:** Guru memulai kelas dengan material terstruktur yang sudah ditinjau, bukan raw AI response.

**Actor:** Teacher

**Feature Spec:** [lesson-preparation.md](../01_features/lesson-preparation.md)

### CAP-SESSION-001 — Start and Pair a Classroom Session

**Description:** Guru memulai cloud teaching session, membuka classroom display, dan memasangkan smartphone controller tanpa screen mirroring atau shared Wi-Fi dependency.

**User Outcome:** Guru mempunyai private controller dan student-facing display yang terhubung ke state sesi yang sama.

**Actor:** Teacher, Classroom Viewer

**Feature Spec:** [classroom-session.md](../01_features/classroom-session.md)

### CAP-CANVAS-001 — Present and Manipulate Structured Classroom Content

**Description:** Penatika merender structured lesson content dan memungkinkan navigation serta direct manipulation yang konsisten pada classroom surface.

**User Outcome:** Guru dapat mengajar dari classroom canvas yang dapat diubah tanpa memperlihatkan internal AI workflow.

**Actor:** Teacher, Classroom Viewer

**Feature Spec:** [classroom-canvas.md](../01_features/classroom-canvas.md)

### CAP-INK-001 — Annotate with Digital Ink

**Description:** Guru dapat menulis, highlight, erase, undo, redo, dan clear menggunakan mouse, touch, atau stylus sesuai kemampuan perangkat.

**User Outcome:** Guru dapat menjelaskan dan menekankan materi dengan interaction yang natural.

**Actor:** Teacher

**Feature Spec:** [classroom-canvas.md](../01_features/classroom-canvas.md)

### CAP-ADAPT-001 — Request Live AI Adaptation

**Description:** Guru dapat meminta perubahan terhadap contoh, pertanyaan, penjelasan, atau visual melalui push-to-talk atau direct command. Teacher request authorizes generation, lalu Penatika menjalankan flow teacher request → structured proposal → policy/assurance → private preview → approval / warned override / block → accepted revision-aware classroom command. AI tidak menjadi authoritative state owner.

**User Outcome:** Guru dapat berimprovisasi tanpa berpindah ke generic chatbot.

**Actor:** Teacher, External AI Capability

**Feature Spec:** [live-ai-adaptation.md](../01_features/live-ai-adaptation.md)

### CAP-MATH-001 — Validate Mathematics and Ground Curriculum Claims

**Description:** Penatika memvalidasi hasil matematika secara deterministik bila rule/engine tersedia dan menautkan curriculum claims ke layered, controlled, versioned curriculum provenance. National normative claims, official guidance, dan local school/teacher context tetap dibedakan.

**User Outcome:** Guru menerima confidence dan warning yang lebih tepat sebelum content digunakan di kelas.

**Actor:** Teacher, Curriculum Source

**Feature Spec:** [mathematics-assurance.md](../01_features/mathematics-assurance.md)

### CAP-SESSION-002 — Save the Teaching Session

**Description:** Guru dapat mengakhiri dan menyimpan authoritative session state beserta lesson version dan classroom changes yang termasuk dalam retention policy.

**User Outcome:** Hasil mengajar dapat dipertahankan untuk continuity atau review yang diizinkan.

**Actor:** Teacher

**Feature Spec:** [classroom-session.md](../01_features/classroom-session.md)

---

## 5. Primary User Journeys

### J-01 — Prepare a Lesson

1. Guru memilih grade, topic, dan learning intent.
2. Penatika menyelesaikan curriculum context dari normative source yang berlaku serta optional guidance/local context yang provenance-nya eksplisit, lalu meminta AI menghasilkan structured lesson proposal.
3. Penatika menunjukkan generation dan validation state pada teacher surface.
4. Guru meninjau, mengubah, menerima, atau menghasilkan ulang bagian lesson.
5. Guru menyimpan lesson version yang siap digunakan.

**Outcome:** Tersedia teacher-reviewed lesson version untuk classroom session.

### J-02 — Start and Pair the Classroom

1. Guru memilih lesson version dan memulai classroom session.
2. Classroom display bergabung sebagai student-facing participant.
3. Guru memasangkan smartphone controller melalui pairing mechanism yang aman.
4. Backend mengotorisasi setiap participant dan mengirim authoritative session state sesuai surface.

**Outcome:** Teacher controller dan classroom display berada pada sesi yang sama dengan visibility yang berbeda.

### J-03 — Teach and Adapt

1. Guru menavigasi lesson dan memberi annotation.
2. Guru meminta adaptation menggunakan push-to-talk atau direct control.
3. Penatika mengubah request menjadi structured AI proposal.
4. Policy, curriculum grounding, dan mathematics validation diterapkan sesuai content type.
5. Guru menerima private preview berupa clean proposal, proposal with warning, atau blocked result.
6. Guru memberikan approval atau explicit warned override bila diizinkan; blocked result tidak dapat dipublikasikan.
7. Proposal yang memiliki publication authorization valid menjadi accepted revision-aware classroom command.
8. Classroom display menerima role-specific projection yang sudah diperbarui.

**Outcome:** Classroom content berubah tanpa guru keluar dari teaching flow.

### J-04 — Recover and Save

1. Jika AI provider gagal, Penatika menghentikan capability AI generation tetapi mempertahankan reviewed lesson presentation dan supported deterministic teaching controls selama backend authority tetap sehat.
2. Jika speech recognition gagal, Penatika menonaktifkan push-to-talk dan menyediakan supported non-voice teacher interaction.
3. Jika classroom display terputus, student-facing mutation berhenti sampai display tersinkronisasi kembali; teacher-private work boleh berlanjut hanya bila tidak mengubah classroom projection.
4. Jika teacher controller terputus, classroom display mempertahankan current authoritative projection tanpa automatic mutation; teacher surface lain hanya boleh bertindak bila diotorisasi secara terpisah oleh backend.
5. Jika backend-authoritative session state tidak dapat dijangkau, client mempertahankan last-known safe classroom projection, membekukan mutation baru, dan tidak membuat offline command baru untuk automatic replay.
6. Setelah reconnect, client mengambil authoritative revision, merekonsiliasi state, menolak stale/conflicting assumptions, lalu melanjutkan mutation hanya setelah synchronization selesai. Command yang sudah dikirim sebelum disconnect tetapi acknowledgement-nya tidak pasti boleh direkonsiliasi melalui command identity, idempotency, dan revision semantics.
7. Jika durable save dependency gagal tetapi runtime authority tetap sehat, pengajaran boleh berlanjut dan save tetap `PENDING`, `FAILED`, atau `RETRY_REQUIRED` sampai durable authoritative acknowledgement diterima.

**Outcome:** Session mempertahankan satu backend-authoritative state, mengalami degradation yang terbatas pada capability terdampak, dan melaporkan recovery serta save outcome secara benar.

---

## 6. Product-Wide Rules

| ID | Rule |
|---|---|
| PR-001 | Guru tetap menjadi decision-maker untuk classroom content dan pacing. |
| PR-002 | Teacher-private controls, AI working state, dan sensitive warnings tidak boleh tampil otomatis pada classroom display. |
| PR-003 | Backend-managed session state adalah authoritative untuk synchronization lintas perangkat. |
| PR-004 | Classroom content harus menggunakan supported structured content model; arbitrary AI-generated HTML atau executable content dilarang. |
| PR-005 | AI output diperlakukan sebagai untrusted proposal dan harus melewati schema validation serta policy checks sebelum dapat memengaruhi authoritative state. |
| PR-006 | Scoped mathematical claims harus melewati deterministic validation bila validator tersedia; failure atau unsupported validation harus terlihat bagi guru. |
| PR-007 | Setiap grounded curriculum claim harus mereferensikan authority level, controlled source, source version, relevant scope/phase, dan provenance. |
| PR-008 | Raw push-to-talk audio tidak disimpan secara default. |
| PR-009 | Student devices tidak boleh menjadi prerequisite untuk core classroom workflow. |
| PR-010 | Pairing tidak boleh bergantung pada screen mirroring atau keberadaan perangkat pada shared Wi-Fi. |
| PR-011 | Client tidak boleh menjadi authorization authority hanya karena mempunyai pairing code, cached state, atau hidden UI control. |
| PR-012 | Degraded mode tidak boleh mengubah stale atau unvalidated proposal menjadi authoritative classroom content. |
| PR-013 | Untuk Mathematics MVP, national normative curriculum authority adalah Keputusan Kepala BSKAP No. 046/H/KR/2025 sampai Penatika secara eksplisit mengaktifkan official superseding source. |
| PR-014 | Official guidance dan school/teacher context boleh memperkaya interpretasi atau sequencing, tetapi tidak boleh dipresentasikan sebagai national normative requirement. AI tidak pernah menjadi curriculum authority. |
| PR-015 | A teacher request to generate AI content authorizes generation but does not authorize publication of unseen generated semantic content. |
| PR-016 | AI-generated student-facing semantic content requires post-generation teacher approval after private preview and applicable checks. |
| PR-017 | Authorized deterministic non-generative presentation/annotation commands may execute directly only when they belong to an explicitly supported direct-action class and do not introduce new semantic teaching content. |
| PR-018 | Unsupported/inconclusive assurance may use explicit warned override when allowed; known-invalid, stale, unauthorized, policy/security/privacy-violating, or structurally unsafe proposals are blocked and cannot be overridden. |
| PR-019 | AI provider failure must degrade AI-generation capabilities without removing the current reviewed classroom lesson or supported deterministic teaching controls while backend authority remains healthy. |
| PR-020 | Speech failure must not disable non-voice teacher controls or supported non-voice AI requests. |
| PR-021 | When the classroom display is disconnected, student-facing mutations must pause until display synchronization is restored; teacher-private work may continue only when it does not mutate classroom projection. |
| PR-022 | When backend-authoritative session state cannot be reached, clients must preserve the last-known safe projection and must not create new authoritative mutations. |
| PR-023 | MVP must not queue new offline state-changing commands for automatic replay. Recovery must reconcile against backend authority before new mutations resume. |
| PR-024 | A session save may be reported successful only after the authoritative durable save path acknowledges success. |
| PR-025 | Degraded mode must never bypass approval, assurance, authorization, privacy, structured-content validation, curriculum provenance, or stale/revision controls. |
| PR-026 | The first pilot is limited to Grade 5 Fractions and Grade 7 Basic Algebra / Linear Equations; broader Mathematics coverage is not a pilot requirement. |
| PR-027 | Pilot success requires all hard safety/trust gates to pass before broader scope expansion may be considered. |
| PR-028 | Pilot telemetry must be privacy-minimized and must not require student profiling or raw-audio retention. |
| PR-029 | Pilot results are directional product evidence and must not be represented as statistically valid learning-outcome, nationwide teacher-demand, or educational-efficacy evidence. |
| PR-030 | Missing product/usability thresholds with all safety gates passing leads to iteration within the current pilot scope rather than automatic feature/topic expansion. |
| PR-031 | Pendago / AI Teaching Canvas material is legacy input and is not authoritative for Penatika unless explicitly reviewed and mapped into a current canonical Penatika document. |
| PR-032 | When a legacy Pendago decision conflicts with the current Penatika Product Brief, PRD, feature specification, or Accepted ADR, the current Penatika decision takes precedence. |
| PR-033 | Legacy field-level contracts, taxonomies, and implementation mechanics must be re-derived and revalidated against current Penatika requirements before they can become active Penatika contracts. |
| PR-034 | Teacher-owned lessons and versions are retained until teacher deletion, subject to documented deletion processing and backup expiry. |
| PR-035 | Saved classroom session history has a default `90-day` retention period and may be deleted earlier by the teacher. |
| PR-036 | Raw push-to-talk audio is not retained by default; full transcription, prompts, raw provider payloads, and unaccepted proposal bodies are transient and must not become long-term history by default. |
| PR-037 | Teacher-owned retained lesson/session data must support an authorized export process and teacher-requested deletion. |
| PR-038 | Primary deletion must complete within `30 days` of an accepted deletion request; backup remnants expire no later than `30 additional days` unless an explicit narrow preservation requirement applies. |
| PR-039 | Accepted AI content follows the retention lifecycle of the lesson/session it becomes part of; rejected or unaccepted AI content does not become durable classroom history by default. |
| PR-040 | Event-level pilot telemetry and identifiable research evidence must be deleted or appropriately de-identified no later than `90 days` after final pilot-report acceptance unless another explicit approved purpose exists. |
| PR-041 | Retention policy must not introduce persistent student identity, student profiling, or broader collection than the MVP otherwise requires. |
| PR-042 | Controlled curriculum source/version metadata may be preserved for provenance and historical reproducibility independently of teacher-account deletion, while teacher-specific local-context data follows teacher-owned data policy. |
| PR-043 | `sipratama` is the accountable Product Owner and Requirement Approver for the current founder-led Penatika phase until explicit delegation or supersession. |
| PR-044 | Material product requirement, scope, user-behavior, pilot, privacy-policy, or business-policy changes require Product Owner / Requirement Approver approval in the applicable canonical source. |
| PR-045 | Engineering, architecture, AI agents, and implementation choices must not silently redefine or supersede accepted product requirements. |
| PR-046 | A material technical decision that changes product behavior, scope, teacher control, privacy, assurance, pilot commitments, or business assumptions requires Product Owner review in addition to the applicable technical decision process. |
| PR-047 | Hard safety/trust gates, Product Release Blockers, and Accepted invariants cannot be waived through ordinary Product Owner approval; a failed hard gate requires remediation and revalidation before blocked progression resumes. |
| PR-048 | Stage B product go/no-go authority belongs to the Product Owner, but approval is valid only after all required Stage B entry gates and evidence are satisfied. |
| PR-049 | Delegation of Product Owner or Requirement Approver authority must be explicit, scoped, dated, and recorded in [PRODUCT_GOVERNANCE.md](./PRODUCT_GOVERNANCE.md). |
| PR-050 | Penatika uses a teacher-first freemium SaaS model; school procurement is not required for initial teacher adoption. |
| PR-051 | The first pilot is free and does not require billing, subscription, payment, or procurement workflows. |
| PR-052 | The initial paid commercial offer is an optional per-teacher Teacher Pro subscription; exact numeric pricing is evidence-driven and requires Product Owner approval before charging users. |
| PR-053 | Free-tier resource controls may limit AI capacity, but quota exhaustion must not weaken safety policy or corrupt an active classroom session. |
| PR-054 | Mathematics assurance, curriculum provenance, AI publication approval, authorization, privacy, data-lifecycle rights, structured-content safety, and other required safety/trust controls must not be made paid-only features. |
| PR-055 | Penatika does not use advertising, sale of teacher/student personal data, or student profiling as its teacher-first monetization model. |
| PR-056 | Institutional licensing is a later commercial path and does not automatically introduce school-administration capabilities into MVP scope. |
| PR-057 | Commercial launch requires evidence for product value, unit economics, willingness to pay, billing correctness, support readiness, privacy/legal readiness, and Product Owner approval. |
| PR-058 | Commercial metrics remain separate from the Q-04 first-pilot pass/fail thresholds unless the Product Owner explicitly approves a future pilot-policy change. |

---

## 7. Roles and Permissions Overview

| Capability | Teacher | Classroom Viewer | Student Device |
|---|:---:|:---:|:---:|
| Create or edit lesson | Yes | No | Not supported |
| Start or end session | Yes | No | Not supported |
| Pair controller | Yes, subject to authorization | No | Not supported |
| View student-facing content | Yes | Yes | Not supported |
| View private AI suggestions and validation details | Yes | No | Not supported |
| Change authoritative classroom state | Yes, through supported commands | No | Not supported |
| Save session | Yes | No | Not supported |

Exact identity, authentication, and session authorization mechanisms remain an Open Architecture Decision.

---

## 8. UX Requirements

### Cross-Product Experience Expectations

- Classroom display prioritizes readability, focus, and absence of private controls.
- Teacher controller prioritizes one-handed or quick interaction where practical.
- AI generation must expose progress without blocking unrelated safe teaching actions.
- Teacher must be able to distinguish private processing, proposal ready, proposal with warning, blocked, approved/accepted, and displayed states.
- Mouse, touch, stylus, and keyboard behavior must be consistent for equivalent actions.

### Required States

- Empty or no lesson selected.
- Private processing, including generation and validation in progress.
- Proposal ready for teacher action.
- Proposal with warning and explicit warned-override action where allowed.
- Blocked proposal or action with no publication override.
- Approved/accepted proposal.
- Displayed content.
- Session ready, paired, active, reconnecting, ending, saved, and failed.
- Degraded AI.
- Degraded speech with non-voice fallback.
- Classroom display disconnected.
- Teacher controller disconnected.
- Backend authority unavailable with authoritative mutation freeze.
- Save pending, save failed, and retry required.
- Unsaved changes.
- Permission denied or invalid pairing.
- Unsupported input or content operation.

### Responsive and Accessibility

- Teacher and display surfaces must support their distinct viewport and interaction contexts.
- Core actions must be keyboard accessible where the device provides a keyboard.
- Focus, contrast, labels, target size, and motion behavior must be appropriate for classroom use.
- Mathematical expressions require an accessible representation where technically feasible; the exact rendering technology remains open.

---

## 9. Notifications and User Communication

MVP requires in-product communication for:

- AI generation progress and failure;
- validation status and warnings;
- pairing status;
- connection and synchronization status;
- unsaved session state;
- recovery or conflict resolution outcome.

Email, push notification, and cross-session messaging are not required for the core MVP.

---

## 10. Search, Filter, Sort, and Discovery

Search, filtering, and sorting across a lesson library are not yet defined as MVP requirements. Basic selection of an existing lesson may be needed, but library scale and discovery behavior remain open.

---

## 11. Analytics and Product Instrumentation

Pilot instrumentation shall distinguish:

- hard safety/trust evidence, including projection privacy, authorization, AI publication integrity, Mathematics/content blocking, session authority, recovery, save acknowledgement, and raw-audio retention;
- primary product metrics for workflow completion, teacher-control confidence, teaching-flow fit, reuse intent, and live-adaptation usefulness;
- diagnostic metrics for proposal decisions, latency, reconnect convergence, degradation, disconnects, save retries, facilitator intervention, teaching interruptions, assurance outcomes, and curriculum-provenance failures.

The metric definitions and initial thresholds are defined in [PILOT_PLAN.md](../06_delivery/PILOT_PLAN.md). Exact analytics provider, event schema, and evidence-driven diagnostic latency targets remain open; telemetry retention follows [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md). Telemetry must be privacy-minimized; no student profiling or default raw-audio retention is required for MVP.

---

## 12. Data and Privacy Expectations

- Collect only teacher, lesson, session, and operational data needed for product behavior.
- Do not require student identity or student device data for core MVP.
- Teacher-owned lessons and stable lesson versions remain retained until the teacher deletes the lesson.
- Saved classroom session history and retained annotations expire after `90 days` by default and may be deleted earlier by the teacher.
- Raw push-to-talk audio must be ephemeral by default and excluded from normal persistence, logs, analytics, and session history.
- Full transcription or command content, prompts, raw provider payloads, and unaccepted proposal bodies are transient by default; diagnostic persistence, where genuinely necessary, has a maximum default window of `24 hours`.
- Accepted AI content follows the lifecycle of the retained lesson or session artifact into which it is accepted; required assurance and curriculum provenance follows that artifact.
- Teacher-owned retained lesson/session data requires an authorized export process and teacher-requested deletion path.
- Accepted deletion requests make data inaccessible from ordinary product use, require primary purge within `30 days`, and require backup expiry within `30 additional days` unless a documented narrow preservation requirement applies.
- Event-level pilot telemetry and identifiable research evidence expire or are appropriately de-identified no later than `90 days` after final pilot-report acceptance.
- Private teacher information must not leak to classroom display or analytics.
- Authorization must be enforced by backend behavior, not client visibility.

The canonical detailed policy is [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md). Retention does not authorize broader data collection or replace applicable legal/privacy review.

---

## 13. Integrations

| Integration | Purpose | Status |
|---|---|---|
| AI model/provider | Lesson and live adaptation proposals | Open Architecture Decision |
| Speech recognition | Push-to-talk command transcription | Open Architecture Decision |
| Curriculum source | Controlled and versioned curriculum grounding | Normative authority selected; ingestion, retrieval, integrity, local-context modeling, and guidance usage/licensing remain architecture follow-up |
| Mathematics validation engine | Deterministic validation for scoped content | Open Architecture Decision |

Integrations must be isolated behind supported application boundaries and must not become authoritative owners of classroom state.

---

## 14. Release Scope

### Required for MVP Baseline

- `CAP-LESSON-001`
- `CAP-SESSION-001`
- `CAP-CANVAS-001`
- `CAP-INK-001`
- `CAP-ADAPT-001`
- `CAP-MATH-001`
- `CAP-SESSION-002`
- Required security, privacy, validation, degraded-state, and recovery behavior.

### Can Be Deferred

- Advanced lesson library discovery.
- Additional subjects or grade ranges.
- Student-device participation.
- Import and advanced visualization capabilities.
- Commercial billing or school administration.

### Product Release Blockers

- Teacher-private information can appear on classroom display.
- Unauthorized participant can control or join a session.
- Unstructured or executable AI output can reach classroom rendering.
- Supported mathematical content can bypass required validation without a visible state.
- Curriculum claims cannot identify authority level, controlled source, source version, and relevant provenance.
- Session state can diverge across clients without deterministic reconciliation.
- Raw audio or long-lived raw AI working data is retained contrary to [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md).
- Saved session history exceeds the approved retention period without an explicit approved purpose.
- Teacher deletion only hides data without a defined primary-purge and backup-expiry path.
- User-visible deletion or export behavior cannot be evidenced as truthful and authorized.
- Real-classroom Stage B begins without tested retention, deletion, export, account-deletion, and backup-expiry handling.
- Critical failure states have no recoverable teacher experience.
- AI-generated semantic content can reach classroom display without required post-generation teacher approval.
- A `BLOCKED` proposal can be forced into authoritative classroom state.
- Approval can be replayed onto another, replaced, or stale proposal.
- A client mutates authoritative classroom state while backend authority is unavailable.
- Newly created offline state-changing commands are replayed automatically after reconnect.
- Classroom display accepts student-facing mutations before safe resynchronization completes.
- Product reports save success without durable authoritative acknowledgement.
- Degraded mode bypasses Q-02 publication approval or applicable assurance policy.

### Commercial Release Readiness

Commercial billing remains deferred from MVP and pilot. A paid Teacher Pro launch must satisfy the gates in [BUSINESS_MODEL.md](./BUSINESS_MODEL.md), including product-value evidence, measured unit economics, tested free-tier limits, willingness-to-pay evidence, explicit numeric-price approval, tested truthful billing, defined cancellation/refund behavior, reviewed commercial/privacy notices, compliant data-retention implementation, paying-user support readiness, and resolved commercial release blockers.

---

## 15. Delivery Dependencies

- Architecture decisions for identity, realtime synchronization, persistence, AI, speech, mathematics validation, and deployment.
- Curriculum ingestion/provenance implementation and permitted usage model for any copied or redistributed guidance content.
- A test corpus for fractions, algebra, and linear equations.
- Pilot environment assumptions and target device/browser evidence.
- Product ownership and requirement approval authority are defined in [PRODUCT_GOVERNANCE.md](./PRODUCT_GOVERNANCE.md). Real-classroom Stage B remains dependent on implementation and testing of the approved [DATA_RETENTION_POLICY.md](../06_delivery/DATA_RETENTION_POLICY.md), applicable privacy/consent review and notices, a tested authorized export process, operational deletion and backup-expiry procedures, accepted deployment/support readiness, reviewed target device/browser/network evidence, explicit disposition of high-impact pilot risks, and the named Product Owner's go/no-go decision after all required entry evidence is satisfied.
- The business model is defined in [BUSINESS_MODEL.md](./BUSINESS_MODEL.md). Paid commercial launch remains deferred until its launch gates are satisfied and does not proceed automatically from pilot `PROCEED`.

---

## 16. Open Product Decisions

No remaining unresolved product decisions from the initialized Q-01–Q-07 / OPD-001–OPD-007 baseline.

---

## 17. Feature Specification Index

| Capability | Feature Specification | Status |
|---|---|---|
| `CAP-LESSON-001` | [Lesson Preparation](../01_features/lesson-preparation.md) | Draft |
| `CAP-SESSION-001`, `CAP-SESSION-002` | [Classroom Session](../01_features/classroom-session.md) | Draft |
| `CAP-CANVAS-001`, `CAP-INK-001` | [Classroom Canvas](../01_features/classroom-canvas.md) | Draft |
| `CAP-ADAPT-001` | [Live AI Adaptation](../01_features/live-ai-adaptation.md) | Draft |
| `CAP-MATH-001` | [Mathematics Assurance](../01_features/mathematics-assurance.md) | Draft |

---

## 18. Product Acceptance

The MVP product baseline is acceptable when:

- required capabilities satisfy their feature acceptance criteria;
- product-wide rules are covered by tests or explicit evidence;
- open decisions that block implementation or pilot are resolved;
- material product and requirement changes have approval from the named Product Owner / Requirement Approver in the applicable canonical source;
- contracts and data definitions match implementation;
- release blockers are absent;
- relevant security, privacy, accessibility, reliability, and AI evaluation evidence is available.

---

## 19. Related Documents

- [Product Brief](./PRODUCT_BRIEF.md)
- [Product Roadmap](./ROADMAP.md)
- [Product Governance and Decision Authority](./PRODUCT_GOVERNANCE.md)
- [Business Model and Commercial Path](./BUSINESS_MODEL.md)
- [MVP Pilot Plan](../06_delivery/PILOT_PLAN.md)
- [Data Retention, History, Export, and Deletion Policy](../06_delivery/DATA_RETENTION_POLICY.md)
- [Pendago → Penatika Legacy Decision Migration Review](../06_delivery/PENDAGO_MIGRATION_REVIEW.md)
- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [ADR-0005 — Layered Curriculum Authority and Versioned Provenance](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](../02_architecture/adr/ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](../02_architecture/adr/ADR-0007-graceful-degradation-without-offline-authority.md)
- [UX Flows](../03_design/UX_FLOWS.md)
- [Test Strategy](../04_engineering/TEST_STRATEGY.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)

---

## 20. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.9` | `2026-09-06` | Resolve `OPD-007` and add teacher-first commercial and release-readiness rules | Codex |
| `0.8` | `2026-09-06` | Resolve `OPD-006` and add founder-led product and requirement approval rules | Codex |
| `0.7` | `2026-09-06` | Resolve `OPD-005` with product-wide retention, history, export, and deletion requirements | Codex |
| `0.6` | `2026-09-06` | Add legacy-authority and contract revalidation rules for resolved Q-05 | Codex |
| `0.5` | `2026-09-06` | Resolve OPD-004 with staged pilot scope, gates, and metrics | Codex |
| `0.4` | `2026-09-06` | Resolve OPD-003 with resilience-oriented degradation and truthful recovery/save behavior | Codex |
| `0.3` | `2026-09-06` | Resolve OPD-001 with post-generation teacher approval and execution classes | Codex |
| `0.2` | `2026-09-06` | Resolve curriculum authority hierarchy and strengthen provenance requirements | Codex |
| `0.1` | `2026-09-06` | Initial capability baseline from confirmed Product Brief | Codex |
