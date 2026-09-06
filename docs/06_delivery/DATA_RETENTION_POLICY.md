# Penatika Data Retention, History, Export, and Deletion Policy

> Canonical detailed source for `OPD-005`. This document defines the Penatika MVP and first-pilot product data-lifecycle baseline; it is not a legal privacy policy or Terms of Service.

## Metadata

| Field | Value |
|---|---|
| Product | Penatika |
| Status | Approved MVP / Pilot Baseline |
| Version | `0.2` |
| Date | `2026-09-06` |
| Scope | MVP and first pilot |
| Legal Status | Product baseline; applicable legal/privacy review still required where relevant |

## 1. Purpose

This policy resolves `OPD-005` by defining product expectations for lesson and session history, data retention, teacher export, deletion processing, account deletion, and backup expiry. It establishes purpose-limited retention and data minimization without selecting a database, storage provider, identity provider, physical schema, API contract, export format, or legal compliance framework.

Retention under this policy does not authorize Penatika to collect additional data. A data class may be retained only when its collection is otherwise required and permitted for a defined teacher, product, safety, operational, or pilot-evidence purpose.

## 2. Governing Principles

1. Retain product data only as long as it serves a defined teacher, product, safety, operational, or pilot-evidence purpose.
2. Raw transient AI and speech data must not become product history by default.
3. Accepted classroom or lesson content follows the lifecycle of the artifact it became part of.
4. Unaccepted AI working data has a much shorter lifecycle than accepted product artifacts.
5. Teachers must be able to delete teacher-owned lessons and saved sessions.
6. Teacher account deletion triggers deletion or anonymization of teacher-owned personal product data, subject only to documented operational, security, or legal exceptions.
7. Penatika distinguishes active data, deletion processing, primary-store purge, and backup expiry.
8. Product UI must not claim that data is deleted when it is only hidden from ordinary access.
9. Student identity is not required for MVP and must not be introduced merely to support history or analytics.
10. Data collected for one purpose does not automatically become available for another purpose.
11. This policy is a product baseline, not a claim of complete legal or privacy compliance.

## 3. Data Classification and Purpose

| Data Class | Allowed MVP / Pilot Purpose | Boundary |
|---|---|---|
| Teacher account and profile | Ownership, authorization, teacher product access, and support of teacher-requested lifecycle actions | Minimize personal fields; exact identity implementation remains open |
| Lessons and lesson versions | Teacher preparation, reuse, classroom delivery, stable history, assurance, and curriculum provenance | Teacher-owned; stable versions remain immutable while retained |
| Saved classroom sessions and annotations | Teacher-visible session history, accepted classroom state, recovery evidence, and allowed pilot evidence | Not a transcript, AI conversation history, or student behavioral history |
| Raw audio, full utterances, prompts, raw provider output, and unaccepted proposal bodies | Transient request processing and narrowly necessary short-term diagnostics | Must not become ordinary product history |
| Assurance and provenance references | Explain or reproduce retained accepted content | Follow the retained content artifact they support |
| Controlled curriculum source versions | Curriculum authority, provenance, integrity, supersession, and historical reproducibility | Not ordinary teacher history; teacher-specific local context remains teacher-owned data |
| Operational and security logs | Reliability, abuse prevention, incident response, and bounded diagnostics | Privacy-minimized; exclude prohibited content and secrets |
| Pilot telemetry and research evidence | Answer approved pilot questions and support the final pilot report | Purpose-bound, minimized, and time-limited at event or identifiable level |

## 4. Retention Matrix

| Data Class | Default Retention | Deletion / Expiry Expectation |
|---|---|---|
| Teacher account and profile | Until teacher-requested account deletion or legitimate account closure under future policy | Revoke normal access immediately; delete or anonymize teacher-owned personal product data from primary active storage within `30 days`; backup copies expire no later than `30 additional days` after primary purge unless a documented narrow preservation requirement applies |
| Lessons and lesson versions | Until the teacher deletes the lesson | Remove from ordinary teacher access when deletion commits; purge primary persistent copies within `30 days`; expire backup remnants within the backup window |
| Saved classroom session history | `90 days` after session end/save | Teacher may delete earlier; primary purge completes within `30 days` of accepted deletion request; backup remnants expire within the backup window |
| Retained annotations | Follow the owning saved session, `90 days` by default | Delete with the session when the session expires or is deleted earlier; no independent indefinite ink history |
| Raw push-to-talk audio | None by default | Ephemeral processing only; never store in ordinary persistence, logs, analytics, or session history |
| Raw/full command or transcription content | Transient operational processing | If diagnostic persistence is genuinely required, maximum default retention is `24 hours`; prefer normalized intent/action metadata |
| Full AI prompts and raw provider output | Transient request processing | If diagnostic persistence is genuinely required, maximum default retention is `24 hours`; never retain secrets or credentials in payloads |
| Rejected, regenerated, abandoned, failed, or blocked proposal bodies | Transient by default | Maximum `24-hour` diagnostic window where genuinely necessary; do not make durable classroom history |
| Privacy-minimized proposal lifecycle and decision metadata | Associated saved-session lifecycle where needed | Up to the session's `90-day` retention window; exclude unnecessary proposal body content |
| Accepted AI content | Lifecycle of the lesson or session artifact into which it is accepted | Retain the accepted structured result and required provenance; do not retain a redundant raw-provider copy |
| Assurance and provenance for accepted content | Lifecycle of the retained lesson or session artifact | Must not be independently deleted if doing so would make the retained artifact misleading |
| Controlled curriculum source/version metadata | As required for provenance, integrity, supersession, and historical reproducibility | May outlive teacher-owned content; teacher-specific/local-context data follows teacher-owned data policy |
| Pairing credentials, tokens, and secrets | Short-lived only | Expire or revoke according to the eventual identity/session design; never retain reusable plaintext credentials or indefinite token history |
| Event-level operational/security logs | `30 days` by default | Longer preservation only for a specific active security incident through an explicit security process |
| Privacy-minimized event-level pilot telemetry | Through pilot analysis and up to `90 days` after final pilot-report acceptance | Delete event-level identifiable or pseudonymous records no longer required; aggregate or de-identify long-lived evidence |
| Identifiable pilot survey/interview/qualitative material | Through pilot analysis and up to `90 days` after final pilot-report acceptance | Delete identifiers and raw material no longer required or retain only appropriately de-identified findings |

## 5. Teacher Lesson History

MVP teacher-visible lesson history consists of retained lessons and stable lesson versions. Teacher-owned lessons remain retained until the teacher deletes the lesson.

- Stable lesson versions remain historically immutable while retained.
- Curriculum authority, controlled source, source version, scope, and provenance attached to a retained version remain historically stable.
- Accepted AI content saved into a lesson follows the lesson lifecycle.
- Raw generation inputs, full prompts, raw provider payloads, and rejected or abandoned proposal bodies are not lesson history.
- Retained lesson data is eligible for authorized teacher export.
- If a saved session requires historical reproducibility after lesson deletion, the session may retain only the allowed stable snapshot or reference needed for that retained session. This must not silently prevent lesson deletion forever.

## 6. Classroom Session History

A successfully saved classroom session becomes eligible for teacher-visible history for `90 days` after session end/save. A teacher may delete a saved session earlier.

Allowed session-history data may include:

- session identity and teacher ownership/reference;
- lesson-version reference or an allowed historical snapshot;
- accepted classroom state needed for history;
- retained annotations;
- save outcome;
- relevant assurance and provenance references;
- privacy-minimized action, proposal-lifecycle, and teacher-decision metadata.

Session history is not:

- a permanent transcript of everything spoken;
- a complete AI conversation history;
- raw provider logs or raw audio history;
- permanent rejected-proposal history;
- student identity, profiling, or behavioral history.

Teacher-facing history may expose date/time, lesson, topic/grade, final accepted classroom state, retained annotations where useful, an adaptation decision summary, and save status. Exact history UI remains a design decision. Save-history status must remain truthful: a session is not represented as successfully saved until the durable authoritative save path acknowledges success.

## 7. AI and Speech Data

- Raw push-to-talk audio has zero default persistence and must not appear in ordinary persistence, logs, analytics, or history.
- Raw/full utterance text or transcription is transient operational input. A genuinely required diagnostic copy has a maximum default retention of `24 hours`.
- Full prompts and raw provider payloads are transient. A genuinely required diagnostic copy has the same maximum default retention of `24 hours` and must be protected as potentially sensitive.
- Rejected, regenerated, abandoned, failed, and blocked proposal bodies are transient and may use at most the `24-hour` diagnostic window where necessary.
- Privacy-minimized lifecycle metadata may follow a saved session for up to `90 days`, including request/proposal identity, execution class, lifecycle timestamps, assurance status, teacher decision, approval or warned-override state, block/failure category, and model, policy, or validator version references.
- Once proposal content is accepted into a lesson or session, the accepted structured result follows that artifact's retention lifecycle. A redundant raw-provider copy is not retained.
- Secrets and provider credentials must never be included in retained request or response payloads.

## 8. Assurance and Curriculum Provenance

For accepted retained content, the Mathematics assurance and curriculum provenance references needed to explain or reproduce that content follow the lifecycle of the lesson or session artifact. Required provenance cannot be independently deleted while the owning artifact remains retained if deletion would make that artifact misleading.

For rejected or unaccepted proposals, retain only privacy-minimized assurance result metadata where needed for bounded diagnostics or pilot evidence.

Controlled curriculum source/version metadata is not ordinary teacher history. It may be archived beyond teacher-content retention to preserve provenance, source-version integrity, supersession tracking, and historical reproducibility. Teacher account deletion does not require deletion of controlled national curriculum source metadata, while teacher-specific or local-context data follows teacher-owned data policy.

## 9. Pilot Telemetry and Research Evidence

Pilot data collection must remain limited to approved pilot questions and evidence needs.

- Privacy-minimized event-level pilot telemetry may be retained through analysis and up to `90 days` after the final pilot report is accepted.
- After that window, delete event-level identifiable or pseudonymous records that are no longer required and aggregate or de-identify evidence needed for product learning.
- Long-lived aggregate evidence must not retain unnecessary teacher, student, prompt, lesson, or classroom identifiers or content.
- Teacher interview, survey, and moderated-session material may be retained through analysis and up to `90 days` after final pilot-report acceptance.
- Participant/contact mapping should be separated from product telemetry where practical.
- After the window, delete identifiers and raw research material no longer needed or retain only appropriately de-identified findings.
- Applicable consent and privacy notices remain a Stage B readiness requirement.

## 10. Student Data Boundary

Core MVP requires no student account, persistent student profile, student-device identity, or student profiling. Penatika must not introduce persistent student identity for session history, evaluation, or analytics. Incidental classroom content must be minimized and must not silently become student history.

## 11. Export Expectations

Teachers have the product-level right to obtain an export of retained teacher-owned lesson and session data that Penatika currently stores.

For MVP and first pilot, a self-service export UI is not required before Stage B if a tested and authorized operational/manual export procedure exists. The export must:

- include retained teacher-owned lesson and session data where technically applicable;
- exclude secrets and internal security data;
- exclude raw provider payloads that are not retained product data;
- preserve important version, assurance, and provenance information where meaningful;
- verify teacher authorization before release.

The exact export format remains an implementation and contract decision. Before commercial production, Penatika must evaluate whether self-service export becomes mandatory product scope.

## 12. Deletion Semantics

Teachers must be able to request deletion of individual lessons, individual saved sessions, and their account with teacher-owned product data.

Minimum deletion lifecycle:

```text
REQUESTED
  → inaccessible from ordinary product use
  → primary data purge within 30 days
  → backup expiry within 30 additional days
```

Product surfaces must distinguish data that is inaccessible or pending purge from data that has completed primary purge. They must not state or imply completed deletion when data has only been hidden.

Data that is already irreversibly aggregated or de-identified and can no longer reasonably identify or reconstruct the teacher or classroom record does not need to be recreated for deletion.

## 13. Account Deletion

When a teacher requests account deletion or an account is otherwise legitimately closed under future policy:

1. Revoke normal account access immediately.
2. Delete or anonymize teacher-owned personal product data from primary active storage within `30 days`.
3. Expire backup copies through the protected backup lifecycle no later than `30 additional days` after primary purge, unless an explicit documented legal or security preservation requirement applies.

Account deletion applies to teacher-owned lessons, retained sessions, retained annotations, teacher-specific local context, and other teacher-owned personal product data. It does not require deletion of controlled national curriculum source metadata or irreversible aggregate/de-identified evidence. Exact identity-provider implementation remains open.

## 14. Backup Expiry

Primary deletion and backup expiry are distinct lifecycle stages. Backup copies may remain inaccessible within protected backups after primary purge, but they must expire no later than `30 additional days` after the primary purge deadline unless a documented narrow preservation requirement applies.

The operational backup procedure must define how expiry is evidenced and how restored backups avoid reactivating data whose deletion or expiry was already due. The selected backup technology and physical implementation remain open.

## 15. Retention Extension Rule

Retention must not be extended silently. Any future longer retention for AI evaluation, training, customer support, research, compliance, or commercial analytics requires:

- an explicit documented purpose;
- a corresponding product and privacy decision;
- collection and access limited to that purpose;
- a defined retention period, deletion behavior, and evidence requirement.

Data collected for one purpose does not automatically become available for another.

## 16. Exceptions and Holds

A legal or security preservation exception may be used only when a real requirement is explicitly documented. Any hold must be narrowly scoped, access-controlled, time-bounded or reviewable, and linked to the applicable record without copying deleted sensitive content into logs.

An exception must not be invented as a general reason for indefinite retention. Longer operational-log preservation is allowed only for a specific active security incident through an explicit incident/security process.

## 17. Stage B Readiness Requirements

This policy resolves the product-policy portion of retention, history, export, and deletion. It does not by itself authorize real-classroom Stage B.

Stage B still requires:

- implementation and testing of this policy;
- applicable privacy and consent review;
- participant/privacy notices reflecting the implemented policy where applicable;
- product ownership is resolved in [PRODUCT_GOVERNANCE.md](../00_product/PRODUCT_GOVERNANCE.md);
- `sipratama` is the Product Owner / Requirement Approver;
- Stage B still requires the Product Owner go/no-go only after all other applicable entry evidence passes;
- deployment and support readiness;
- target environment review;
- explicit disposition of high-impact risks;
- operational support for expiry, deletion, export, account deletion, backup expiry, and any real preservation hold.

## 18. Implementation Follow-Ups

Before Stage B, Penatika must:

- enforce retention by data class and purpose;
- implement automated or operational expiry for `90-day`, `30-day`, and `24-hour` windows;
- implement authorized lesson, saved-session, and account deletion paths;
- evidence primary purge within `30 days` and backup expiry within `30 additional days`;
- provide a tested authorized export procedure;
- prevent persistence or logging of raw audio and prohibited raw AI working data;
- keep auditable deletion state without logging deleted sensitive content;
- test restored-backup handling for records already due for deletion or expiry;
- update applicable participant/privacy notices and operating procedures;
- collect Stage B evidence for raw-audio non-retention, session expiry, event-level pilot telemetry expiry, teacher deletion, export authorization, and participant/contact mapping separation where applicable.

These follow-ups do not select a database, storage provider, identity provider, export format, or physical schema.

## 19. Open Legal / Architecture Questions

- Which privacy, consent, notice, and jurisdiction-specific obligations apply to the intended pilot environment?
- What documented legal or security preservation requirements, if any, can apply, and who authorizes and reviews them?
- Which identity/account implementation will enforce access revocation and account deletion?
- Which persistence and backup mechanisms will evidence primary purge, backup expiry, and safe restoration without changing this policy?
- Which authorized export format and delivery process will be implemented for MVP/pilot?
- Whether self-service export becomes mandatory before commercial production.
- Which de-identified evaluation or research datasets, if any, require a separate approved purpose and retention decision?

Initialized product ownership and business-model decisions are resolved in [PRODUCT_GOVERNANCE.md](../00_product/PRODUCT_GOVERNANCE.md) and [BUSINESS_MODEL.md](../00_product/BUSINESS_MODEL.md). Remaining questions in this section are legal, architecture, implementation, operational, or future separately-approved product matters.

## 20. Change Log

| Version | Date | Change | Author |
|---|---|---|---|
| `0.2` | `2026-09-06` | Align Stage B and open-question wording with resolved ownership and business-model decisions | Codex |
| `0.1` | `2026-09-06` | Resolve `OPD-005` with the approved MVP and first-pilot data-lifecycle baseline | Codex |
