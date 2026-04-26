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
            "/ops/readiness"
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
        assertThat(app).contains("if (path === '/mycampus/elearning')");
        assertThat(app).contains("if (path === '/mycampus/bookmarks')");
        assertThat(app).contains("if (path === '/mycampus/documents')");
        assertThat(app).contains("if (path === '/mycampus/pledges')");
        assertThat(app).contains("if (path === '/mycampus/ebooks')");
        assertThat(app).contains("if (path === '/learning/required-studies')");
        assertThat(app).contains("if (path === '/learning/live')");
        assertThat(app).contains("if (path === '/learning/replays' || path === '/learning/replays/my')");
        assertThat(app).contains("if (path === '/learning/replays/all')");
        assertThat(app).contains("if (path === '/help/rules')");
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
        assertThat(app).contains("match(/^\\/quest\\/(\\d+)\\/submit$/)");
        assertThat(app).contains("match(/^\\/survey\\/(\\d+)\\/respond$/)");
        assertThat(app).contains("match(/^\\/help\\/qna\\/tickets\\/(\\d+)$/)");
        assertThat(readinessPage)
                .contains("getAccessPolicy")
                .contains("screenSmokeRoutes.map")
                .contains("accessPolicies.map")
                .contains("권한 정책 매트릭스");
    }
    @Test
    void priorityDataPagesExposeLoadingErrorAndEmptyStates() throws IOException {
        Map<String, Path> pages = Map.ofEntries(
                Map.entry("attendance", Path.of("..", "frontend", "src", "pages", "AttendancePage.tsx")),
                Map.entry("board", Path.of("..", "frontend", "src", "components", "BoardListPage.tsx")),
                Map.entry("survey", Path.of("..", "frontend", "src", "pages", "SurveyPage.tsx")),
                Map.entry("notifications", Path.of("..", "frontend", "src", "pages", "NotificationsPage.tsx")),
                Map.entry("learning", Path.of("..", "frontend", "src", "pages", "MaterialsPage.tsx")),
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
                Map.entry("mentoring-meeting-results", Path.of("..", "frontend", "src", "pages", "MentoringMeetingResultsPage.tsx"))
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
    void frontendApiClientSurfacesBackendRequestIds() throws IOException {
        String client = Files.readString(CLIENT_TS);

        assertThat(client)
                .contains("readonly requestId")
                .contains("X-Request-Id")
                .contains("요청 ID:");
    }

}
