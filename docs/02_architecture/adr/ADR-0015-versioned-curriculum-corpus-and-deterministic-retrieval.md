# ADR-0015 — Use a Versioned Controlled Curriculum Corpus with Deterministic Retrieval

| Field | Value |
|---|---|
| ADR | `ADR-0015` |
| Status | Accepted |
| Date | `2026-09-07` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-LESSON-001`, `CAP-MATH-001`, `PR-007`, `PR-013`, `PR-014`, `PR-042`, `FR-MATH-006`, `FR-MATH-010`, `FR-MATH-011`, `FR-MATH-012` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

OAD-009 selects curriculum ingestion, normalization, integrity/versioning,
local-context modeling, and retrieval. [ADR-0005](./ADR-0005-layered-curriculum-authority.md)
already establishes the three-level curriculum authority model
(`NORMATIVE`, `OFFICIAL_GUIDANCE`, `LOCAL_CONTEXT`), the MVP normative
source (Keputusan Kepala BSKAP Nomor 046/H/KR/2025), the
grade-to-phase mapping (Grade 4 → Phase B, Grade 5–6 → Phase C, Grade 7–9 →
Phase D), and the provenance requirements every grounded curriculum claim
must satisfy. [ADR-0004](./ADR-0004-ai-assurance-boundary.md) already
establishes that AI self-evaluation cannot make content
curriculum-authoritative. Neither ADR selected how curriculum data is
acquired, versioned, stored, activated, or retrieved at runtime — that is
this decision.

[ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) establishes
module-first Hexagonal Architecture and the rule that persistence stays
behind application-owned output ports. [ADR-0014](./ADR-0014-postgresql-flyway-sql-first-persistence.md)
establishes PostgreSQL as the primary authoritative database and Flyway as
the schema-migration tool, while explicitly separating physical schema
evolution from reference-data content evolution. [DATA_MODEL.md](../DATA_MODEL.md)
already defines `Curriculum Source Version` and `Curriculum Reference` as
conceptual domain concepts owned by the Curriculum module.
[Mathematics Assurance](../../01_features/mathematics-assurance.md)
(`FR-MATH-006`, `FR-MATH-010`, `FR-MATH-011`, `FR-MATH-012`) requires
grounded curriculum claims to carry authority level, controlled source,
source version, phase/scope, and provenance, and requires that retrieval
rank, AI confidence, or similarity never promote one authority level into
another.

This decision must resolve how Penatika turns an official controlled source
into runtime-retrievable, provenance-bearing curriculum data without making
curriculum authority depend on AI model memory, live web retrieval,
uncontrolled scraping, or embedding similarity ranking — while keeping the
initial MVP scope small (Grade 5 Fractions; Grade 7 Basic Algebra / Linear
Equations) rather than requiring ingestion of the entire national
curriculum before pilot.

This ADR does not scaffold source, create database schema or migrations,
create field-level OpenAPI/JSON Schema, create curriculum corpus data
files, select an AI provider (OAD-006), select a vector database, or
resolve the Mathematics validator approach (OAD-008).

## Decision

Penatika uses a **controlled, versioned, human-verified curriculum corpus**
with **deterministic metadata-first retrieval**:

```text
official controlled source
        ↓
registered source version
        ↓
integrity verification
        ↓
human-reviewed normalization
        ↓
versioned Penatika curriculum corpus
        ↓
explicit activation
        ↓
runtime deterministic retrieval
        ↓
provider-neutral curriculum context
```

Runtime curriculum authority does not depend on AI model memory, live web
search, arbitrary PDF retrieval, embedding similarity, vector ranking,
provider-specific RAG, or unreviewed scraping. AI consumes curriculum
authority; AI never creates curriculum authority.

This ADR does not select an AI provider, a vector database, or the
Mathematics validator approach, and does not create the actual corpus data,
physical schema, or contracts described conceptually below.

## Authority Model

[ADR-0005](./ADR-0005-layered-curriculum-authority.md) remains authoritative
for the three-level model:

1. `NORMATIVE`
2. `OFFICIAL_GUIDANCE`
3. `LOCAL_CONTEXT`

These levels remain structurally distinct and are never flattened into one
ranked search-result list whose position determines truth. Retrieval
relevance is not authority. Similarity score is not authority. AI
confidence is not authority. A `LOCAL_CONTEXT` or `OFFICIAL_GUIDANCE` item
can never become `NORMATIVE` because it ranks more highly, matches more
strongly, or an AI provider treats it as more relevant.

For MVP Mathematics, the `NORMATIVE` source remains Keputusan Kepala BSKAP
Nomor 046/H/KR/2025 (`FR-MATH-011`). The verified current policy state is
that the 2026 BKPDM amendment changes only Pendidikan Agama dan Budi
Pekerti; Mathematics remains under 046/H/KR/2025. The existing Q-01 /
ADR-0005 product decision is unchanged by this ADR. The grade-to-phase
mapping remains Grade 4 → Phase B, Grade 5–6 → Phase C, Grade 7–9 → Phase D.
Subject scope is not broadened beyond Mathematics.

## Controlled Source Registry

A conceptual `CurriculumSource` (source registry) represents stable source
identity, independent of any particular version. It conceptually retains:

- authority level;
- stable source identifier;
- official title;
- issuing authority;
- jurisdiction;
- subject;
- source kind;
- official location/reference;
- applicable scope;
- content-use / licensing review status.

No physical columns are defined here. A registered source is not
automatically active — registration is metadata acquisition, not authority
activation.

## Source-Version Model

A conceptual `CurriculumSourceVersion` represents one immutable official
source version. It conceptually retains:

- the owning `CurriculumSource`;
- official version / decision number;
- publication/effective metadata where applicable;
- subject and applicable phase/scope;
- acquisition provenance;
- source-artifact integrity digest;
- verification state;
- activation state;
- supersession relationship.

For acquired source artifacts, the integrity-digest baseline is **SHA-256**;
no custom cryptography is invented. When exact source bytes are available
during controlled ingestion, the SHA-256 digest of the artifact used is
recorded. Storing large source PDF binaries in PostgreSQL is not required;
raw artifact archival/storage remains a separate implementation/operations
decision.

## Curriculum-Corpus Versioning

Penatika's own normalization is versioned **separately** from the official
source version — this distinction is mandatory. For example, official
version `046/H/KR/2025` may correspond to Penatika normalization "corpus
revision A" and later "corpus revision B"; a correction to Penatika's
normalization does not change the official source version.

A conceptual `CurriculumCorpusVersion` represents Penatika's immutable
normalized representation of one or more controlled source versions for a
defined scope. It conceptually retains:

- corpus identity/version;
- source-version references;
- normalization/schema version;
- corpus integrity digest;
- creation/review information;
- validation state;
- activation state;
- supersession relationship.

After activation, a corpus version is not edited in place. Correction
follows: old corpus → new corpus version → verify → explicitly activate.
Historical lesson provenance remains bound to the old corpus/version.

When curriculum corpus implementation begins, Penatika should maintain the
normalized controlled corpus as a reviewable, version-controlled import
artifact: the official source remains the semantic authority, and the
version-controlled Penatika corpus is the controlled normalized
representation used to populate runtime persistence. PostgreSQL is the
runtime materialized authoritative store for activated curriculum records.
Ad-hoc manual production `INSERT`/`UPDATE` is not the normal curriculum
publishing workflow. This ADR does not create the actual import bundle or
its file schema; exact repository path and field-level format may be
established during curriculum implementation.

## Normalization Model

A conceptual `CurriculumEntry` is the atomic grounding unit inside a
`CurriculumCorpusVersion`. It should be able to represent:

- authority level;
- subject;
- phase/scope;
- curriculum element/domain where applicable;
- source reference identity;
- normalized topic classification;
- controlled topic aliases/bindings;
- source-supported statement/content needed for grounding;
- source/corpus provenance.

No physical fields are defined here. Source text is not split into
arbitrary chunks merely because generic RAG systems use chunks; the unit
boundary must preserve curriculum meaning and source reference.

Content ingestion is source-policy aware: a document being publicly
downloadable does not imply unrestricted commercial redistribution is
allowed. For source content, Penatika stores/uses only the amount and form
approved for the source, retains citation/reference metadata, and separates
source-metadata registration from substantial-content ingestion.

## Ingestion and Activation Workflow

New curriculum data is not active merely because it was downloaded or
normalized:

```text
REGISTER SOURCE VERSION
        ↓
ACQUIRE / VERIFY SOURCE
        ↓
NORMALIZE
        ↓
SCHEMA / INTEGRITY VALIDATE
        ↓
HUMAN REVIEW
        ↓
ACTIVATE
        ↓
RUNTIME RETRIEVAL
```

Exact lifecycle status names are not physical-schema decisions in this ADR.
For `NORMATIVE` content, activation requires explicit accountable review.
No crawler or AI may self-activate national curriculum authority.

A controlled curriculum import process should eventually validate the
import artifact, verify version/integrity, reject mutation of an existing
immutable version, import idempotently, and activate only after required
review. Exact implementation is deferred.

## Human Review Boundary

For the MVP controlled normative corpus, automated extraction/parsing may
assist preparation, but human verification is required before activation.
Review must verify at least: source/version; authority level; phase/scope;
normalized statement; topic mapping; and source reference/provenance. Two
independent reviewers are not required as an architecture requirement in
the current founder-led MVP, but reviewer identity/evidence should be
recorded.

## Normative MVP Corpus

Architecture may support Grade 4–9 Mathematics, but initial normalized
corpus work prioritizes the existing pilot scope: Grade 5 Fractions and
Grade 7 Basic Algebra / Linear Equations. Ingesting all Indonesian subjects,
or normalizing the entire national curriculum before pilot, is not
required. Depth and correctness within controlled MVP scope are preferred
over broad coverage.

The initial pilot corpus should support controlled topic identifiers/
bindings for at least `FRACTIONS`, `BASIC_ALGEBRA`, and `LINEAR_EQUATIONS`.
Exact naming and taxonomy schema remain implementation decisions; the
architecture principle is that topic-to-curriculum mapping is a
reviewable/versioned controlled data concern, not something hidden inside
an AI prompt.

## Official Guidance / Licensing Boundary

The official `Panduan Mata Pelajaran Matematika 2025` remains
`OFFICIAL_GUIDANCE`, not `NORMATIVE`. The current official repository
identifies this work under Creative Commons Attribution-NonCommercial 4.0
International. Penatika has an approved future paid Teacher Pro commercial
path (per `PR-054`, safety/trust controls including curriculum provenance
are never paid-only; this licensing boundary concerns copyright/usage
rights, not commercial gating of trust features).

Therefore, Penatika does not ingest or redistribute substantial Official
Guidance content as part of the default runtime corpus until an explicit
licensing/usage/legal review approves a compliant use. For now, the
architecture supports source-registry metadata, source/version reference,
and licensing-review state for this source; its substantial content
remains disabled/not activated pending review. This is a conservative
product boundary, not a definitive legal interpretation.
`OFFICIAL_GUIDANCE` is not removed from the conceptual authority model; it
may be activated later after documented approval.

## Local Context Model

A conceptual `LocalCurriculumContextVersion` represents teacher-owned local
context: ATP sequencing, KSP/KOSP-derived context, lesson-plan sequencing,
explicit teacher sequencing decisions, or a teacher-entered local curriculum
note. It must be explicitly identified as `LOCAL_CONTEXT`, owned/authorized
by the `TeacherAccount`, versioned, traceable when used for lesson
grounding, and independently retained/deleted according to teacher-owned
data policy. Local context must not mutate `NORMATIVE` corpus entries. This
ADR does not implement student identity and does not introduce
`SCHOOL_ADMIN`.

Generic curriculum-document upload ingestion is not introduced during MVP
(PDF/PPT import is already out of MVP scope). For initial implementation,
local context may be captured through controlled teacher-entered/selected
structured context; exact UX and fields remain deferred. OCR/document
parsing infrastructure is not introduced through this ADR.

Local sequencing may differ from a broad national phase-level curriculum
statement. When local context affects sequencing, both `NORMATIVE`
provenance and `LOCAL_CONTEXT` provenance are retained. If local context
appears to contradict — rather than merely sequence or narrow — the
normative source, the system does not silently merge them; it returns an
explicit conflict/warning state for teacher-private handling. Local context
never rewrites national normative truth.

## Retrieval Strategy

Runtime retrieval uses **deterministic metadata-first retrieval**. Primary
retrieval dimensions are: authority level; subject; grade; deterministic
grade → phase mapping; curriculum element/domain where available;
normalized topic key; curated topic aliases/bindings; and source/corpus
activation state. For example: Mathematics + Grade 5 → Phase C + topic
`FRACTIONS` → approved Phase C Mathematics normative entries. Embedding
search is not the primary retrieval path.

Lexical/text matching may assist candidate discovery inside an already
authorized/scoped corpus, but lexical score does not establish authority; a
candidate becomes usable grounding only when it resolves to a controlled
`CurriculumEntry` with explicit source/corpus provenance. This ADR does not
require PostgreSQL extensions, `pg_trgm`, or Elasticsearch/OpenSearch;
ordinary PostgreSQL structured/lexical capabilities may be evaluated at
implementation time.

No vector/embedding retrieval is selected for MVP: no pgvector, Pinecone,
Qdrant, Weaviate, Milvus, Elasticsearch/OpenSearch vector retrieval,
embeddings provider, or AI reranking. The current controlled MVP curriculum
corpus does not require a vector database because current scope is small,
phase/topic metadata is deterministic, authority must remain independent of
similarity ranking, and embeddings add provider/model/version complexity
before evidence exists. If future breadth creates a genuine
semantic-retrieval problem, vector search may later be evaluated as
candidate discovery only — even then, similarity must never redefine
authority.

## Topic Binding

Topic-to-curriculum mapping is reviewable/versioned controlled data, not
logic hidden inside an AI prompt. See "Normative MVP Corpus" above for the
initial curated topic set.

## Retrieval Provenance

Grounded retrieval retains enough provenance to reconstruct: subject;
requested grade; resolved phase; normalized topic/scope; authority level;
selected `CurriculumEntry` identities; official source versions; Penatika
corpus versions; matching strategy/rule version; and the selected
`LocalCurriculumContextVersion` where applicable. An opaque similarity score
is never the only explanation.

## Curriculum Context Bundle

A provider-neutral conceptual `CurriculumContextBundle` preserves separate
lanes such as: normative references/content; approved official-guidance
references/content; selected local-context references/content; retrieval
provenance; source versions; corpus versions; match method/version; and
warning/conflict state where applicable. Exact JSON fields and JSON Schema
are not defined by this ADR — the bundle is an application/domain concept.
OAD-006 later decides how the relevant minimal context is mapped into a
specific AI provider payload; AI provider payloads must not become the
curriculum model.

## No-Match Behavior

If controlled retrieval cannot find adequate grounding, the system returns
an explicit `NO_MATCH` / `UNGROUNDED`-equivalent state. It does not fall
back to "the AI probably knows the curriculum." A claim requiring national
curriculum grounding is not presented as grounded without a controlled
reference. Existing AI approval/assurance policy (Q-02, ADR-0004,
ADR-0006) continues to decide whether the result is warned or blocked for
the specific content path; this ADR does not redefine Q-02.

## Supersession and Historical Reproducibility

An official newer source does not automatically replace the active source:

```text
candidate official source
        ↓
register new CurriculumSourceVersion
        ↓
verify official status
        ↓
normalize as new CurriculumCorpusVersion
        ↓
review impact
        ↓
explicitly activate
        ↓
new generation/retrieval uses new active version
```

Historical retained lessons/sessions keep their original source version,
their original corpus version, and their existing `CurriculumReference`.
Historical provenance is not rewritten (`INV-012`).

Independently of official-source supersession: if Penatika discovers a
normalization error, wrong phase mapping, wrong topic binding, or a
meaning-changing typo while the official source is unchanged, it creates a
new `CurriculumCorpusVersion` rather than mutating an activated corpus
version in place.

## PostgreSQL / Hexagonal Boundary

[ADR-0013](./ADR-0013-java21-module-first-hexagonal-backend.md) remains
binding:

```text
Application / AI Orchestration / Lesson
        ↓
Curriculum application boundary
        ↓
Curriculum domain/application
        ↓
Curriculum persistence output port
        ↑
PostgreSQL curriculum adapter
```

Controlled ingestion is a separate inbound/admin/operational use case. AI
provider adapters do not read curriculum tables directly. Other modules do
not mutate curriculum-owned persistence directly.

[ADR-0014](./ADR-0014-postgresql-flyway-sql-first-persistence.md) remains
binding: PostgreSQL stores runtime activated curriculum data using
relational structures for source identity/version, authority, lifecycle,
phase/scope, corpus version, topic classification, provenance links, and
local-context ownership/version. Selective JSONB may be used later for
justified structured payloads. This ADR does not design physical tables and
does not select a vector extension.

## Reference-Data Import Boundary

Flyway controls schema; curriculum import controls curriculum reference
data. Schema version and curriculum corpus version are not the same concept
and evolve independently. Every curriculum content revision is not encoded
as a database schema migration merely because Flyway exists.

## Security and Integrity

Curriculum ingestion is a privileged controlled operation. Teachers may
create/modify their own `LOCAL_CONTEXT` where product behavior permits.
Teachers must not modify the `NORMATIVE` corpus, promote guidance/local data
to `NORMATIVE`, or activate national curriculum source versions.

The design protects against: source-artifact tampering; provenance loss;
malicious imported content; authority-level escalation; unreviewed
activation; unauthorized local-context access; and stale/superseded corpus
accidentally used for new work.

Logging avoids substantial copyrighted source content or teacher
local-context content without operational need; prefer identifiers such as
`sourceVersionId`, `corpusVersionId`, `entryId`, `matchMethod`, and
`correlationId` for observability. Teacher-private local-context content is
never logged by default.

OAD-011 is not activated by this ADR. Initial curriculum ingestion is a
controlled explicit operation; a queue/broker is not required merely
because ingestion exists. If future large-scale ingestion or processing
becomes slow/retry-heavy enough to justify background execution, OAD-011 is
evaluated with actual evidence.

## Runtime Reliability

Runtime curriculum retrieval reads activated corpus data from PostgreSQL
through the Curriculum module persistence adapter. It does not require
official-website availability, PDF parsing, web scraping, or AI-provider
availability. This keeps curriculum authority available independently of
external source websites. Updating curriculum data is a controlled
ingestion process, not an ordinary classroom request; official websites are
acquisition/governance sources, not runtime dependencies, because official
URLs may change, networks may fail, source content can change, runtime
retrieval would weaken reproducibility, and arbitrarily fetched content has
not passed Penatika's activation review.

## Consequences

### Positive

- Resolves OAD-009 while keeping AI, speech, Mathematics-validator, and
  deployment decisions (OAD-006/007/008/010) open.
- Keeps curriculum authority reproducible, versioned, and independent of
  any AI provider or model.
- Preserves ADR-0005's three-level authority separation at the
  architecture level, not just as policy prose.
- Avoids premature vector/embedding infrastructure and its associated
  provider/model/version complexity.
- Gives historical lessons stable provenance across future source
  supersession and corpus corrections.
- Keeps the MVP corpus scope small and reviewable (Grade 5 Fractions;
  Grade 7 Basic Algebra / Linear Equations) rather than requiring
  wall-to-wall national curriculum ingestion before pilot.

### Negative / Costs

- Requires a human-review and explicit-activation workflow before any
  curriculum content becomes runtime-usable, which is slower than
  automatic ingestion.
- Requires maintaining two distinct version concepts (official source
  version vs. Penatika corpus version) and their relationship, which adds
  conceptual overhead relative to a single "curriculum version" field.
- Metadata-first retrieval requires curated topic bindings and taxonomy
  maintenance rather than "index everything and rank by similarity."
- The Official Guidance licensing boundary means official guidance content
  is not immediately usable for enrichment until a legal/usage review
  completes.
- Local-context conflict detection (versus mere narrowing/sequencing) adds
  design complexity to the retrieval/grounding path.

## Alternatives Considered

1. **Live retrieval from government websites at request time.** Rejected:
   creates a runtime dependency on mutable external websites, weakens
   reproducibility, bypasses activation/integrity review, and is
   inappropriate for classroom-critical authority.
2. **Entire-PDF RAG / vector database.** Acknowledged as useful for broad
   document discovery; rejected for MVP because authority is not
   similarity, the corpus is small and structured, version/provenance
   semantics matter more than similarity ranking, it adds unnecessary
   vector/provider complexity, and it makes source licensing/content-use
   boundaries harder to enforce.
3. **AI model knowledge as curriculum source.** Rejected completely as
   curriculum authority; model knowledge is unversioned and unauditable.
4. **Store one large curriculum JSON document.** Acknowledged for
   implementation simplicity; rejected as default because source/version/
   entry lifecycle, deterministic scoped queries, provenance, local
   overlays, and supersession benefit from explicit relational concepts.
   Selective JSONB remains allowed.
5. **Encode curriculum in Java constants/source code.** Rejected: curriculum
   data has an independent version/activation lifecycle, a source change
   should not require domain-code edits, and provenance becomes harder to
   manage.
6. **Use Flyway data migrations as the curriculum publishing workflow.**
   Rejected as default: schema evolution and curriculum-content evolution
   have different lifecycles, and source/corpus activation should be
   explicit and auditable independent of schema deploys.
7. **Automatically scrape and activate official sources.** Rejected: source
   discovery is not authority activation, automated parsing can be wrong,
   official scope must be reviewed, and no external automation may
   self-authorize national curriculum truth.
8. **Ingest full Official Guidance immediately.** Rejected for the current
   baseline: guidance is not normative, the current repository marks the
   guidance CC BY-NC 4.0, Penatika has a future commercial paid path, and
   substantial ingestion/redistribution requires explicit review first.
9. **Teacher local context overwrites normative entries.** Rejected:
   sequencing context and national authority have different semantics.

## Deferred Implementation Decisions

This ADR does not scaffold source, create database schema or migrations,
create field-level OpenAPI/JSON Schema, or create curriculum corpus data
files. It does not select an AI provider (OAD-006), a vector database, or
the Mathematics validator approach (OAD-008). It does not define: exact
physical table/column names; exact `CurriculumContextBundle` JSON fields or
JSON Schema; exact topic-taxonomy schema; exact teacher UX for
local-context entry; exact licensing-review outcome or process for Official
Guidance; or exact repository path/format for the version-controlled
curriculum import artifact.

## Follow-Up Decisions

1. Perform the explicit licensing/usage/legal review for `Panduan Mata
   Pelajaran Matematika 2025` before any substantial Official Guidance
   content is activated.
2. Define the physical schema for `CurriculumSource`,
   `CurriculumSourceVersion`, `CurriculumCorpusVersion`, `CurriculumEntry`,
   and `LocalCurriculumContextVersion` during source scaffolding, per
   ADR-0014's Hexagonal persistence boundary.
3. Define the version-controlled curriculum import artifact's repository
   path and field-level format during curriculum implementation.
4. Define field-level OpenAPI/JSON Schema for the
   `CurriculumContextBundle` and local-context teacher UX/contracts when
   field-level contract work begins.
5. Resolve OAD-006 (AI provider/model) so it can consume the
   `CurriculumContextBundle` as bounded provider request context.
6. Resolve OAD-008 (Mathematics validator approach), which is independent
   of but complementary to curriculum grounding.
7. Add curriculum authority/provenance test cases per
   [Mathematics Assurance](../../01_features/mathematics-assurance.md) §8.

## Related Requirements / ADRs

- [ADR-0004 — Separate AI Generation from Mathematical and Curriculum Authority](./ADR-0004-ai-assurance-boundary.md)
- [ADR-0005 — Use Layered Curriculum Authority and Versioned Provenance](./ADR-0005-layered-curriculum-authority.md)
- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](./ADR-0010-contract-first-openapi-json-schema.md)
- [ADR-0013 — Use Java 21 LTS with Module-First Hexagonal Backend Architecture](./ADR-0013-java21-module-first-hexagonal-backend.md)
- [ADR-0014 — Use PostgreSQL with Flyway and SQL-First Hexagonal Persistence](./ADR-0014-postgresql-flyway-sql-first-persistence.md)
- [Mathematics Assurance and Curriculum Grounding](../../01_features/mathematics-assurance.md)
- [Data Model](../DATA_MODEL.md)
- [Data Retention, History, Export, and Deletion Policy](../../06_delivery/DATA_RETENTION_POLICY.md)
- [System Architecture](../SYSTEM_ARCHITECTURE.md)

## Decision History

| Date | Status | Change |
|---|---|---|
| `2026-09-07` | Accepted | Resolve OAD-009 with a controlled versioned curriculum corpus, deterministic metadata-first retrieval, explicit activation, and local-context overlays |
