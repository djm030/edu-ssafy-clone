package com.edussafy.backend.docs.api;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiDocsController {

    private static final Resource API_DOCS_HTML = new ClassPathResource("static/api-docs/index.html");

    @GetMapping(value = {"/api/docs", "/api/docs/"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> showApiDocs() {
        if (!API_DOCS_HTML.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.TEXT_HTML)
                .body(API_DOCS_HTML);
    }
}
