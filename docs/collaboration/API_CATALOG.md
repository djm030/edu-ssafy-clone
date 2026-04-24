# API Catalog

## 우선순위 표기
- P1: 핵심 read-only 웹앱
- P2: 운영 목록과 보조 기능
- P3: 상세/응답/작성
- P4: 수정/제출/관리

## Auth/Profile
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | POST | `/api/auth/login` | 데모 로그인 |
| P1 | GET | `/api/me` | 현재 사용자 |
| P2 | POST | `/api/profile/password-check` | 회원정보 재인증 |
| P3 | GET | `/api/profile` | 회원정보 조회 |
| P4 | PUT | `/api/profile` | 회원정보 수정 |

## Dashboard/MyCampus
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | GET | `/api/dashboard/summary` | 메인 위젯 통합 조회 |
| P1 | GET | `/api/attendance/records` | 출석 현황 |
| P2 | GET | `/api/notifications` | 알림함 |
| P4 | POST | `/api/attendance/appeals` | 출석 소명 제출 |

## Learning
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P2 | GET | `/api/learning/curriculum` | 주차별 커리큘럼 |
| P2 | GET | `/api/learning/replays` | 강의 다시보기 |
| P1 | GET | `/api/learning/materials` | 학습자료 목록 |
| P3 | GET | `/api/learning/materials/{id}` | 학습자료 상세 |
| P3 | GET | `/api/learning/materials/{id}/resources` | eBook/PDF/resource 목록 |

## Quest/Survey
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | GET | `/api/quests` | Quest/평가 목록 |
| P3 | GET | `/api/quests/{id}` | Quest 상세 |
| P4 | POST | `/api/quests/{id}/submissions` | Quest 제출 |
| P1 | GET | `/api/surveys` | 설문 목록 |
| P3 | GET | `/api/surveys/{id}` | 설문 문항 |
| P4 | POST | `/api/surveys/{id}/responses` | 설문 응답 |

## Board/Help Desk
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P1 | GET | `/api/boards/{boardCode}/categories` | 게시판 카테고리 |
| P1 | GET | `/api/boards/{boardCode}/posts` | 게시글 목록 |
| P3 | GET | `/api/boards/{boardCode}/posts/{postId}` | 게시글 상세 |
| P4 | POST | `/api/boards/{boardCode}/posts` | 게시글 작성 |
| P4 | POST | `/api/boards/{boardCode}/posts/{postId}/comments` | 댓글 작성 |
| P4 | POST | `/api/boards/{boardCode}/posts/{postId}/reactions` | 추천/찜 |
| P2 | GET | `/api/support/tickets` | 1:1 문의 목록 |
| P3 | POST | `/api/support/tickets` | 문의 등록 |

## Community
| Priority | Method | Path | 설명 |
|---|---|---|---|
| P2 | GET | `/api/community/classmates` | 우리반 보기 |
| P3 | POST | `/api/community/classmates/{userId}/notifications` | 학생 알림 보내기 |

## 공통 규칙
- 목록 API는 `page`, `size`, `keyword`를 기본으로 받는다.
- 오류는 `{ "error": { "code": "...", "message": "..." } }`로 통일한다.
- P1/P2 API는 read path 안정성을 우선하고, P3/P4부터 validation과 쓰기 정책을 강화한다.

