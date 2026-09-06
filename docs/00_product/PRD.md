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
| Version | `0.2` |
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

**Description:** Guru dapat meminta perubahan terhadap contoh, pertanyaan, penjelasan, atau visual melalui push-to-talk atau direct command. AI menghasilkan structured proposal dan tidak menjadi authoritative state owner.

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
5. Guru menerima status atau suggestion secara privat.
6. Content hanya menjadi authoritative classroom state sesuai approval policy yang berlaku.

**Outcome:** Classroom content berubah tanpa guru keluar dari teaching flow.

### J-04 — Recover and Save

1. Jika koneksi atau AI dependency bermasalah, Penatika mempertahankan safe last-known classroom state dan menunjukkan degraded status pada teacher surface.
2. Setelah reconnect, client melakukan synchronization terhadap authoritative backend state.
3. Guru mengakhiri dan menyimpan sesi.

**Outcome:** Session tidak menghasilkan conflicting authoritative state dan dapat disimpan sesuai policy.

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
- Teacher must be able to distinguish draft, proposed, validated, warning, accepted, and displayed content states.
- Mouse, touch, stylus, and keyboard behavior must be consistent for equivalent actions.

### Required States

- Empty or no lesson selected.
- Draft generation in progress.
- Validation in progress.
- Proposal ready for teacher action.
- Validation warning or failure.
- Session ready, paired, active, reconnecting, degraded, ending, saved, and failed.
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

Instrumentation should provide privacy-minimized evidence for:

- completion or abandonment of core journeys;
- AI proposal lifecycle;
- validation outcomes;
- pairing and synchronization reliability;
- degraded-mode activation and recovery;
- explicit teacher acceptance, rejection, or modification where captured.

Exact analytics provider, event schema, retention, and pilot metrics are not decided. No student profiling is required for MVP.

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

---

## 15. Delivery Dependencies

- Open product decisions `Q-02` through `Q-04` from the Product Brief.
- Architecture decisions for identity, realtime synchronization, persistence, AI, speech, mathematics validation, and deployment.
- Curriculum ingestion/provenance implementation and permitted usage model for any copied or redistributed guidance content.
- A test corpus for fractions, algebra, and linear equations.
- Pilot environment assumptions and target device/browser evidence.

---

## 16. Open Product Decisions

| ID | Decision |
|---|---|
| OPD-001 | Exact approval policy for each live AI adaptation type. |
| OPD-003 | Minimum degraded-mode capability during internet or AI outage. |
| OPD-004 | Pilot scope, success metrics, and quantitative targets. |
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
- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [ADR-0005 — Layered Curriculum Authority and Versioned Provenance](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md)
- [UX Flows](../03_design/UX_FLOWS.md)
- [Test Strategy](../04_engineering/TEST_STRATEGY.md)
- [Threat Model](../04_engineering/THREAT_MODEL.md)

---

## 20. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.2` | `2026-09-06` | Resolve curriculum authority hierarchy and strengthen provenance requirements | Codex |
| `0.1` | `2026-09-06` | Initial capability baseline from confirmed Product Brief | Codex |
