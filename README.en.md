# Penatika

> [🇮🇩 Bahasa Indonesia](./README.md) · 🇬🇧 English

**Penatika** is a teaching copilot that helps Indonesian Mathematics teachers prepare, present, and adapt lessons naturally while class is in progress.

> **Speak. Write. Teach. AI follows.**

The teacher remains in control. AI produces structured content proposals in the background; it is neither an autonomous teacher nor a generic chatbot that takes over the teaching flow.

## Product Context

Penatika initially serves Indonesian Mathematics teachers for Grade 4 elementary school through Grade 9 junior high school. Teachers use a laptop or PC connected to a classroom display and may use a smartphone as a private teaching controller.

Penatika separates:

- the **teacher-private surface** for control, AI progress, suggestions, and validation details;
- the **classroom display** for content appropriate for students;
- **backend-authoritative session state** for cross-device synchronization.

Student devices are not required for the core MVP.

## MVP Baseline

The MVP establishes one teacher-to-classroom workflow:

1. generate a curriculum-aware lesson draft with AI assistance;
2. review and edit the lesson before class;
3. start a cloud classroom session;
4. pair the smartphone and classroom display without screen mirroring or shared-Wi-Fi dependence;
5. teach from a structured classroom canvas;
6. use mouse, touch, stylus, and digital ink;
7. request live adaptations to examples, questions, explanations, or visuals;
8. apply deterministic Mathematics validation where available;
9. connect curriculum claims to a controlled source and version;
10. end and save the teaching session.

Initial validation work prioritizes Grade 5 fractions and Grade 7 basic algebra or linear equations.

## MVP Non-Goals

- Autonomous AI teaching.
- Student-device participation.
- PDF or PowerPoint import.
- Attendance or school administration.
- Advanced graphing or 3D visualization.
- Proprietary smart-board dependence.
- Subjects other than Mathematics.

## Project Shape

| Concern | Decision |
|---|---|
| Base profile | `fullstack` |
| Modifier | `ai-enabled` |
| Initial backend shape | Modular monolith with explicit domain modules |
| Classroom state | Backend-authoritative and revision-aware |
| Classroom content | Versioned structured model; no arbitrary generated HTML |
| AI trust | Untrusted proposal generator; not an authority |
| Mathematics trust | Deterministic validation for supported scope |
| Curriculum trust | BSKAP 046/H/KR/2025 as the Mathematics normative authority; official guidance is interpretive and local context is an overlay |
| Implementation stack | Not decided |
| Deployment target | Not decided |

Material decisions are recorded in [`docs/02_architecture/adr/`](./docs/02_architecture/adr/).

## Current Status

Project Discovery and documentation initialization are complete. The repository does not yet contain application source code, package manifests, database migrations, contract schemas, or deployment configuration.

The next step is to resolve blocking product and architecture decisions and define field-level contracts before source scaffolding.

## Documentation Map

### Product

- [Product Brief](./docs/00_product/PRODUCT_BRIEF.md)
- [PRD](./docs/00_product/PRD.md)
- [Roadmap](./docs/00_product/ROADMAP.md)

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

Deployment, Runbook, and Release Checklist remain conditional until deployment maturity is sufficient.

## Repository Rules

`AGENTS.md` is the repository-wide instruction source for AI coding agents and contributors. Before implementation:

1. read the relevant PRD capability;
2. read the relevant feature specification;
3. read the System Architecture and relevant ADRs;
4. create or update contracts when a cross-component boundary changes;
5. read the applicable engineering standards;
6. implement the smallest coherent change with evidence.

Do not invent requirements, silently change architecture, commit secrets, or treat AI output as authoritative.

## Validate Documentation

```bash
python3 scripts/validate_template.py --project-mode
```

This validator checks local Markdown links and unresolved core metadata placeholders. It does not prove product, architecture, security, or application correctness.

## Open Decisions Before Implementation

- Curriculum ingestion, provenance contracts, local-context modeling, and official-guidance usage/licensing.
- Approval policy for live AI adaptation.
- Minimum degraded-mode capability.
- Client and backend technologies.
- Database and migration tooling.
- Identity and authentication model.
- Realtime transport and reconnect protocol.
- AI and speech provider strategy.
- Mathematics validator approach.
- Field-level structured contracts.
- Deployment environment and secret management.
- Product owner and requirements approver.

See the Product Brief, PRD, System Architecture, Risks, and Known Limitations for the complete decision record and timing.
