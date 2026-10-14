# DocHelp Backend

DocHelp is a backend for a document question-answering application built around retrieval-augmented generation (RAG). The project is under active development. The current implementation supports uploading documents, extracting their text, splitting it into chunks, generating embeddings, and storing those chunks in PostgreSQL with pgvector. A question-and-answer endpoint is not implemented yet.

## Current features

- Upload a document through a REST API.
- Parse PDFs by page and use Apache Tika for other supported document formats.
- Split extracted text into chunks and attach document metadata.
- Store vectors in PostgreSQL using pgvector and Spring AI.
- Track document processing status and metadata in the relational database.
- Serve interactive API documentation with Springdoc OpenAPI.

## Technology

- Java 21 and Spring Boot
- Spring AI with Ollama for local chat and embedding models
- PostgreSQL 16 with the pgvector extension
- Spring Data JPA, Maven, and Docker Compose

## Requirements

- JDK 21
- Docker with the Compose plugin
- Ollama installed and running locally
- The configured Ollama models available locally

The development configuration defaults to `llama3.2:3b` for chat and `nomic-embed-text` for embeddings. Pull them with:

```bash
ollama pull llama3.2:3b
ollama pull nomic-embed-text
```

## Run locally

Start PostgreSQL with the pgvector extension:

```bash
docker compose up -d postgres
```

Make sure Ollama is running, then start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

The API listens on [http://localhost:8081](http://localhost:8081). The development profile is active by default. Docker Compose publishes PostgreSQL on port `5434`; the application connects to that port by default.

## Configuration

The development profile provides local defaults. Override them with environment variables as needed:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5434` | PostgreSQL host port |
| `DB_NAME` | `dochelp` | Database name |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama server URL |
| `OLLAMA_CHAT_MODEL` | `llama3.2:3b` | Chat model name |
| `OLLAMA_EMBEDDING_MODEL` | `nomic-embed-text` | Embedding model name |

Uploaded request size is limited to 50 MB, with individual files limited to 25 MB. RAG chunking and vector-store settings are in `src/main/resources/application-dev.yml`.

## API

### Upload and index a document

`POST /api/v1/document/upload` accepts `multipart/form-data` with a `file` field. The current controller is the only implemented endpoint.

```bash
curl -X POST http://localhost:8081/api/v1/document/upload \
  -F 'file=@./path/to/document.pdf'
```

On success, the API returns a response containing the document ID, filename, file size, processing status, and number of chunks created. Uploaded metadata is persisted in the database; the document chunks and embeddings are stored in the pgvector table.

Interactive API docs are available at [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html), and the OpenAPI document is at [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs).

## Project layout

```text
src/main/java/com/pritam/dochelp/
├── config/       # Application and API configuration
├── controller/   # REST endpoints
├── dto/          # API request and response objects
├── entity/       # Document metadata and status
├── exception/    # Processing errors and exception handling
├── repository/   # Persistence interfaces
└── service/      # Parsing, ingestion, and metadata workflows
```

## Development status

This is an in-progress project. Document ingestion is implemented, while asking questions against indexed documents and returning grounded answers are planned next steps. Authentication, document listing/deletion, and production deployment configuration are also not currently exposed as implemented features.
