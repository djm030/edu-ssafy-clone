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

Then start the backend and open:

```text
http://localhost:8080/docs/api/index.html
```

## Outputs

- Snippets: `backend/target/generated-snippets/`
- Packaged HTML page: `backend/target/classes/static/docs/api/index.html` after `prepare-package` or `package`
- Server URL after starting the backend from that build output: `http://localhost:8080/docs/api/index.html`
- Source: `backend/src/docs/asciidoc/index.adoc`
- Tests: `backend/src/test/java/com/edussafy/backend/docs/ApiRestDocsTest.java`

## Current documented operations

- `POST /api/auth/login`
- `POST /api/learning/materials/{id}/reactions`
- `GET /api/boards/{boardCode}/posts`
- `POST /api/support/tickets`

Keep `docs/api-summary.md`, `docs/openapi.yaml`, and `docs/openapi.json` as the broad controller-derived API catalog. Use Spring REST Docs for executable request/response examples generated from tests.
