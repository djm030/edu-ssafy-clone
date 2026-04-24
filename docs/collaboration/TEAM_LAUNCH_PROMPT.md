# Team Launch Prompt

## 목적
이 파일은 PM + Backend + Frontend + DevOps-QA 구조로 파일럿 작업을 실행할 때 사용할 시작 프롬프트다.

## 권장 실행 명령
```bash
omx team 3:executor "Execute the board-list pilot using docs/collaboration/TEAM_LAUNCH_PROMPT.md. Preserve the sequence: design notes, implementation plan, code writing, tests, human review package."
```

최종 검증은 별도 Ralph 루프로 닫는다.

```bash
omx ralph "Verify the board-list pilot against docs/collaboration/TEST_SPEC.md and prepare the human review package."
```

## Shared Context
Read first:
- `docs/collaboration/TEAM_HARNESS.md`
- `docs/collaboration/DESIGN_NOTES.md`
- `docs/collaboration/IMPLEMENTATION_PLAN.md`
- `docs/collaboration/API_SPEC_DRAFT.md`
- `docs/collaboration/SCREEN_LIST.md`
- `docs/collaboration/TEST_SPEC.md`

Authoritative schema:
- `docs/revised_schema_mysql8.sql`

Reference requirements:
- `docs/REQUIREMENTS.md`
- `docs/FUNCTIONAL_SPEC.md`
- `docs/ERD.md`

Pilot feature:
- Notice list: `FR-NOTI-001`, `FS-NOTI-001`
- Free-board list: `FR-COMM-001`, `FS-COMM-001`
- Board metadata: `FR-COMM-002`, `FS-COMM-002`

## Global Rules
- Do not skip from design notes directly to code.
- Do not implement outside the approved pilot scope.
- Do not introduce a separate `notices` table.
- Treat legacy `.do` URLs as reference evidence only.
- Use `/api/boards/{boardCode}/categories` and `/api/boards/{boardCode}/posts` as the draft API contract.
- Use `/help/notice` and `/community/free` as the draft frontend routes.
- If the app framework is missing, stop at scaffold proposal plus exact file plan instead of inventing an unapproved stack.
- Record test gaps honestly.

## Lane Assignment
### Backend Agent
Read:
- `docs/lanes/BACKEND_LANE.md`

Own:
- Backend/API/schema integration work only.

Main tasks:
- Validate whether current project has a backend scaffold.
- If scaffold exists, implement the board-list API contract.
- If scaffold does not exist, produce a backend scaffold proposal with exact files and commands, but do not choose a stack without PM approval.
- Support MySQL 8 schema execution verification.
- Prepare API tests or test placeholders mapped to `TEST_SPEC.md`.

### Frontend Agent
Read:
- `docs/lanes/FRONTEND_LANE.md`

Own:
- Frontend route/screen/UI-state work only.

Main tasks:
- Validate whether current project has a frontend scaffold.
- If scaffold exists, implement `/help/notice` and `/community/free`.
- If scaffold does not exist, produce a frontend scaffold proposal with exact files and commands, but do not choose a stack without PM approval.
- Use the API draft response shape.
- Prepare smoke/visual test points.

### DevOps-QA Agent
Read:
- `docs/lanes/DEVOPS_QA_LANE.md`

Own:
- DB/runtime/test harness verification only.

Main tasks:
- Check whether MySQL/Docker/test commands already exist.
- Attempt non-destructive schema execution verification only when a safe local MySQL 8 or approved Docker harness exists.
- If no harness exists, write the exact proposed commands and files needed.
- Prepare evidence template and test command map.

## PM Completion Package
Before human review, PM must collect:
- Changed files
- Implemented scope
- Deferred scope
- Test commands run
- Test result evidence
- Known risks
- Contract changes made during implementation

## Stop Conditions
- The repository has no app scaffold and stack choice is required.
- MySQL 8 execution requires installing/running services without approval.
- API contract and screen contract conflict.
- Any lane needs to edit a file outside its ownership boundary.
