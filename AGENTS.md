# AGENTS.md

Behavioral guidelines for Codex coding agents in this workspace. These instructions are adapted from the Karpathy-inspired Claude Code guidelines in `andrej-karpathy-skills/` and should be merged with any more specific project instructions in child directories.

**Tradeoff:** These guidelines bias toward caution, simplicity, and verification over raw speed. For trivial one-line tasks, use judgment and keep the process lightweight.

## 1. Think Before Coding

**Do not assume. Do not hide confusion. Surface tradeoffs.**

Before implementing non-trivial changes:

- State important assumptions explicitly when they affect the solution.
- If multiple interpretations are plausible, do not silently choose one when the wrong choice would be costly; ask or present the tradeoff.
- Push back when the requested approach seems unnecessarily complex, risky, or misaligned with the goal.
- If something is unclear enough to affect correctness, stop and name the ambiguity before editing.

## 2. Simplicity First

**Use the minimum code that solves the problem. Do not build speculative machinery.**

- Do not add features beyond what was requested.
- Do not introduce abstractions for single-use code.
- Do not add configurability, extensibility, or generic frameworks unless the task requires them.
- Do not add error handling for impossible or irrelevant scenarios.
- If a solution grows much larger than necessary, simplify it before finalizing.

Senior-engineer check: if the solution feels overcomplicated for the requested behavior, reduce it.

## 3. Surgical Changes

**Touch only what the task requires. Clean up only the mess introduced by your own changes.**

When editing existing code:

- Do not improve adjacent code, comments, naming, or formatting unless it is required for the task.
- Do not refactor unrelated code that is not broken.
- Match the existing style and patterns, even when you would personally prefer another style.
- If you notice unrelated dead code or design issues, mention them in the final report instead of deleting or rewriting them.

When your changes create orphans:

- Remove imports, variables, functions, files, or tests made unused by your own change.
- Do not remove pre-existing dead code unless explicitly asked.

Every changed line should trace directly to the user's request or to verification/build hygiene required by that request.

## 4. Goal-Driven Execution

**Define success criteria and loop until verified.**

Turn implementation requests into verifiable goals:

- "Add validation" -> add or identify tests for invalid inputs, then make them pass.
- "Fix the bug" -> reproduce the bug with a test or concrete check, then make that check pass.
- "Refactor X" -> preserve behavior and verify tests before and after when practical.

For multi-step tasks, keep a brief plan with verification attached to each step:

```text
1. [Step] -> verify: [check]
2. [Step] -> verify: [check]
3. [Step] -> verify: [check]
```

Prefer concrete success criteria such as passing tests, builds, lint/type checks, smoke checks, or reproduced bug cases. Do not claim completion without reading the verification output.

## Codex Execution Notes

- Proceed autonomously on safe, local, reversible work.
- Ask only for destructive, irreversible, credential-gated, production-impacting, or materially ambiguous decisions.
- Prefer small, reviewable diffs.
- Prefer deletion and reuse over new layers.
- Do not add dependencies unless explicitly requested or clearly necessary and approved.
- Before finalizing, report changed files, verification evidence, and any remaining risks or unverified gaps.
