# Spring REST Docs

This project uses Spring REST Docs as the executable API documentation path instead of requiring Swagger UI or `/v3/api-docs`.

## Generate docs

From the repository root:

```bash
bash scripts/dev/backend-test.sh
```

or from `backend/` with Maven/Java 21:

```bash
mvn -B test
mvn -B prepare-package
```

Then start the backend directly and open:

```text
http://localhost:8080/docs/api/index.html
```

When using the Docker app stack through nginx, open:

```text
http://localhost/docs/api/index.html
```

## Outputs

- Snippets: `backend/target/generated-snippets/`
- Packaged HTML page: `backend/target/classes/static/docs/api/index.html` after `prepare-package` or `package`
- Backend direct URL after starting the backend from that build output: `http://localhost:8080/docs/api/index.html`
- Docker nginx URL after rebuilding/reloading app stack: `http://localhost/docs/api/index.html`
- Source: `backend/src/docs/asciidoc/index.adoc`
- Tests: `backend/src/test/java/com/edussafy/backend/docs/ApiRestDocsTest.java`

## Final verification status (2026-04-25)

- `mvn -B prepare-package` generated `backend/target/classes/static/docs/api/index.html` and the file size was verified as 37565 bytes.
- The rebuilt Docker app stack served the generated page from backend (`http://127.0.0.1:8080/docs/api/index.html` inside the backend container: `200 37565`) and through nginx (`http://127.0.0.1/docs/api/index.html` inside the nginx container: `200`, `Content-Length: 37565`).
- Spring REST Docs executable coverage is **PARTIAL** for full-clone completion: the current test suite generates snippets for 4 documented operations while the Spring MVC controller surface has 52 operations. Use `docs/openapi.yaml` and `docs/openapi.json` as the broad controller-derived catalog until REST Docs coverage is expanded.

## Current documented operations

- `POST /api/auth/login`
- `POST /api/learning/materials/{id}/reactions`
- `GET /api/boards/{boardCode}/posts`
- `POST /api/support/tickets`

Keep `docs/api-summary.md`, `docs/openapi.yaml`, and `docs/openapi.json` as the broad controller-derived API catalog. Use Spring REST Docs for executable request/response examples generated from tests.
