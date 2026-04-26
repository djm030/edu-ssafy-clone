package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiDocsCatalogCoverageTest {

    private static final Path API_DOCS_HTML = Path.of("src", "main", "resources", "static", "api-docs", "index.html");
    private static final Path ENDPOINT_CATALOG = Path.of("src", "test", "resources", "api-docs-endpoints.tsv");

    @Test
    void springRestDocsHtmlListsEveryImplementedControllerEndpoint() throws IOException {
        String html = Files.readString(API_DOCS_HTML);
        List<String> endpoints = Files.readAllLines(ENDPOINT_CATALOG).stream()
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("#"))
                .toList();

        assertThat(endpoints).hasSizeGreaterThanOrEqualTo(140);
        assertThat(html).contains("Complete Implemented Endpoint Catalog");
        assertThat(html).contains("Implemented backend API endpoints (" + endpoints.size() + ")");

        for (String endpoint : endpoints) {
            String[] fields = endpoint.split("\\t");
            assertThat(fields).hasSize(5);
            assertThat(html)
                    .as("REST Docs catalog should include %s %s from %s", fields[1], fields[2], fields[3])
                    .contains("<code>" + fields[1] + "</code>")
                    .contains("<code>" + fields[2] + "</code>")
                    .contains("<code>" + fields[3] + "</code>");
        }
    }
}
