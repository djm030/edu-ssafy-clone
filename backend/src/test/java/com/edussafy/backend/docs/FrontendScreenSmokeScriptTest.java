package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class FrontendScreenSmokeScriptTest {

    private static final Path ROUTES_TS = Path.of("..", "frontend", "src", "routes.ts");
    private static final Path SMOKE_ROUTES_SH = Path.of("..", "scripts", "dev", "smoke-routes.sh");

    @Test
    void screenSmokeScriptExercisesEveryManifestRouteAgainstSpaShell() throws IOException {
        String routes = Files.readString(ROUTES_TS);
        String script = Files.readString(SMOKE_ROUTES_SH);
        Matcher matcher = Pattern.compile("path: '([^']+)'").matcher(routes);
        int routeCount = 0;

        while (matcher.find()) {
            String route = matcher.group(1);
            assertThat(script).contains('"' + route + '"');
            routeCount++;
        }

        assertThat(routeCount).isGreaterThanOrEqualTo(25);
        assertThat(script)
                .contains("curl -fsS")
                .contains("<div id=\"root\"></div>")
                .contains("SKIP_HTTP");
    }
}
