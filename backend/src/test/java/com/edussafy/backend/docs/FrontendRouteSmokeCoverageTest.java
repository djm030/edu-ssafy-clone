package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FrontendRouteSmokeCoverageTest {

    private static final Path ROUTES_TS = Path.of("..", "frontend", "src", "routes.ts");
    private static final Path APP_TSX = Path.of("..", "frontend", "src", "App.tsx");
    private static final Path CLIENT_TS = Path.of("..", "frontend", "src", "api", "client.ts");
    private static final Path OPS_READINESS_TSX = Path.of("..", "frontend", "src", "pages", "OpsReadinessPage.tsx");

    private static final List<String> REQUIRED_SCREEN_ROUTES = List.of(
            "/",
            "/login",
            "/profile/check",
            "/profile/edit",
            "/mycampus/attendance",
            "/mycampus/attendance/appeals/new",
            "/mycampus/elearning",
            "/mycampus/elearning/1",
            "/mycampus/bookmarks",
            "/mycampus/documents",
            "/mycampus/documents/1",
            "/mycampus/pledges",
            "/mycampus/pledges/1",
            "/mycampus/ebooks",
            "/mycampus/ebooks/1",
            "/community/free",
            "/community/free/1",
            "/community/free/write",
            "/community/anonymous",
            "/community/anonymous/1",
            "/community/anonymous/write",
            "/survey",
            "/survey/1",
            "/survey/1/respond",
            "/help/academic-rules",
            "/help/rules",
            "/mentoring/stories",
            "/mentoring/stories/1",
            "/mentoring/questions",
            "/mentoring/questions/new",
            "/mentoring/questions/1",
            "/mentoring/notices",
            "/mentoring/notices/1",
            "/mentoring/meetings",
            "/mentoring/meetings/1",
            "/mentoring/meetings/my-applications",
            "/mentoring/meeting-results",
            "/mentoring/meeting-results/993",
            "/mentoring/meeting-reviews",
            "/mentoring/meeting-reviews/write",
            "/mentoring/meeting-reviews/1301",
            "/help/qna",
            "/help/qna/new",
            "/help/qna/tickets/1",
            "/mycampus/notifications",
            "/learning/live",
            "/learning/curriculum",
            "/learning/curriculum/1",
            "/learning/materials",
            "/learning/materials/1",
            "/learning/materials/1/viewer",
            "/learning/required-studies",
            "/learning/required-studies/1",
            "/learning/replays",
            "/learning/replays/my",
            "/learning/replays/all",
            "/quest",
            "/quest/1",
            "/quest/1/submit",
            "/ops/readiness",
            "/external-services"
    );

    @Test
    void screenSmokeManifestCoversAllPriorityDomainRoutes() throws IOException {
        String routes = Files.readString(ROUTES_TS);

        for (String route : REQUIRED_SCREEN_ROUTES) {
            assertThat(routes).contains("path: '" + route + "'");
        }
        assertThat(routes).contains("priority: 1");
        assertThat(routes).contains("priority: 9");
    }

    @Test
    void appAndReadinessPageConsumeTheSharedRouteManifest() throws IOException {
        String app = Files.readString(APP_TSX);
        String readinessPage = Files.readString(OPS_READINESS_TSX);

        assertThat(app).contains("if (path === '/login')");
        assertThat(app).contains("if (path === '/ops/readiness')");
        assertThat(app).contains("if (path === '/external-services')");
        assertThat(app).contains("if (path === '/mycampus/elearning')");
        assertThat(app).contains("if (path === '/mycampus/bookmarks')");
        assertThat(app).contains("if (path === '/mycampus/documents')");
        assertThat(app).contains("match(/^\\/mycampus\\/documents\\/(\\d+)$/)");
        assertThat(app).contains("if (path === '/mycampus/pledges')");
        assertThat(app).contains("if (path === '/mycampus/ebooks')");
        assertThat(app).contains("if (path === '/learning/required-studies')");
        assertThat(app).contains("if (path === '/learning/live')");
        assertThat(app).contains("if (path === '/learning/replays' || path === '/learning/replays/my')");
        assertThat(app).contains("if (path === '/learning/replays/all')");
        assertThat(app).contains("if (path === '/help/academic-rules' || path === '/help/rules')");
        assertThat(app).contains("if (path === '/mentoring/stories')");
        assertThat(app).contains("match(/^\\/mentoring\\/stories\\/(\\d+)$/)");
        assertThat(app).contains("if (path === '/mentoring/questions')");
        assertThat(app).contains("if (path === '/mentoring/questions/new')");
        assertThat(app).contains("match(/^\\/mentoring\\/questions\\/(\\d+)$/)");
        assertThat(app).contains("if (path === '/mentoring/notices')");
        assertThat(app).contains("match(/^\\/mentoring\\/notices\\/(\\d+)$/)");
        assertThat(app).contains("if (path === '/mentoring/meetings')");
        assertThat(app).contains("if (path === '/mentoring/meetings/my-applications')");
        assertThat(app).contains("match(/^\\/mentoring\\/meetings\\/(\\d+)$/)");
        assertThat(app).contains("if (path === '/mentoring/meeting-results')");
        assertThat(app).contains("if (path === '/mentoring/meeting-reviews')");
        assertThat(app).contains("if (path === '/mentoring/meeting-reviews/write')");
        assertThat(app).contains("match(/^\\/mentoring\\/meeting-results\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/mentoring\\/meeting-reviews\\/(\\d+)$/)");
        assertThat(app).contains("if (path === '/community/anonymous/write' || path === '/community/anonymous/new')");
        assertThat(app).contains("match(/^\\/community\\/anonymous\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/mycampus\\/pledges\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/mycampus\\/ebooks\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/mycampus\\/elearning\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/learning\\/materials\\/(\\d+)\\/viewer$/)");
        assertThat(app).contains("match(/^\\/learning\\/required-studies\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/learning\\/curriculum\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/quest\\/(\\d+)\\/submit$/)");
        assertThat(app).contains("match(/^\\/survey\\/(\\d+)\\/respond$/)");
        assertThat(app).contains("match(/^\\/help\\/qna\\/tickets\\/(\\d+)$/)");
        String boardList = Files.readString(Path.of("..", "frontend", "src", "components", "BoardListPage.tsx"));
        String boardApi = Files.readString(Path.of("..", "frontend", "src", "api", "boards.ts"));
        String boardDetail = Files.readString(Path.of("..", "frontend", "src", "pages", "BoardDetailPage.tsx"));
        assertThat(readinessPage)
                .contains("getAccessPolicy")
                .contains("screenSmokeRoutes.map")
                .contains("accessPolicies.map")
                .contains("권한 정책 매트릭스");
        assertThat(boardApi)
                .contains("/api/help/notices")
                .contains("/api/help/faqs");
        assertThat(boardList)
                .contains("FaqAccordion")
                .contains("FAQ 목록")
                .contains("상세 보기");
        assertThat(app)
                .contains("boardCode=\"notice\" readOnly")
                .contains("boardCode=\"faq\" readOnly");
        assertThat(boardDetail)
                .contains("readOnly?: boolean")
                .contains("읽기 전용")
                .contains("공지/FAQ는 운영자가 관리하는 읽기 전용 콘텐츠입니다.");
    }
    @Test
    void priorityDataPagesExposeLoadingErrorAndEmptyStates() throws IOException {
        Map<String, Path> pages = Map.ofEntries(
                Map.entry("attendance", Path.of("..", "frontend", "src", "pages", "AttendancePage.tsx")),
                Map.entry("level", Path.of("..", "frontend", "src", "pages", "LevelPage.tsx")),
                Map.entry("board", Path.of("..", "frontend", "src", "components", "BoardListPage.tsx")),
                Map.entry("survey", Path.of("..", "frontend", "src", "pages", "SurveyPage.tsx")),
                Map.entry("notifications", Path.of("..", "frontend", "src", "pages", "NotificationsPage.tsx")),
                Map.entry("learning", Path.of("..", "frontend", "src", "pages", "MaterialsPage.tsx")),
                Map.entry("curriculum", Path.of("..", "frontend", "src", "pages", "CurriculumPage.tsx")),
                Map.entry("elearning", Path.of("..", "frontend", "src", "pages", "ElearningPage.tsx")),
                Map.entry("bookmarks", Path.of("..", "frontend", "src", "pages", "BookmarksPage.tsx")),
                Map.entry("documents", Path.of("..", "frontend", "src", "pages", "DocumentsPage.tsx")),
                Map.entry("pledges", Path.of("..", "frontend", "src", "pages", "PledgesPage.tsx")),
                Map.entry("ebooks", Path.of("..", "frontend", "src", "pages", "EbooksPage.tsx")),
                Map.entry("required-studies", Path.of("..", "frontend", "src", "pages", "RequiredStudiesPage.tsx")),
                Map.entry("live-sessions", Path.of("..", "frontend", "src", "pages", "LiveSessionsPage.tsx")),
                Map.entry("replays", Path.of("..", "frontend", "src", "pages", "ReplaysPage.tsx")),
                Map.entry("quest", Path.of("..", "frontend", "src", "pages", "QuestPage.tsx")),
                Map.entry("support", Path.of("..", "frontend", "src", "pages", "QnaListPage.tsx")),
                Map.entry("academic-rules", Path.of("..", "frontend", "src", "pages", "AcademicRulesPage.tsx")),
                Map.entry("mentor-stories", Path.of("..", "frontend", "src", "pages", "MentorStoriesPage.tsx")),
                Map.entry("mentoring-questions", Path.of("..", "frontend", "src", "pages", "MentoringQuestionsPage.tsx")),
                Map.entry("mentoring-notices", Path.of("..", "frontend", "src", "pages", "MentoringNoticesPage.tsx")),
                Map.entry("mentoring-meetings", Path.of("..", "frontend", "src", "pages", "MentoringMeetingsPage.tsx")),
                Map.entry("mentoring-meeting-results", Path.of("..", "frontend", "src", "pages", "MentoringMeetingResultsPage.tsx")),
                Map.entry("external-services", Path.of("..", "frontend", "src", "pages", "ExternalServicesPage.tsx"))
        );

        for (Map.Entry<String, Path> entry : pages.entrySet()) {
            String source = Files.readString(entry.getValue());

            assertThat(source)
                    .as(entry.getKey() + " page imports shared data state component")
                    .contains("DataState")
                    .contains("LoadingRows");
            assertThat(source)
                    .as(entry.getKey() + " page handles error and empty load states")
                    .contains("loadState === 'error'")
                    .contains("loadState === 'empty'");
        }
    }


    @Test
    void liveSessionsPageExposesJoinAndDisabledStates() throws IOException {
        String livePage = Files.readString(Path.of("..", "frontend", "src", "pages", "LiveSessionsPage.tsx"));
        String appApi = Files.readString(Path.of("..", "frontend", "src", "api", "app.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(livePage)
                .contains("joinEnabled")
                .contains("disabledReason")
                .contains("actionLabel")
                .contains("Meeting 입장 기록");
        assertThat(appApi)
                .contains("hasLaunchableLiveUrl")
                .contains("#none;")
                .contains("입장 대기");
        assertThat(types)
                .contains("joinEnabled: boolean")
                .contains("disabledReason?: string | null");
    }


    @Test
    void ebooksPageExposesAccessAndDisabledStates() throws IOException {
        String ebooksPage = Files.readString(Path.of("..", "frontend", "src", "pages", "EbooksPage.tsx"));
        String dashboard = Files.readString(Path.of("..", "frontend", "src", "pages", "DashboardPage.tsx"));
        String appApi = Files.readString(Path.of("..", "frontend", "src", "api", "app.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(ebooksPage)
                .contains("accessEnabled")
                .contains("disabledReason")
                .contains("actionLabel")
                .contains("이 계정으로는 e-book을 열람할 수 없습니다.");
        assertThat(dashboard)
                .contains("accessEnabled === false")
                .contains("dashboard-widget-card")
                .contains("e-book 열람");
        assertThat(appApi)
                .contains("hasLaunchableEbookUrl")
                .contains("#none;")
                .contains("권한 없음");
        assertThat(types)
                .contains("accessEnabled: boolean")
                .contains("disabledReason?: string | null");
    }

    @Test
    void anonymousBoardDetailExposesSafetyAndReportStates() throws IOException {
        String boardDetail = Files.readString(Path.of("..", "frontend", "src", "pages", "BoardDetailPage.tsx"));
        String boardApi = Files.readString(Path.of("..", "frontend", "src", "api", "boards.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(boardDetail)
                .contains("AnonymousSafetyPanel")
                .contains("익명 게시글 신고가 접수되었습니다.")
                .contains("nextReportedSafety")
                .contains("createReaction(boardCode, post.id, 'report')")
                .contains("신고 {safety.reportCount.toLocaleString('ko-KR')}");
        assertThat(boardApi)
                .contains("type: 'bookmark' | 'like' | 'report'");
        assertThat(types)
                .contains("BoardSafetySummary")
                .contains("reportCount: number")
                .contains("reportable: boolean");
    }

    @Test
    void academicRulesPageExposesAnchorsAndSearchMetadata() throws IOException {
        String academicRulesPage = Files.readString(Path.of("..", "frontend", "src", "pages", "AcademicRulesPage.tsx"));
        String appApi = Files.readString(Path.of("..", "frontend", "src", "api", "app.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(academicRulesPage)
                .contains("학사규정 바로가기")
                .contains("검색 초기화")
                .contains("rule.anchorId")
                .contains("searchMatched")
                .contains("규정 링크");
        assertThat(appApi)
                .contains("totalRuleCount")
                .contains("detailPath: rule.detailPath || `/help/academic-rules#rule-${rule.id}`");
        assertThat(types)
                .contains("anchorId?: string")
                .contains("detailPath?: string")
                .contains("searchMatched?: boolean");
    }

    @Test
    void externalServicesPageExposesLaunchPolicies() throws IOException {
        String externalPage = Files.readString(Path.of("..", "frontend", "src", "pages", "ExternalServicesPage.tsx"));
        String appApi = Files.readString(Path.of("..", "frontend", "src", "api", "app.ts"));
        String mockData = Files.readString(Path.of("..", "frontend", "src", "data", "mockData.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(externalPage)
                .contains("isLaunchable")
                .contains("policyLabel")
                .contains("disabledReason")
                .contains("SSO Launch")
                .contains("새 창 열기");
        assertThat(appApi)
                .contains("openInNewWindow")
                .contains("mockExternalServices.find");
        assertThat(mockData)
                .contains("launchType: 'SSO_FORM'")
                .contains("launchable: false")
                .contains("disabledReason");
        assertThat(types)
                .contains("launchable?: boolean")
                .contains("policyLabel?: string")
                .contains("openInNewWindow?: boolean");
    }



    @Test
    void attendancePageExposesMonthlyCalendarMatrix() throws IOException {
        String attendancePage = Files.readString(Path.of("..", "frontend", "src", "pages", "AttendancePage.tsx"));
        String appApi = Files.readString(Path.of("..", "frontend", "src", "api", "app.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(attendancePage)
                .contains("MonthlyAttendancePanel")
                .contains("월간 출석현황")
                .contains("attendance-month-layout")
                .contains("attendance-month-grid")
                .contains("aria-pressed")
                .contains("AttendanceDayDetailPanel")
                .contains("선택한 일자 출석 상세")
                .contains("소명 신청 가능")
                .contains("기록 대기");
        assertThat(appApi)
                .contains("buildAttendanceMonthSummary")
                .contains("month: response.month");
        assertThat(types)
                .contains("AttendanceMonthSummary")
                .contains("AttendanceMonthDay");
    }


    @Test
    void levelPageExposesTierRoadmap() throws IOException {
        String levelPage = Files.readString(Path.of("..", "frontend", "src", "pages", "LevelPage.tsx"));

        assertThat(levelPage)
                .contains("레벨&장학포인트 현황")
                .contains("level-hero-panel")
                .contains("level-ring")
                .contains("level-kpi-row")
                .contains("포인트 사유")
                .contains("point-reason-badge")
                .contains("Bronze/Silver 단계")
                .contains("tier-roadmap")
                .contains("tier-step-index")
                .contains("최근 레벨 변동")
                .contains("level-trend-panel")
                .contains("LevelTierItem")
                .contains("progressPercent")
                .contains("visualState")
                .contains("scholarshipLabel");
    }

    @Test
    void bookmarksPageExposesTypeSummaryAndOptimisticDeleteState() throws IOException {
        String bookmarksPage = Files.readString(Path.of("..", "frontend", "src", "pages", "BookmarksPage.tsx"));
        String appApi = Files.readString(Path.of("..", "frontend", "src", "api", "app.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(bookmarksPage)
                .contains("BookmarkSummaryPanel")
                .contains("찜한 목록 유형별 요약")
                .contains("BookmarkDeleteConfirm")
                .contains("bookmark-confirm-panel")
                .contains("찜 해제 확인")
                .contains("실패하면 기존 목록을 유지")
                .contains("decrementBookmarkSummary")
                .contains("summaryCount")
                .contains("confirmTarget")
                .contains("deletingId")
                .contains("해제 중");
        assertThat(appApi)
                .contains("BookmarksResponse")
                .contains("materialCount");
        assertThat(types)
                .contains("BookmarkSummary")
                .contains("BookmarksResponse");
    }

    @Test
    void documentsPageExposesSubmissionHistoryDetailRoute() throws IOException {
        String app = Files.readString(APP_TSX);
        String routes = Files.readString(ROUTES_TS);
        String documentsPage = Files.readString(Path.of("..", "frontend", "src", "pages", "DocumentsPage.tsx"));

        assertThat(routes)
                .contains("id: 'document-detail'")
                .contains("path: '/mycampus/documents/1'");
        assertThat(app)
                .contains("documentMatch")
                .contains("<DocumentsPage requestId={Number(documentMatch[1])} />");
        assertThat(documentsPage)
                .contains("getDocumentRequest")
                .contains("DocumentDetailPanel")
                .contains("제출 상태 이력")
                .contains("제출 파일 이력")
                .contains("보완 요청/검토 의견")
                .contains("deadlineLabel")
                .contains("pendingDocumentAction")
                .contains("제출 중")
                .contains("취소 중");
    }

    @Test
    void pledgesPageExposesOriginalTextAndAgreementEvidence() throws IOException {
        String pledgesPage = Files.readString(Path.of("..", "frontend", "src", "pages", "PledgesPage.tsx"));

        assertThat(pledgesPage)
                .contains("PledgeDetailView")
                .contains("서약서 원문 재열람")
                .contains("PledgeAgreementEvidencePanel")
                .contains("동의 이력")
                .contains("동의 버전 스냅샷")
                .contains("versionSnapshot")
                .contains("excerpt(item.content)");
    }

    @Test
    void educationStatusPageExposesSemesterTrackAndAchievementMetrics() throws IOException {
        String educationStatusPage = Files.readString(Path.of("..", "frontend", "src", "pages", "EducationStatusPage.tsx"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(types)
                .contains("semesterLabel")
                .contains("cohortName")
                .contains("trackName");
        assertThat(educationStatusPage)
                .contains("EducationProfilePanel")
                .contains("교육현황 학기 및 트랙 요약")
                .contains("SEMESTER / TRACK")
                .contains("출석률")
                .contains("필수학습 이수")
                .contains("MetricBadge");
    }


    @Test
    void elearningPageExposesOperationalSummaryAndMeta() throws IOException {
        String elearningPage = Files.readString(Path.of("..", "frontend", "src", "pages", "ElearningPage.tsx"));
        String appApi = Files.readString(Path.of("..", "frontend", "src", "api", "app.ts"));
        String types = Files.readString(Path.of("..", "frontend", "src", "types.ts"));

        assertThat(elearningPage)
                .contains("ElearningSummaryPanel")
                .contains("학습중 이러닝 운영 요약")
                .contains("elearning-meta-row")
                .contains("남은 차시")
                .contains("resumingCourseId")
                .contains("이어보기 준비 중")
                .contains("외부 플레이어 준비중")
                .contains("다시 불러오기")
                .contains("setRetryToken");
        assertThat(appApi)
                .contains("ElearningProgressResponse")
                .contains("remainingLessonCount");
        assertThat(types)
                .contains("ElearningProgressSummary")
                .contains("ElearningProgressResponse");
    }


    @Test
    void appShellExposesEduSsafyGlobalNavigation() throws IOException {
        String appShell = Files.readString(Path.of("..", "frontend", "src", "components", "AppShell.tsx"));
        String styles = Files.readString(Path.of("..", "frontend", "src", "styles.css"));
        String responsive = Files.readString(Path.of("..", "frontend", "src", "responsive.css"));

        assertThat(appShell)
                .contains("EduSSAFY 상단 대메뉴")
                .contains("mega-menu-panel")
                .contains("global-mobile-menu")
                .contains("aria-expanded")
                .contains("aria-controls")
                .contains("onMouseEnter")
                .contains("onFocus")
                .contains("세션")
                .contains("알림함")
                .contains("외부 서비스")
                .contains("회원정보")
                .contains("로그아웃")
                .contains("전체메뉴");
        assertThat(styles)
                .contains(".global-header")
                .contains(".global-nav__item.active")
                .contains(".mega-menu-panel__links")
                .contains(".mega-menu-link.active");
        assertThat(responsive)
                .contains(".global-mobile-menu.open")
                .contains(".global-menu-toggle");
    }

    @Test
    void dashboardPageExposesEduSsafyHomeWidgets() throws IOException {
        String dashboard = Files.readString(Path.of("..", "frontend", "src", "pages", "DashboardPage.tsx"));

        assertThat(dashboard)
                .contains("출석체크 & 현황")
                .contains("submitAttendanceCheck")
                .contains("입실 체크")
                .contains("퇴실 체크")
                .contains("장학포인트")
                .contains("레벨&경험치")
                .contains("필독 알림")
                .contains("mandatoryAlerts")
                .contains("curriculumOverview")
                .contains("개 세션")
                .contains("주차별 커리큘럼")
                .contains("Quest/평가")
                .contains("학습자료")
                .contains("자료")
                .contains("상세보기")
                .contains("actionLabel")
                .contains("최대")
                .contains("학습중 이러닝")
                .contains("자유게시판")
                .contains("e-book")
                .contains("공지사항")
                .contains("DataState")
                .contains("LoadingRows");
    }

    @Test
    void frontendApiClientSurfacesBackendRequestIds() throws IOException {
        String client = Files.readString(CLIENT_TS);

        assertThat(client)
                .contains("readonly requestId")
                .contains("X-Request-Id")
                .contains("요청 ID:");
    }

}
