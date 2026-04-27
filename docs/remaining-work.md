# Remaining Work

Date: 2026-04-27 KST

## Current Readiness

The requested local full-stack clone gates are **PASS**. The current readiness source of truth is `docs/final-verification.md`.

Verified PASS areas include backend tests, frontend build/lint, Docker Compose config, app-profile Docker image rebuild/startup, Nginx/backend smoke, session-backed auth, major domain APIs, attachment paths, role/error handling, Swagger/OpenAPI runtime docs, and SPA route smoke.

## Active Work Source

`docs/next_plan.md` is the active backlog for UI/UX parity and production polish. Continue implementation from that document.

## Remaining Follow-Ups

| Area | Status | Next Work |
|---|---:|---|
| Browser visual baseline | TODO | Add Playwright/Cypress or equivalent screenshot and interaction checks for priority routes. |
| UI/UX parity | IN_PROGRESS | Follow `docs/next_plan.md` for home dashboard density, menus, tables, filters, badges, and state fidelity. |
| External deployment evidence | TODO | Re-run the same Compose, smoke, backend, and frontend gates in each deployment target. |
| Long-term DB migration discipline | TODO | Avoid relying on reused local volumes; document fresh-volume and migration/seed refresh procedure. |
| OpenAPI examples | TODO | Enrich generated schema descriptions/examples and authenticated operation examples beyond route/method coverage. |

## Non-Goals

- Do not resurrect deleted worker/team prompt files as project guidance.
- Do not use historical OMX runtime state as completion evidence.
- Do not downgrade the PASS decision without fresh failing verification evidence.

## Completion Rule

For new work, update this file only when a follow-up is added, completed, or reclassified. Verification evidence belongs in `docs/test-report.md`; final release readiness belongs in `docs/final-verification.md`.
