# Penatika

> 🇮🇩 Bahasa Indonesia · [🇬🇧 English](./README.en.md)

**Penatika** adalah teaching copilot untuk membantu guru Matematika Indonesia menyiapkan, menyajikan, dan mengadaptasi pelajaran secara natural saat kelas berlangsung.

> **Bicara. Tulis. Mengajar. AI mengikuti.**

Guru tetap memegang kendali. AI menghasilkan proposal konten terstruktur di belakang layar, bukan bertindak sebagai guru otonom atau generic chatbot yang mengambil alih teaching flow.

## Product Context

Penatika ditujukan untuk guru Matematika kelas 4 SD sampai kelas 9 SMP yang mengajar menggunakan laptop atau PC yang terhubung ke classroom display. Smartphone guru dapat digunakan sebagai private teaching controller.

Penatika memisahkan:

- **teacher-private surface** untuk control, AI progress, suggestion, dan validation detail;
- **classroom display** untuk konten yang sesuai dilihat siswa;
- **backend-authoritative session state** untuk menjaga synchronization lintas perangkat.

Student devices tidak diperlukan untuk core MVP.

## MVP Baseline

MVP berfokus pada satu teacher-to-classroom workflow:

1. membuat AI-assisted lesson draft dengan curriculum-aware context;
2. meninjau dan mengubah lesson sebelum kelas;
3. memulai cloud classroom session;
4. memasangkan smartphone dan classroom display tanpa screen mirroring atau shared-Wi-Fi dependency;
5. mengajar menggunakan structured classroom canvas;
6. menggunakan mouse, touch, stylus, dan digital ink;
7. meminta adaptation terhadap contoh, pertanyaan, penjelasan, atau visual;
8. menerapkan deterministic Mathematics validation bila tersedia;
9. menautkan curriculum claim ke controlled source dan version;
10. mengakhiri dan menyimpan teaching session.

Validation awal diprioritaskan untuk pecahan kelas 5 dan aljabar dasar atau persamaan linear kelas 7.

## Non-Goals MVP

- Autonomous AI teacher.
- Student-device participation.
- PDF atau PowerPoint import.
- Attendance dan school administration.
- Advanced graphing atau 3D visualization.
- Proprietary smart-board dependency.
- Mata pelajaran selain Matematika.

## Project Shape

| Concern | Decision |
|---|---|
| Base profile | `fullstack` |
| Modifier | `ai-enabled` |
| Initial backend shape | Modular monolith dengan explicit domain modules |
| Classroom state | Backend-authoritative, revision-aware |
| Classroom content | Versioned structured model; bukan arbitrary generated HTML |
| AI trust | Untrusted proposal generator; bukan authority |
| Mathematics trust | Deterministic validation untuk supported scope |
| Curriculum trust | BSKAP 046/H/KR/2025 as Mathematics normative authority; official guidance is interpretive and local context is an overlay |
| Implementation stack | Belum diputuskan |
| Deployment target | Belum diputuskan |

Material decisions tersedia di [`docs/02_architecture/adr/`](./docs/02_architecture/adr/).

## Current Status

Project Discovery dan documentation initialization sudah selesai. Repository belum memiliki application source code, package manifest, database migration, contract schema, atau deployment configuration.

Langkah berikutnya adalah menyelesaikan blocking product/architecture decisions dan membuat field-level contracts sebelum source scaffolding.

## Documentation Map

### Product

- [Product Brief](./docs/00_product/PRODUCT_BRIEF.md) — why, who, value, principles, constraints, assumptions.
- [PRD](./docs/00_product/PRD.md) — capabilities, journeys, product-wide rules, release scope.
- [Roadmap](./docs/00_product/ROADMAP.md) — outcome-oriented sequencing.

### Features

- [Lesson Preparation](./docs/01_features/lesson-preparation.md)
- [Classroom Session](./docs/01_features/classroom-session.md)
- [Classroom Canvas and Digital Ink](./docs/01_features/classroom-canvas.md)
- [Live AI Adaptation](./docs/01_features/live-ai-adaptation.md)
- [Mathematics Assurance and Curriculum Grounding](./docs/01_features/mathematics-assurance.md)

### Architecture and Design

- [System Architecture](./docs/02_architecture/SYSTEM_ARCHITECTURE.md)
- [Conceptual Data Model](./docs/02_architecture/DATA_MODEL.md)
- [Non-Functional Requirements](./docs/02_architecture/NON_FUNCTIONAL_REQUIREMENTS.md)
- [UX Flows](./docs/03_design/UX_FLOWS.md)
- [Design System](./docs/03_design/DESIGN_SYSTEM.md)

### Engineering and Delivery

- [Test Strategy](./docs/04_engineering/TEST_STRATEGY.md)
- [Threat Model](./docs/04_engineering/THREAT_MODEL.md)
- [Developer Setup](./docs/05_operations/DEVELOPER_SETUP.md)
- [Configuration](./docs/05_operations/CONFIGURATION.md)
- [Risks](./docs/06_delivery/RISKS.md)
- [Known Limitations](./docs/06_delivery/KNOWN_LIMITATIONS.md)

Deployment, Runbook, dan Release Checklist tetap dipertahankan sebagai conditional documentation sampai deployment maturity cukup.

## Repository Rules

`AGENTS.md` adalah repository-wide instruction source untuk AI coding agents dan contributor. Sebelum implementation:

1. baca relevant PRD capability;
2. baca relevant feature specification;
3. baca System Architecture dan relevant ADR;
4. buat/update contract bila cross-component boundary berubah;
5. baca engineering standard yang relevan;
6. implementasikan smallest coherent change dengan evidence.

Jangan mengarang requirement, mengubah architecture secara diam-diam, menyimpan secrets, atau menganggap AI output authoritative.

## Validate Documentation

```bash
python3 scripts/validate_template.py --project-mode
```

Validator ini memeriksa local Markdown links dan unresolved core metadata placeholders. Validator tidak membuktikan bahwa product, architecture, security, atau application behavior sudah benar.

## Open Decisions Before Implementation

- Curriculum ingestion, provenance contracts, local-context modeling, serta usage/licensing untuk official guidance.
- Approval policy untuk live AI adaptation.
- Minimum degraded-mode capability.
- Client dan backend technology.
- Database dan migration tooling.
- Identity dan authentication model.
- Realtime transport dan reconnect protocol.
- AI dan speech provider strategy.
- Mathematics validator approach.
- Field-level structured contracts.
- Deployment environment dan secret management.
- Product owner dan requirement approver.

Lihat Product Brief, PRD, System Architecture, Risks, dan Known Limitations untuk daftar lengkap beserta decision timing.
