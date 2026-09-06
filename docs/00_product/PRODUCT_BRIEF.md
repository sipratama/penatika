# Product Brief — Penatika

> **Peran dokumen:** Source of truth untuk alasan Penatika dibuat, pengguna yang dilayani, outcome yang dituju, serta constraint product dan business yang membentuknya.
>
> Capability product berada di [PRD](./PRD.md). Perilaku feature berada di `docs/01_features/`. Technical design berada di dokumentasi architecture.

---

## Metadata Dokumen

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Draft |
| Version | `0.2` |
| Owner | Open Question — belum ditetapkan |
| Last Updated | `2026-09-06` |
| Primary Market | Indonesia |

---

## 1. Product Vision

### Vision

Membantu guru Indonesia mengajar secara lebih alami dan adaptif dengan classroom teaching surface yang mengikuti intent guru tanpa mengambil alih kendali mengajar.

### Product Statement

Untuk guru Indonesia yang perlu menyiapkan sekaligus menyesuaikan materi saat kelas berlangsung, Penatika adalah teaching copilot yang mengubah intent guru menjadi konten kelas terstruktur. Berbeda dari materi statis atau generic AI chat, Penatika menjaga guru tetap berada dalam alur mengajar, memisahkan kontrol privat dari tampilan siswa, dan memperlakukan keluaran AI sebagai usulan yang harus dibatasi serta divalidasi.

### Core Principle

> **Bicara. Tulis. Mengajar. AI mengikuti.**

Guru tetap memegang kendali. AI adalah teaching copilot, bukan guru otonom.

---

## 2. Problem

### Core Problem

Persiapan pelajaran dan delivery di kelas sering terpisah. Guru menyiapkan materi statis sebelum kelas, tetapi kebutuhan tak terduga selama mengajar mengharuskan guru mengubah penjelasan, contoh, latihan, atau visual secara manual.

Generic AI chat dapat membantu menghasilkan konten, tetapi bukan classroom teaching surface. Penggunaannya dapat memutus alur mengajar, memaksa guru mengoperasikan chatbot, mengekspos proses yang seharusnya privat, dan menghasilkan konten matematika atau klaim kurikulum yang tidak boleh dipercaya tanpa validasi.

### Why It Matters

- Guru kehilangan momentum saat harus berpindah alat atau menyusun ulang materi secara manual.
- Materi yang tidak dapat diadaptasi membatasi respons terhadap pemahaman aktual siswa.
- Konten AI yang langsung ditampilkan tanpa kontrol atau validasi dapat menyesatkan kelas.
- Screen mirroring mencampur kontrol privat guru dengan tampilan yang dilihat siswa.

### Current Alternatives

- Slide, dokumen, atau papan tulis yang disiapkan sebelum kelas.
- Perubahan manual terhadap contoh, latihan, atau visual selama kelas.
- Generic AI chat yang digunakan terpisah dari classroom display.
- Screen mirroring dari perangkat guru.
- Mengabaikan kebutuhan adaptasi dan melanjutkan materi yang sudah disiapkan.

### Evidence Status

| Evidence | Source | Confidence |
|---|---|---|
| Problem, target user, desired workflow, dan constraints | Confirmed project discovery, `2026-09-06` | High untuk intended product direction; belum menjadi market validation |
| Efektivitas solusi bagi guru dalam kelas nyata | Belum tersedia | Open assumption |

---

## 3. Target Users

### Primary User

**Who:** Guru Matematika Indonesia untuk kelas 4 SD sampai kelas 9 SMP.

**Context:** Guru menyiapkan pelajaran menggunakan laptop atau PC, menghubungkannya ke classroom display, dan dapat menggunakan smartphone sebagai private teaching controller selama kelas.

**Primary Job-to-be-Done**

> Ketika saya menyiapkan dan mengajar pelajaran Matematika, saya ingin mengubah materi secara langsung tanpa keluar dari alur mengajar, sehingga saya dapat merespons kebutuhan kelas sambil tetap mengendalikan apa yang dilihat siswa.

### Beneficiaries

| User | Need | MVP Role |
|---|---|---|
| Siswa kelas 4 SD–9 SMP | Mendapat penjelasan, contoh, latihan, dan visual yang sesuai konteks pembelajaran | Beneficiary dan viewer classroom display; bukan primary product user |

### Explicitly Not Targeted Yet

- Siswa sebagai pengguna perangkat individual.
- Administrator sekolah dan workflow administrasi sekolah.
- Mata pelajaran selain Matematika.
- Guru di luar rentang kelas 4 SD sampai kelas 9 SMP.

---

## 4. Value Proposition

### Primary Value

Guru dapat menyiapkan, menyajikan, dan mengadaptasi pelajaran Matematika dari satu teaching workflow tanpa merasa sedang mengoperasikan AI chatbot.

### Differentiation

1. Teacher-first: guru mengendalikan pacing, approval, dan classroom display.
2. Dual-surface: proses dan saran privat berada di teacher controller, sedangkan siswa hanya melihat konten yang sesuai.
3. Structured content: AI memperbarui model konten kelas, bukan menghasilkan arbitrary HTML.
4. Mathematics assurance: hasil matematika divalidasi secara deterministik bila memungkinkan.
5. Curriculum grounding: klaim kurikulum menggunakan authority level, controlled source, version, scope, dan provenance yang eksplisit.

### Product Promise

> Penatika membantu guru berimprovisasi saat mengajar sementara AI menangani generasi dan adaptasi konten terstruktur di belakang layar.

---

## 5. Desired Outcomes

### User Outcomes

- Guru dapat menghasilkan, meninjau, dan mengubah draft pelajaran sebelum mengajar.
- Guru dapat memulai sesi, memasangkan smartphone, dan mengendalikan classroom display tanpa screen mirroring.
- Guru dapat menggunakan suara, touch, mouse, stylus, dan digital ink sesuai perangkat yang tersedia.
- Guru dapat meminta variasi penjelasan, contoh, pertanyaan, atau visual selama kelas.
- Guru dapat menjaga suggestion, validation status, dan progress AI tetap privat saat diperlukan.
- Guru dapat menyimpan hasil sesi mengajar.

### Product Outcomes

- Membuktikan bahwa teacher-controlled AI classroom surface dapat mempertahankan alur mengajar.
- Membuktikan bahwa structured content dan validation boundary dapat mengurangi risiko keluaran AI yang tidak tepat.
- Menghasilkan evidence pilot yang cukup untuk memutuskan scope lanjutan.

---

## 6. Goals

### G-01 — Complete Teacher-to-Classroom Workflow

**Goal:** Guru dapat menyelesaikan alur prepare, review, present, adapt, dan save dalam satu pengalaman yang koheren.

**Evidence of Success:** Prototype atau pilot menunjukkan alur end-to-end dapat dilakukan tanpa berpindah ke generic AI chat atau screen mirroring.

### G-02 — Preserve Teacher Control

**Goal:** AI tidak menampilkan atau mengubah konten siswa di luar kebijakan kontrol dan approval guru.

**Evidence of Success:** Setiap jalur perubahan classroom content mempunyai state, authorization, dan acceptance behavior yang dapat diuji.

### G-03 — Establish Mathematics and Curriculum Trust Boundaries

**Goal:** Konten Matematika dan klaim kurikulum diperlakukan sesuai tingkat kepercayaannya.

**Evidence of Success:** Scoped mathematics content melewati deterministic validation bila tersedia dan setiap grounded curriculum claim dapat ditelusuri ke authority level, controlled source, version, scope, dan provenance yang digunakan.

---

## 7. Non-Goals

- AI guru otonom.
- Student-device participation sebagai kebutuhan core experience.
- PDF atau PowerPoint lesson import.
- Advanced graphing atau 3D visualization.
- Attendance management.
- School administration features.
- Ketergantungan pada smart board atau proprietary hardware.
- Dukungan mata pelajaran di luar Matematika pada MVP.

---

## 8. Product Principles

### P-01 — Teacher Control Before Automation

Guru menentukan pacing dan konten yang ditampilkan. Automation tidak boleh menghilangkan keputusan material dari guru.

### P-02 — Teaching Flow Before Chat Interaction

AI harus mengikuti workflow mengajar, bukan mengubah kegiatan mengajar menjadi percakapan chatbot.

### P-03 — Private Control, Appropriate Display

Kontrol, suggestion, dan status yang hanya dibutuhkan guru tidak boleh otomatis muncul pada student-facing display.

### P-04 — Structured and Verifiable Content

Classroom content menggunakan struktur yang dapat dirender konsisten, divalidasi, diuji, dan dipulihkan.

### P-05 — Graceful Degradation

Gangguan AI atau konektivitas tidak boleh membuat seluruh classroom session tidak dapat digunakan jika fungsi lokal atau state terakhir masih dapat dipertahankan dengan aman.

### P-06 — Data Minimization

Penatika hanya mengumpulkan data yang diperlukan untuk outcome product. Raw push-to-talk audio tidak disimpan secara default.

---

## 9. MVP Boundary

### In Scope

- AI-assisted lesson draft dengan curriculum-aware context.
- Review dan modification sebelum kelas.
- Cloud classroom session dengan backend-managed authoritative state.
- Pairing teacher smartphone dan classroom display tanpa screen mirroring atau shared Wi-Fi dependency.
- Structured classroom canvas.
- Voice command dan direct manipulation untuk adaptasi konten.
- Digital ink: write, highlight, erase, undo, redo, dan clear.
- Private teacher suggestions dan public classroom presentation.
- Deterministic mathematics validation bila memungkinkan.
- Layered curriculum grounding yang membedakan normative national authority, official interpretive guidance, dan local school/teacher context.
- Save classroom session.
- Prioritas validation awal: pecahan kelas 5 dan aljabar dasar atau persamaan linear kelas 7.

### Out of Scope

Semua item pada bagian Non-Goals serta capability yang belum diperlukan untuk membuktikan core teacher-to-classroom workflow.

### MVP Exit Condition

MVP siap dievaluasi untuk investment berikutnya ketika core workflow dapat digunakan end-to-end dalam prototype atau pilot, teacher control dapat dibuktikan, failure/degraded states telah diuji, dan evidence penggunaan cukup untuk menilai apakah Penatika memperbaiki alur mengajar.

Target kuantitatif dan desain pilot belum diputuskan.

---

## 10. Business Model

Monetization, payer, pricing, dan business model belum diputuskan. Keputusan ini tidak diperlukan untuk baseline MVP, tetapi harus ditetapkan sebelum commercial release planning.

---

## 11. Success Metrics

Primary metric dan target kuantitatif belum dipilih karena belum ada baseline atau desain pilot.

Candidate evidence yang perlu didefinisikan sebelum pilot:

- keberhasilan penyelesaian core teaching workflow;
- waktu atau jumlah interruption saat melakukan live adaptation;
- tingkat acceptance atau rejection terhadap AI proposals;
- jumlah validation failure yang tertangkap sebelum display;
- keberhasilan recovery dari gangguan konektivitas atau AI provider;
- qualitative teacher confidence dan perceived control.

Metric final dan target tetap menjadi Open Product Decision.

---

## 12. Constraints

### Product Constraints

- Market awal Indonesia.
- Mata pelajaran awal Matematika untuk kelas 4 SD–9 SMP.
- Teacher-first, software-first, dan hardware-agnostic.
- Student devices tidak diperlukan untuk core experience.
- Guru bertanggung jawab atas classroom content dan pacing.
- Classroom content harus terstruktur, bukan arbitrary generated HTML.

### Environment Constraints

- Minimum setup: smartphone guru, laptop atau PC, classroom display, dan koneksi internet.
- Pairing tidak boleh bergantung pada screen mirroring atau shared Wi-Fi.
- Mouse, touch, stylus, dan digital ink harus diperlakukan sebagai input yang relevan.

### Trust, Privacy, and Safety Constraints

- AI output adalah untrusted proposal sampai melewati policy dan validation yang berlaku.
- Mathematics harus divalidasi secara deterministik bila memungkinkan.
- Untuk Mathematics MVP, normative curriculum authority adalah Keputusan Kepala BSKAP Nomor 046/H/KR/2025; official guidance dan local school/teacher context memiliki authority level terpisah.
- Curriculum claims harus mempertahankan controlled source, source version, scope/phase, authority level, dan provenance.
- AI-generated content tidak pernah menjadi curriculum authority.
- Student data collection dan privacy exposure harus diminimalkan.
- Raw push-to-talk audio tidak disimpan secara default.
- Product harus mempunyai graceful degradation ketika AI atau internet tidak andal.

### Technology Constraints

- Backend-managed classroom state adalah constraint yang sudah dikonfirmasi.
- Programming language, framework, database, cloud provider, deployment target, identity solution, realtime transport, AI provider, dan speech provider belum diputuskan.

---

## 13. Dependencies

| Dependency | Purpose | Status |
|---|---|---|
| National Mathematics curriculum authority | Grounding normative curriculum claims | Selected: Keputusan Kepala BSKAP No. 046/H/KR/2025 |
| Official Mathematics guidance | Secondary pedagogical interpretation | Panduan Mata Pelajaran Matematika 2025; usage/licensing review required before copying or redistribution |
| School / teacher curriculum context | Local sequencing and classroom context | Optional contextual overlay; ingestion/model remains open |
| AI generation capability | Draft dan adaptation proposal | Provider/model belum dipilih |
| Speech recognition capability | Push-to-talk teacher commands | Provider/approach belum dipilih |
| Deterministic mathematics validation | Memeriksa hasil scoped mathematics | Engine/implementation belum dipilih |
| Internet connectivity | Cloud session dan remote services | Degradation boundary belum final |

---

## 14. Assumptions

| ID | Assumption | Validation Approach | Status |
|---|---|---|---|
| A-01 | Smartphone dapat menjadi private controller yang dapat diterima guru saat mengajar | Prototype observation dan teacher feedback | Open |
| A-02 | Structured teaching canvas lebih sedikit mengganggu alur daripada generic AI chat | Comparative prototype/pilot observation | Open |
| A-03 | Konektivitas kelas cukup untuk cloud-mediated session dengan degradation support | Environment testing pada target sekolah | Open |
| A-04 | Deterministic validation dapat mencakup risiko utama pada topik MVP | Define validation corpus dan evaluate coverage | Open |
| A-05 | Curriculum source yang dapat dikontrol dan diberi versi tersedia untuk scope awal | Official-source review | Confirmed for normative Mathematics CP; guidance usage/licensing remains implementation follow-up |

---

## 15. Product Decisions and Open Questions

### Resolved

| ID | Decision | Resolution |
|---|---|---|
| Q-01 | Curriculum source dan versi apa yang menjadi authoritative untuk MVP? | Mathematics normative authority: Keputusan Kepala BSKAP No. 046/H/KR/2025. Official guidance is secondary interpretive guidance; school/teacher context is a local overlay. See ADR-0005. |

### Open

| ID | Question | Decision Needed By |
|---|---|---|
| Q-02 | AI action mana yang memerlukan explicit approval sebelum tampil di classroom display? | Sebelum live adaptation implementation |
| Q-03 | Fungsi minimum apa yang harus tetap tersedia saat konektivitas atau AI provider terganggu? | Sebelum end-to-end classroom pilot |
| Q-04 | Seberapa luas pilot pertama di luar dua topik Matematika prioritas? | Sebelum pilot planning |
| Q-05 | Bagian mana dari materi Pendago / AI Teaching Canvas yang masih valid untuk dimigrasikan? | Sebelum requirements baseline dikunci |
| Q-06 | Business model, payer, dan commercial path apa yang dituju? | Sebelum commercial release planning |
| Q-07 | Siapa owner product dan approver requirement Penatika? | Sebelum requirement locking |

---

## 16. Related Documents

- [Product Requirements Document](./PRD.md)
- [Product Roadmap](./ROADMAP.md)
- [System Architecture](../02_architecture/SYSTEM_ARCHITECTURE.md)
- [ADR-0005 — Layered Curriculum Authority and Versioned Provenance](../02_architecture/adr/ADR-0005-layered-curriculum-authority.md)
- [Risks](../06_delivery/RISKS.md)

---

## 17. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.2` | `2026-09-06` | Resolve Mathematics curriculum authority and provenance model | Codex |
| `0.1` | `2026-09-06` | Initial Penatika product baseline from confirmed discovery context | Codex |
