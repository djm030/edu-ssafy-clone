# API Catalog

## 우선순위 표기
- P1: 핵심 read-only 웹앱
- P2: 운영 목록과 보조 기능
- P3: 상세/응답/작성
- P4: 수정/제출/관리

## Auth/Profile
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | POST | `/api/auth/login` | 세션 로그인 |
| P1 | GET | `/api/me` | 현재 사용자 |
| P1 | GET | `/api/auth/session` | 현재 세션 |
| P1 | POST | `/api/auth/logout` | 로그아웃 |
| P1 | GET | `/api/auth/access-policy` | 권한 정책 매트릭스 |
| P2 | POST | `/api/profile/password-check` | 회원정보 재인증 |
| P3 | GET | `/api/profile` | 회원정보 조회 |
| P4 | PUT | `/api/profile` | 회원정보 수정 |
| P4 | POST | `/api/profile/password-change` | 비밀번호 변경 |

## Dashboard/MyCampus
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | GET | `/api/dashboard/summary` | 메인 위젯 통합 조회 |
| P1 | GET | `/api/attendance/records` | 출석 현황 |
| P3 | GET | `/api/attendance/appeals` | 출석 소명 목록 |
| P3 | GET | `/api/attendance/appeals/pending` | staff 소명 처리 목록 |
| P2 | GET | `/api/notifications` | 알림함 |
| P3 | POST | `/api/notifications/{id}/read` | 알림 읽음 |
| P3 | POST | `/api/notifications/read-all` | 알림 전체 읽음 |
| P4 | DELETE | `/api/notifications/{id}` | 알림 삭제 |
| P4 | POST | `/api/attendance/appeals` | 출석 소명 제출 |
| P4 | POST | `/api/attendance/appeals/{id}/cancel` | 출석 소명 취소 |
| P4 | POST | `/api/attendance/appeals/{id}/resolve` | 출석 소명 처리 |

## Learning
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P2 | GET | `/api/learning/curriculum` | 주차별 커리큘럼 |
| P2 | GET | `/api/learning/replays` | 강의 다시보기 |
| P1 | GET | `/api/learning/materials` | 학습자료 목록 |
| P3 | GET | `/api/learning/materials/{id}` | 학습자료 상세 |
| P3 | GET | `/api/learning/materials/{id}/resources` | eBook/PDF/resource 목록 |
| P4 | POST | `/api/learning/materials/{id}/reactions` | 학습자료 좋아요/북마크 |
| P4 | DELETE | `/api/learning/materials/{id}/reactions/{reactionType}` | 학습자료 반응 취소 |
| P4 | POST | `/api/learning/materials/{id}/resources/{resourceId}/attachments` | 학습자료 리소스 첨부 |
| P4 | GET | `/api/learning/materials/{id}/resources/{resourceId}/attachments/{attachmentId}` | 학습자료 첨부 다운로드 |

## Quest/Survey
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | GET | `/api/quests` | Quest/평가 목록 |
| P3 | GET | `/api/quests/{id}` | Quest 상세 |
| P3 | GET | `/api/quests/{id}/submissions/current` | 현재 제출/결과 |
| P4 | POST | `/api/quests/{id}/submissions` | Quest 제출 |
| P4 | POST | `/api/quests/{id}/submissions/{submissionId}/attachments` | Quest 제출 첨부 |
| P4 | GET | `/api/quests/{id}/submissions/{submissionId}/attachments/{attachmentId}` | Quest 첨부 다운로드 |
| P1 | GET | `/api/surveys` | 설문 목록 |
| P3 | POST | `/api/surveys` | 설문 생성 |
| P3 | GET | `/api/surveys/{id}` | 설문 문항 |
| P3 | PUT | `/api/surveys/{id}` | 설문 수정 |
| P3 | DELETE | `/api/surveys/{id}` | 설문 삭제 |
| P3 | GET | `/api/surveys/{id}/responses/current` | 현재 설문 응답 |
| P4 | POST | `/api/surveys/{id}/responses` | 설문 응답 |

## Board/Help Desk
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | GET | `/api/boards/{boardCode}/categories` | 게시판 카테고리 |
| P1 | GET | `/api/boards/{boardCode}/posts` | 게시글 목록 |
| P3 | GET | `/api/boards/{boardCode}/posts/{postId}` | 게시글 상세 |
| P4 | POST | `/api/boards/{boardCode}/posts` | 게시글 작성 |
| P4 | PUT | `/api/boards/{boardCode}/posts/{postId}` | 게시글 수정 |
| P4 | DELETE | `/api/boards/{boardCode}/posts/{postId}` | 게시글 삭제 |
| P4 | POST | `/api/boards/{boardCode}/posts/{postId}/comments` | 댓글 작성 |
| P4 | PUT | `/api/boards/{boardCode}/posts/{postId}/comments/{commentId}` | 댓글 수정 |
| P4 | DELETE | `/api/boards/{boardCode}/posts/{postId}/comments/{commentId}` | 댓글 삭제 |
| P4 | POST | `/api/boards/{boardCode}/posts/{postId}/attachments` | 게시글 첨부 |
| P4 | GET | `/api/boards/{boardCode}/posts/{postId}/attachments/{attachmentId}` | 게시글 첨부 다운로드 |
| P4 | POST | `/api/boards/{boardCode}/posts/{postId}/reactions` | 추천/찜 |
| P4 | DELETE | `/api/boards/{boardCode}/posts/{postId}/reactions/{reactionType}` | 추천/찜 취소 |
| P2 | GET | `/api/support/tickets` | 1:1 문의 목록 |
| P3 | GET | `/api/support/tickets/{id}` | 1:1 문의 상세 |
| P3 | POST | `/api/support/tickets` | 문의 등록 |
| P4 | POST | `/api/support/tickets/{id}/messages` | 문의 메시지 등록 |
| P4 | POST | `/api/support/tickets/{id}/answer` | staff 문의 답변 |
| P4 | POST | `/api/support/tickets/{id}/attachments` | 문의 첨부 |
| P4 | GET | `/api/support/tickets/{id}/attachments/{attachmentId}` | 문의 첨부 다운로드 |

## Community
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P2 | GET | `/api/community/classmates` | 우리반 보기 |
| P3 | POST | `/api/community/classmates/{userId}/notifications` | 학생 알림 보내기 |

## 공통 규칙
- 목록 API는 `page`, `size`, `keyword`를 기본으로 받는다.
- 오류는 `{ "error": { "code": "...", "message": "..." } }`로 통일한다.
- P1/P2 API는 read path 안정성을 우선하고, P3/P4는 validation, 권한, 저장/삭제 정책까지 검증한다.
- 전체 route/method의 최신 기계 판정은 `docs/openapi.json`을 기준으로 확인한다.
