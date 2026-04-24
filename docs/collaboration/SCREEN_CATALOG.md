# Screen Catalog

## 공통
| Priority | Route | 화면 |
|---|---|---|
| P1 | `/login` | 로그인 |
| P1 | `/` | 메인 대시보드 |
| P1 | shell | 글로벌 메뉴, 사용자 요약, 모바일 대응 |

## MyCampus
| Priority | Route | 화면 |
|---|---|---|
| P1 | `/mycampus/attendance` | 출석현황 |
| P1 | `/mycampus/level` | 레벨/경험치/장학포인트 |
| P2 | `/mycampus/notifications` | 알림함 |
| P4 | `/mycampus/attendance/appeals/new` | 출석 소명 작성 |

## Learning
| Priority | Route | 화면 |
|---|---|---|
| P2 | `/learning/curriculum` | 주차별 커리큘럼 |
| P2 | `/learning/replays` | 강의 다시보기 |
| P1 | `/learning/materials` | 학습자료 목록 |
| P3 | `/learning/materials/:id` | 학습자료 상세 |
| P3 | `/learning/materials/:id/viewer` | PDF/eBook popup 대체 화면 |

## Quest/Survey
| Priority | Route | 화면 |
|---|---|---|
| P1 | `/quest` | Quest/평가 목록 |
| P3 | `/quest/:id` | Quest 상세 |
| P4 | `/quest/:id/submit` | Quest 제출 |
| P1 | `/survey` | 설문 목록 |
| P3 | `/survey/:id` | 설문 상세 |
| P4 | `/survey/:id/respond` | 설문 응답 |

## Community/Help
| Priority | Route | 화면 |
|---|---|---|
| P1 | `/community/free` | 자유게시판 목록 |
| P3 | `/community/free/:postId` | 자유게시판 상세 |
| P4 | `/community/free/write` | 게시글 작성 |
| P2 | `/community/classmates` | 우리반 보기 |
| P1 | `/help/notice` | 공지사항 목록 |
| P3 | `/help/notice/:postId` | 공지 상세 |
| P2 | `/help/faq` | FAQ |
| P2 | `/help/qna` | 1:1 문의 목록 |
| P3 | `/help/qna/new` | 문의 등록 |

## Profile
| Priority | Route | 화면 |
|---|---|---|
| P2 | `/profile/check` | 비밀번호 재확인 |
| P3 | `/profile/edit` | 회원정보 수정 |

## 화면 완료 조건
- 모든 route는 직접 URL 진입이 가능해야 한다.
- loading, empty, error, success 상태가 있어야 한다.
- 목록 화면은 검색/필터/페이지 전환이 URL 또는 내부 상태와 일관되어야 한다.
- P3/P4 전에는 placeholder route라도 깨지지 않게 만든다.

