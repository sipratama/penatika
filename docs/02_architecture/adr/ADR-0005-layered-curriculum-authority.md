# ADR-0005 — Use Layered Curriculum Authority and Versioned Provenance

| Field | Value |
|---|---|
| Status | Accepted |
| Date | `2026-09-06` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-MATH-001`, `FR-MATH-006`, `FR-MATH-010`, `FR-MATH-011`, `PR-007`, `PR-013`, `PR-014` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

Penatika generates and adapts Mathematics teaching content for Indonesian teachers. Curriculum-related claims can influence lesson preparation and live classroom content, so the system needs an explicit authority model rather than treating every curriculum-like source as equally authoritative.

The MVP targets Mathematics for Grade 4 SD through Grade 9 SMP. At the time of this decision, Mathematics Capaian Pembelajaran remains governed by **Keputusan Kepala BSKAP Nomor 046/H/KR/2025**. The 2026 amendment under **Keputusan Kepala BKPDM Nomor 020 Tahun 2026** changes only Pendidikan Agama dan Budi Pekerti; other subjects, including Mathematics, continue to refer to 046/H/KR/2025.

Penatika also needs to distinguish national normative curriculum truth from official pedagogical guidance and from school/teacher sequencing decisions.

## Decision

Penatika will use a **three-level curriculum authority model**.

### Level 1 — National Normative Authority

For MVP Mathematics curriculum claims, the normative authority is:

> **Keputusan Kepala BSKAP Nomor 046/H/KR/2025 tentang Capaian Pembelajaran pada Pendidikan Anak Usia Dini, Jenjang Pendidikan Dasar, dan Jenjang Pendidikan Menengah — Capaian Pembelajaran Matematika.**

Within the Penatika MVP scope:

- Grade 4 maps to **Fase B**;
- Grade 5–6 map to **Fase C**;
- Grade 7–9 map to **Fase D**.

Normative claims such as whether a competency belongs to a curriculum phase must resolve to this authority or to a later official superseding source explicitly activated by Penatika.

### Level 2 — Official Interpretive Guidance

Official Kemendikdasmen Mathematics guidance, including the **Panduan Mata Pelajaran Matematika 2025**, may be used as secondary interpretive guidance for:

- pedagogical interpretation;
- learning progression;
- example activities;
- assessment inspiration;
- topic organization.

Guidance content is **not normative authority** and must never be presented as a mandatory national curriculum requirement solely because it appears in the guide.

Copying, redistribution, embedding, or commercial use of source content requires explicit licensing/usage review before implementation. Reference or interpretation does not imply permission to redistribute the full source.

### Level 3 — Local School / Teacher Context

School curriculum, KSP/KOSP, ATP, lesson plans, and explicit teacher decisions may define local sequencing and classroom context.

Local context may determine, for example, that a Phase C competency is being taught in Grade 5 during a particular lesson. It does not redefine the national curriculum authority.

### AI Authority

AI providers are never curriculum authorities.

AI may:

- retrieve or receive grounded curriculum context;
- generate structured proposals using that context;
- explain or adapt content.

AI may not:

- create a new curriculum truth;
- promote interpretive guidance to normative status;
- infer local sequencing and present it as national policy;
- mark an ungrounded curriculum claim as authoritative.

## Provenance Requirements

Every grounded curriculum claim must retain enough provenance to answer:

```text
What authority level?
Which source?
Which source version?
Which subject?
Which phase/scope?
Which relevant reference?
How was this claim matched or retrieved?
Which local context, if any, affected sequencing?
```

The conceptual provenance model must support at least:

- `authority_level`: `NORMATIVE`, `OFFICIAL_GUIDANCE`, or `LOCAL_CONTEXT`;
- stable source identifier;
- source title;
- source version / decision number;
- jurisdiction;
- subject;
- curriculum phase or applicable scope;
- relevant section/reference identity;
- retrieval or matching provenance;
- integrity/version metadata where ingested;
- local-context identifier/version where applicable.

Exact storage and contract field names remain an implementation decision.

## Consequences

### Positive

- National curriculum claims remain traceable to an explicit official authority.
- Official guidance can enrich pedagogy without being confused with regulation.
- School and teacher sequencing remains flexible without rewriting national truth.
- AI provider replacement cannot change curriculum authority.
- Future official curriculum updates can be introduced as explicit source versions instead of silently changing existing lessons.
- Teacher-facing warnings can distinguish ungrounded, stale, local, interpretive, and normative context.

### Negative / Cost

- Curriculum ingestion and retrieval must preserve provenance and authority level.
- Source version lifecycle and supersession require governance.
- Licensing/usage review is required before copying or redistributing official guidance content.
- Curriculum matching becomes more complex than simple semantic retrieval.
- Existing grounded content may need re-evaluation when an authoritative source is superseded.

## Architecture Invariants

- `INV-007`: Every curriculum claim presented as grounded identifies its authority level, controlled source, and source version.
- `INV-011`: Official guidance and local context cannot be promoted to national normative authority by AI, retrieval ranking, or implementation convenience.
- `INV-012`: A new curriculum source version does not silently rewrite provenance of previously saved lesson versions.

## Alternatives Considered

- **Treat all curriculum-related sources equally:** rejected because normative rules, pedagogical guidance, and local sequencing have different authority.
- **Use AI/model knowledge as curriculum authority:** rejected because model knowledge is unversioned, non-authoritative, and not reliably traceable.
- **Use only a national CP document and ignore local context:** rejected because CP is phase-based while real classroom sequencing may be school- or teacher-specific.
- **Copy the full official guidance into the product by default:** rejected because interpretation and redistribution are different concerns and licensing/usage must be reviewed explicitly.

## Required Follow-Up

- Define the curriculum source registry and provenance contract.
- Decide ingestion, normalization, integrity verification, and retrieval strategy for 046/H/KR/2025.
- Perform explicit licensing/usage review before storing or redistributing substantial official guidance content.
- Define how teacher/school local context is created, versioned, and displayed.
- Define stale/superseded curriculum behavior for saved lessons and new generation.
- Add curriculum authority/provenance cases to assurance tests.

## References

- Keputusan Kepala BSKAP Nomor 046/H/KR/2025:
  `https://kurikulum.kemendikdasmen.go.id/file/1753929861_manage_file.pdf`
- Kemendikdasmen clarification on the 2026 amendment:
  `https://www.kemendikdasmen.go.id/siaran-pers/15636-capaian-pembelajaran-baru-telah-terbit-yang-berubah-hanya-mata-pelajaran-agama-dan-budi-pekerti`
- Panduan Mata Pelajaran Matematika 2025:
  `https://repositori.kemendikdasmen.go.id/33608/`
- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./ADR-0004-ai-assurance-boundary.md)
- [Mathematics Assurance](../../01_features/mathematics-assurance.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-06` | Accepted | Adopt layered curriculum authority and versioned provenance for the MVP Mathematics scope |
