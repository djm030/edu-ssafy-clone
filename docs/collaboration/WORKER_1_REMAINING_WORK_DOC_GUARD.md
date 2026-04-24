# Worker 1 Remaining Work Document Guard

Date: 2026-04-24
Worker: worker-1
Task: 71 - `docs/remaining-work.md` 작성 기준

## Required Sections

Task 71 requires `docs/remaining-work.md` to include:

- 아직 PASS가 아닌 항목
- PARTIAL 항목
- FAIL 항목
- UNKNOWN 항목
- 다음에 생성해야 할 task
- 위험 요소
- known issue
- 완료 판단

## Evidence

Command used:

```bash
grep -nE '^### (아직 PASS가 아닌 항목|PARTIAL 항목|FAIL 항목|UNKNOWN 항목|다음에 생성해야 할 task|위험 요소 / known issue|완료 판단)' docs/remaining-work.md
```

Observed section anchors:

| Section | Line |
|---|---:|
| 아직 PASS가 아닌 항목 | 86 |
| PARTIAL 항목 | 90 |
| FAIL 항목 | 93 |
| UNKNOWN 항목 | 97 |
| 다음에 생성해야 할 task | 102 |
| 위험 요소 / known issue | 111 |
| 완료 판단 | 116 |

## Decision

`docs/remaining-work.md` satisfies the required structure. It still lists required work and therefore correctly blocks final completion.
