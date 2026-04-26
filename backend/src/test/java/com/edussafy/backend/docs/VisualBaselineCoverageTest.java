package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class VisualBaselineCoverageTest {

    private static final Path VISUAL_SPEC = Path.of("..", "frontend", "e2e", "visual-baseline.spec.ts");
    private static final Path SNAPSHOT_DIR = Path.of("..", "frontend", "e2e", "visual-baseline.spec.ts-snapshots");

    private static final List<String> REQUIRED_TARGETS = List.of(
            "login",
            "dashboard",
            "attendance",
            "level",
            "elearning",
            "bookmarks",
            "documents",
            "pledges",
            "ebooks",
            "education-status",
            "live-sessions",
            "curriculum",
            "required-studies",
            "my-replays",
            "all-replays",
            "quest",
            "materials",
            "survey",
            "free-board",
            "anonymous-board",
            "classmates",
            "notice",
            "faq",
            "academic-rules",
            "qna",
            "mentor-stories",
            "mentoring-questions",
            "mentoring-notices",
            "mentoring-meetings",
            "meeting-results",
            "meeting-reviews",
            "external-services"
    );

    @Test
    void visualBaselineSpecTracksFullCloneScreenSet() throws IOException {
        String spec = Files.readString(VISUAL_SPEC);

        assertThat(spec)
                .contains("width: 1440")
                .contains("width: 390")
                .contains("toHaveScreenshot")
                .doesNotContain("@naver.com")
                .doesNotContain("djm" + "062954");

        for (String target : REQUIRED_TARGETS) {
            assertThat(spec).contains("name: '" + target + "'");
            assertThat(Files.exists(SNAPSHOT_DIR.resolve(target + "-desktop.png")))
                    .as(target + " desktop baseline")
                    .isTrue();
            assertThat(Files.exists(SNAPSHOT_DIR.resolve(target + "-mobile.png")))
                    .as(target + " mobile baseline")
                    .isTrue();
        }
    }
}
