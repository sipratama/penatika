# ADR-0011 — Use OIDC with Backend-Managed Browser Sessions and Scoped Pairing

| Field | Value |
|---|---|
| ADR | `ADR-0011` |
| Status | Accepted |
| Date | `2026-09-07` |
| Decision Owners | Penatika project team; named owner pending |
| Related Requirements | `CAP-SESSION-001`, `CAP-SESSION-002`, `FR-SESSION-004`, `FR-SESSION-021`–`FR-SESSION-025` |
| Supersedes | N/A |
| Superseded By | N/A |

## Context

Penatika has two browser boundaries with different authority: Teacher Web owns
teacher-facing workflows, while Classroom Display Web receives only a
classroom-safe projection. The authoritative Java/Spring Boot backend already
owns protected application behavior, classroom-session state, participant
roles, and authorization decisions.

OAD-004 must define teacher identity, browser authentication, participant
authorization, pairing, revocation, and recovery boundaries before protected
HTTP contracts or realtime credential transport are designed. The decision
must keep OAuth credentials out of ordinary browser JavaScript, preserve the
single-backend architecture from ADR-0008 and ADR-0009, and prevent pairing
from becoming an alternative teacher authentication mechanism.

## Decision

Penatika uses OpenID Connect 1.0 for teacher authentication with OAuth 2.0
Authorization Code flow, PKCE using `S256`, and backend-managed browser
sessions.

The existing Penatika Backend is the confidential OIDC client and relying
party. It performs the authorization-code exchange, validates the external
identity, retains upstream OAuth/OIDC tokens server-side when needed, resolves
the local teacher account, creates and revokes Penatika sessions, serves the
protected application API, and enforces authorization.

```text
OIDC authenticates the teacher
        ↓
Penatika Backend resolves a local TeacherAccount
        ↓
backend-managed session authenticates browser requests
        ↓
backend authorization decides access
```

Teacher Web and Classroom Display Web do not become OAuth clients that retain
OAuth bearer credentials. This decision does not introduce a separate BFF,
frontend application server, or second product authority.

## Teacher Account Model

Penatika owns a stable internal `TeacherAccount` identity. The account owns
Penatika lessons, classroom sessions, and other teacher-scoped product data.
Its conceptual lifecycle states are equivalent to:

- `ACTIVE`;
- `DISABLED`;
- `DELETION_REQUESTED`;
- `CLOSED`.

Only active accounts receive normal application authorization. OIDC proves an
external authentication event; it does not replace Penatika account lifecycle,
ownership, or authorization policy.

MVP has one human product-account role: `TEACHER`. Student, school-admin,
institution-manager, billing-admin, and product super-admin roles are not
introduced by this decision. Any future operational administration is a
separate privileged boundary.

Penatika does not store teacher passwords, password hashes, password-reset
tokens, recovery questions, or local credential-MFA secrets for MVP. Password
authentication, credential MFA, and credential recovery remain the upstream
OIDC provider's responsibility.

## External Identity Link

An `ExternalIdentityLink` associates a `TeacherAccount` with the validated OIDC
identity key:

```text
(issuer, subject)
```

The validated `iss` and `sub` pair is the authoritative external identity key.
Email address, display name, and provider username are mutable attributes and
must not be used as stable identity keys or as sufficient evidence for
authorization or account linking.

A successful OIDC login creates a new local account only when the applicable
Penatika provisioning policy permits it. Controlled pilot allowlists, future
public registration, and institution-managed invitations remain product and
onboarding decisions.

The conceptual model may support more than one external link in the future,
but multiple identities must not be linked or merged automatically by email.
Any account-linking flow requires explicit authenticated verification and a
separate security design.

## OIDC Authentication Flow

The backend initiates and completes OIDC authentication using Authorization
Code flow with PKCE `S256`. The implementation must provide:

- `state` protection;
- an OIDC `nonce`;
- exact redirect URI validation;
- issuer and subject validation;
- audience/client validation;
- signature validation;
- expiry and other applicable time validation.

OAuth implicit grant, Resource Owner Password Credentials/password grant, and
access tokens in URL fragments are prohibited.

External OIDC claims remain untrusted input until validated. Only normalized
claims required by Penatika may enter application semantics. Provider group or
role claims do not automatically become Penatika permissions.

## Backend-Managed Browser Session

After successful authentication and authorized account resolution, the backend:

1. creates server-managed authenticated session state;
2. rotates the session identifier after authentication;
3. returns an opaque random session reference in a protected cookie;
4. authenticates later browser requests from that session reference;
5. retains OAuth/OIDC access, refresh, and ID tokens server-side.

OAuth access tokens, refresh tokens, and ID tokens must not be exposed to
ordinary application JavaScript or stored in `localStorage`, `sessionStorage`,
or IndexedDB. The browser cookie is not a broad OAuth bearer credential.

Sessions must support server-side revocation, bounded idle and absolute
lifetimes, rejection after revocation, and local logout independent of
upstream logout availability. If refresh tokens are used, they remain
backend-only, use the minimum required scope, do not request `offline_access`
by default, and are discarded or revoked at local session end where supported
and appropriate.

The physical session store and exact lifetime values remain implementation,
OAD-003, and deployment decisions.

## Cookie and CSRF Security

Authenticated session cookies must be `Secure`, `HttpOnly`, narrowly scoped,
and host-only where practical. A broad `Domain` attribute must not be selected
for convenience. `SameSite=Strict` is preferred for the authenticated session
when compatible with the final OIDC deployment.

If redirect/callback mechanics require different transient behavior, the main
authenticated session remains as strict as practical and a separately scoped
authorization-transaction mechanism handles the transient flow. Any
relaxation must be documented; the implementation must not silently default to
`SameSite=None`.

Cookie authentication requires explicit CSRF protection for state-changing
requests. SameSite is not the complete CSRF strategy. A framework anti-forgery
mechanism, synchronizer token, validated custom request header with strict
CORS/origin policy, or equivalent reviewed control is required. State changes
must not use `GET`.

Cross-origin credentialed deployment requires explicit trusted-origin
allowlists and must not combine wildcard CORS with credentials. Exact cookie,
path, CSRF header, and framework configuration names remain field-level
contract and implementation decisions.

## Authorization Model

Authentication is not authorization. The backend authorizes each protected
operation using the applicable combination of:

- active authenticated `TeacherAccount`;
- object ownership;
- classroom-session ownership and state;
- participant role and authorization state;
- explicit session-scoped capability;
- current authoritative revision and policy.

A coarse global `ROLE_TEACHER` check is insufficient. An authenticated teacher
must not read or mutate another teacher's lesson, control another teacher's
session, approve another teacher's proposal, or export another teacher's data.

## Teacher Controller Authorization

A `TEACHER_CONTROLLER` participant must be backed by an active authenticated
`TeacherAccount` browser session. Pairing alone never authenticates a teacher
or grants teacher-account authority.

Controller establishment requires the backend to verify the authenticated
account, classroom-session ownership or authorization, pairing-grant validity,
and participant constraints before granting session-scoped controller
authority.

MVP permits at most one active mutation-authorized teacher controller per
classroom session. A teacher may have multiple ordinary authenticated browser
sessions, but controller replacement or handoff requires explicit backend
authorization and revokes the previous controller participant authority.

## Classroom Display Authorization

Classroom Display does not require a `TeacherAccount`. A valid display pairing
grant establishes a distinct, revocable `CLASSROOM_DISPLAY` participant
session that can read only the classroom-safe projection.

Display authority cannot issue teacher commands, call teacher-account APIs,
approve AI proposals, export teacher data, obtain teacher-private projections,
or use pairing as account authentication.

MVP permits one active classroom display participant per classroom session.
Reconnect by the same valid participant is supported. Explicit replacement or
re-pair revokes the previous display participant credential.

## Pairing Grant Model

A `PairingGrant` is a short-lived authorization grant, not a teacher password,
OAuth access token, account session, or permanent device credential. Every
grant is bound to:

- one classroom session;
- one intended participant role and purpose;
- issuance time and expiry;
- single-use state;
- revocation state.

The backend owns issuance and redemption. Machine-transferred secrets use
cryptographically strong randomness. A future human-entered short code may not
be the sole weak security secret and requires rate limits, attempt limits,
short expiry, session binding, and anti-enumeration behavior.

After redemption, the backend issues a revocable participant session instead
of requiring the pairing secret on every request. Display participant sessions
are separate from teacher-account sessions. Controller participant authority
is additionally tied to the authenticated teacher account and browser session.

## Pairing Lifetime and Revocation

The initial MVP pairing-grant lifetime is five minutes. A grant is single-use
and becomes invalid after successful redemption, explicit revocation, expiry,
or classroom-session end. It cannot refresh itself into another pairing grant
without backend authorization.

Changing the five-minute baseline requires security review. Exact QR,
deep-link, or human-code UX remains a design and contract follow-up.

Participant authority ends when the classroom session ends, the participant is
revoked or replaced, the associated teacher authorization becomes invalid for
a controller, or security policy invalidates the participant. Temporary
network disconnection alone does not revoke an otherwise valid participant;
reconnect and resynchronization remain governed by ADR-0007 and OAD-005.

## Account Lifecycle and Deletion

An accepted teacher-account deletion request immediately revokes normal
application access, invalidates active Penatika browser sessions, revokes
active controller authority, and starts the primary-purge and backup-expiry
process defined by `DATA_RETENTION_POLICY.md`.

Teacher-owned data and local external-identity links follow that canonical
lifecycle. Penatika does not claim or imply deletion of the teacher's upstream
identity-provider account.

## Recovery Boundary

Penatika does not own password or upstream credential recovery. Those flows
belong to the selected OIDC provider.

Loss of access to a linked external identity must not be recovered merely from
a matching email address, display name, or classroom pairing credential. A
future identity-relink or account-recovery flow requires explicit secure
design.

## Realtime Authentication Boundary

This decision defines who participants are and what authority they may hold.
OAD-005 remains responsible for how authenticated teacher and participant
sessions are carried into realtime communication, including reconnect
semantics.

OAD-005 must reuse these identities, revocation rules, and scoped participant
grants rather than inventing a separate realtime authentication model. This
ADR does not choose WebSocket, SSE, polling, STOMP, Socket.IO, or another
transport.

## Provider Independence

No concrete identity provider is selected. A deployment may use any
standards-compliant provider that supplies:

- OIDC discovery and stable issuer identity;
- Authorization Code flow and PKCE `S256`;
- a secure token endpoint and stable `iss` + `sub` identity;
- appropriate session, logout, and revocation capabilities;
- acceptable security, privacy, and data-processing properties.

A provider that materially changes the identity model or cannot satisfy these
requirements requires a new architecture review.

## Consequences

### Positive

- OAuth access and refresh tokens stay outside ordinary browser JavaScript.
- Server-side revocation fits logout, account disablement, participant
  replacement, and classroom-session termination.
- The local account and object-authorization model remains provider-independent.
- Teacher and display authority remain explicitly separated.
- The design reuses the authoritative backend without adding another deployable
  BFF or product authority.

### Negative / Costs

- The backend must manage OIDC transaction state, token handling, sessions,
  CSRF defenses, logout, revocation, and account provisioning securely.
- Server-managed sessions require a physical session store and lifecycle
  operations that remain to be selected.
- Cookie-authenticated cross-origin deployments add CORS, origin, and CSRF
  complexity.
- Controller/display replacement and revocation require authoritative
  participant state and concurrency enforcement.
- Account linking and recovery cannot be approximated by email and require
  deliberate future flows if introduced.

## Alternatives Considered

### Browser SPA as an OAuth/OIDC Public Client

Authorization Code with PKCE is standardized and can be implemented securely
for browser applications. It is not the Penatika baseline because access tokens
would be available to browser JavaScript, increasing token-theft consequences
from XSS or client compromise, while the existing authoritative backend can own
the OAuth client and session.

### Token-Mediating Backend

This keeps refresh tokens server-side but still delivers access tokens to
browser JavaScript. Penatika has no requirement for the browser to call a
separate resource server directly, so the authoritative backend accepting its
own browser sessions is simpler and limits token exposure.

### Separate BFF Service

A dedicated BFF can provide strong browser-token isolation, but Penatika
already has one authoritative backend. A second deployable component would add
operational complexity without a separate resource-server topology that
requires it. The Penatika Backend itself performs OIDC and session handling.

### Local Username and Password Authentication

Rejected for MVP because Penatika would own password storage, reset, recovery,
credential MFA, and abuse protection without product differentiation.

### Browser-Stored Stateless Bearer JWT

Rejected because browser bearer exposure increases token-theft risk and
stateless revocation fits controller/display replacement and account/session
invalidation poorly.

### Email as Account Identity

Rejected because email addresses change, providers validate and normalize them
differently, and automatic email-based linking creates account-takeover risk.
The external identity key is `(issuer, subject)`.

### Pairing Code as Teacher Authentication

Rejected completely. Pairing grants only session-scoped participant authority
and cannot grant broad teacher-account authority.

## Deferred Implementation Decisions

- Concrete OIDC provider and provider-specific deployment configuration.
- Account provisioning/onboarding policy and any future account-linking UX.
- Physical browser-session and participant-session store.
- Exact idle and absolute session lifetimes.
- Exact cookie names, paths, transient transaction mechanism, and CSRF names.
- Spring Security and session configuration.
- Secret manager and confidential-client credential handling.
- Exact logout, revocation, and upstream token-retention behavior supported by
  the selected provider.
- Database representation, migrations, and uniqueness constraints.
- Pairing QR/deep-link/human-code UX and exact rate/attempt limits.
- Field-level HTTP and realtime contracts.

## Follow-Up Decisions

- OAD-005 defines realtime transport, credential carriage, and reconnect
  protocol using this identity model.
- OAD-003 selects persistence and migration technology, including the physical
  session-store approach where applicable.
- OAD-010 selects deployment, environment, secret-management, and provider
  configuration.
- Field-level contract work defines protected operations, cookie security
  schemes, anti-CSRF requirements, participant sessions, and pairing shapes.

## Related Requirements / ADRs

- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [RFC 9700 — OAuth 2.0 Security Best Current Practice](https://www.rfc-editor.org/rfc/rfc9700.html)
- [RFC 10017 — OAuth 2.0 for Browser-Based Applications](https://www.rfc-editor.org/rfc/rfc10017.html)
- [ADR-0002 — Keep Classroom Session State Backend-Authoritative](./ADR-0002-backend-authoritative-session-state.md)
- [ADR-0006 — Teacher Approval and AI Publication Policy](./ADR-0006-teacher-approval-ai-publication-policy.md)
- [ADR-0007 — Graceful Degradation Without Offline Authority](./ADR-0007-graceful-degradation-without-offline-authority.md)
- [ADR-0008 — Use Browser-First React Clients](./ADR-0008-browser-first-react-client-strategy.md)
- [ADR-0009 — Use Java and Spring Boot for the Backend](./ADR-0009-java-spring-boot-backend.md)
- [ADR-0010 — Use Contract-First OpenAPI and JSON Schema Boundaries](./ADR-0010-contract-first-openapi-json-schema.md)
- [Classroom Session Feature](../../01_features/classroom-session.md)
- [Data Retention, History, Export, and Deletion Policy](../../06_delivery/DATA_RETENTION_POLICY.md)
- [Threat Model](../../04_engineering/THREAT_MODEL.md)

