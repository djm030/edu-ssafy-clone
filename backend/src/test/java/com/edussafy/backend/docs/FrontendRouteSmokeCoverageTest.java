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
            "/community/free",
            "/community/free/1",
            "/community/free/write",
            "/survey",
            "/survey/1",
            "/survey/1/respond",
            "/help/qna",
            "/help/qna/new",
            "/help/qna/tickets/1",
            "/mycampus/notifications",
            "/learning/curriculum",
            "/learning/materials",
            "/learning/materials/1",
            "/learning/materials/1/viewer",
            "/learning/replays",
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
        assertThat(app).contains("match(/^\\/mycampus\\/elearning\\/(\\d+)$/)");
        assertThat(app).contains("match(/^\\/learning\\/materials\\/(\\d+)\\/viewer$/)");
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
        Map<String, Path> pages = Map.of(
                "attendance", Path.of("..", "frontend", "src", "pages", "AttendancePage.tsx"),
                "board", Path.of("..", "frontend", "src", "components", "BoardListPage.tsx"),
                "survey", Path.of("..", "frontend", "src", "pages", "SurveyPage.tsx"),
                "notifications", Path.of("..", "frontend", "src", "pages", "NotificationsPage.tsx"),
                "learning", Path.of("..", "frontend", "src", "pages", "MaterialsPage.tsx"),
                "elearning", Path.of("..", "frontend", "src", "pages", "ElearningPage.tsx"),
                "quest", Path.of("..", "frontend", "src", "pages", "QuestPage.tsx"),
                "support", Path.of("..", "frontend", "src", "pages", "QnaListPage.tsx")
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
