package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class FrontendRouteSmokeCoverageTest {

    private static final Path ROUTES_TS = Path.of("..", "frontend", "src", "routes.ts");
    private static final Path APP_TSX = Path.of("..", "frontend", "src", "App.tsx");
    private static final Path OPS_READINESS_TSX = Path.of("..", "frontend", "src", "pages", "OpsReadinessPage.tsx");

    private static final List<String> REQUIRED_SCREEN_ROUTES = List.of(
            "/",
            "/login",
            "/profile/check",
            "/profile/edit",
            "/mycampus/attendance",
            "/mycampus/attendance/appeals/new",
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
        assertThat(app).contains("match(/^\\/learning\\/materials\\/(\\d+)\\/viewer$/)");
        assertThat(app).contains("match(/^\\/quest\\/(\\d+)\\/submit$/)");
        assertThat(app).contains("match(/^\\/survey\\/(\\d+)\\/respond$/)");
        assertThat(app).contains("match(/^\\/help\\/qna\\/tickets\\/(\\d+)$/)");
        assertThat(readinessPage).contains("screenSmokeRoutes.map");
    }
}
