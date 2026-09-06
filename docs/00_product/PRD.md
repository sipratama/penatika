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
| Version | `0.5` |
| Owner | Open Question — belum ditetapkan |
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

The metric definitions and initial thresholds are defined in [PILOT_PLAN.md](../06_delivery/PILOT_PLAN.md). Exact analytics provider, event schema, retention, and evidence-driven diagnostic latency targets remain open. Telemetry must be privacy-minimized; no student profiling or default raw-audio retention is required for MVP.

---

## 12. Data and Privacy Expectations

- Collect only teacher, lesson, session, and operational data needed for product behavior.
- Do not require student identity or student device data for core MVP.
- Raw push-to-talk audio must be ephemeral by default and excluded from normal persistence and logs.
- Text commands, generated proposals, annotations, and session history require explicit retention decisions before implementation.
- Private teacher information must not leak to classroom display or analytics.
- Authorization must be enforced by backend behavior, not client visibility.

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
- Raw audio is retained by default or exposed through logs.
- Critical failure states have no recoverable teacher experience.
- AI-generated semantic content can reach classroom display without required post-generation teacher approval.
- A `BLOCKED` proposal can be forced into authoritative classroom state.
- Approval can be replayed onto another, replaced, or stale proposal.
- A client mutates authoritative classroom state while backend authority is unavailable.
- Newly created offline state-changing commands are replayed automatically after reconnect.
- Classroom display accepts student-facing mutations before safe resynchronization completes.
- Product reports save success without durable authoritative acknowledgement.
- Degraded mode bypasses Q-02 publication approval or applicable assurance policy.

---

## 15. Delivery Dependencies

- Architecture decisions for identity, realtime synchronization, persistence, AI, speech, mathematics validation, and deployment.
- Curriculum ingestion/provenance implementation and permitted usage model for any copied or redistributed guidance content.
- A test corpus for fractions, algebra, and linear equations.
- Pilot environment assumptions and target device/browser evidence.
- Real-classroom Stage B remains dependent on applicable privacy review, resolved retention/deletion rules for collected data, a named product/requirement approver, accepted deployment/support readiness, reviewed target device/browser/network evidence, and explicit disposition of high-impact pilot risks.

---

## 16. Open Product Decisions

| ID | Decision |
|---|---|
| OPD-005 | Lesson/session retention, history, export, and deletion expectations. |
| OPD-006 | Product ownership and requirement approval authority. |
| OPD-007 | Business model and commercial release path. |

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
- contracts and data definitions match implementation;
- release blockers are absent;
- relevant security, privacy, accessibility, reliability, and AI evaluation evidence is available.

---

## 19. Related Documents

- [Product Brief](./PRODUCT_BRIEF.md)
- [Product Roadmap](./ROADMAP.md)
- [MVP Pilot Plan](../06_delivery/PILOT_PLAN.md)
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
| `0.5` | `2026-09-06` | Resolve OPD-004 with staged pilot scope, gates, and metrics | Codex |
| `0.4` | `2026-09-06` | Resolve OPD-003 with resilience-oriented degradation and truthful recovery/save behavior | Codex |
| `0.3` | `2026-09-06` | Resolve OPD-001 with post-generation teacher approval and execution classes | Codex |
| `0.2` | `2026-09-06` | Resolve curriculum authority hierarchy and strengthen provenance requirements | Codex |
| `0.1` | `2026-09-06` | Initial capability baseline from confirmed Product Brief | Codex |
