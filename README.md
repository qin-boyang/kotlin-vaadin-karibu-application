# Kotlin Vaadin + Ktor Todo App

This is a small Kotlin todo application that runs a Vaadin web UI, a Ktor JSON API, and an embedded H2 database from one `main()` function.

It is intentionally not a Spring Boot application. The Vaadin UI is served by Vaadin Boot on embedded Jetty, while the API is served separately by Ktor on embedded Netty.

## What Runs

When you start the app, it launches three local services:

| Service | URL | Purpose |
| --- | --- | --- |
| Vaadin UI | `http://localhost:8080` | Browser UI for managing todos |
| Ktor API | `http://localhost:8081` | JSON CRUD API for todos |
| H2 Console | `http://localhost:8082` | Browser console for inspecting the embedded database |

The UI and API both use the same embedded H2 database through Exposed.

## Tech Stack

- Kotlin
- Vaadin Flow UI
- Karibu-DSL for declarative Vaadin UI code
- Vaadin Boot with embedded Jetty for the servlet-based Vaadin app
- Ktor with embedded Netty for the REST API
- H2 in-memory database
- Exposed SQL library
- Gradle

## Requirements

- JDK 21 or newer

## Run Locally

```bash
./gradlew run
```

Then open:

- Vaadin UI: `http://localhost:8080`
- Ktor API: `http://localhost:8081/api/todos`
- H2 Console: `http://localhost:8082`

## H2 Console Login

Use these settings in the H2 Console:

| Field | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:test` |
| User Name | `sa` |
| Password | leave empty |

The database is in memory, so data is reset when the application stops.

## API Endpoints

The Ktor API exposes full CRUD for todos:

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/todos` | List all todos |
| `GET` | `/api/todos/{id}` | Get one todo |
| `POST` | `/api/todos` | Create a todo |
| `PUT` | `/api/todos/{id}` | Update a todo |
| `DELETE` | `/api/todos/{id}` | Delete a todo |

Example create request:

```bash
curl -X POST http://localhost:8081/api/todos \
  -H "Content-Type: application/json" \
  -d '{"text":"Learn Vaadin and Ktor","done":false}'
```

Example response:

```json
{
  "id": 1,
  "text": "Learn Vaadin and Ktor",
  "done": false
}
```

## Project Structure

```text
src/main/kotlin/Main.kt
```

Starts everything:

- H2 web console on port `8082`
- Ktor API server on port `8081`
- Vaadin UI server on port `8080`

```text
src/main/kotlin/com/example/karibudsl/MainView.kt
```

Defines the Vaadin todo UI and the `Todos` table/data model used by the app.

```text
src/main/kotlin/com/example/karibudsl/MainApi.kt
```

Defines the Ktor JSON API routes.

```text
src/main/kotlin/com/example/karibudsl/Database.kt
```

Connects to the embedded H2 database and creates the schema.

## Useful Commands

```bash
./gradlew test
```

Run tests.

```bash
./gradlew build
```

Compile, test, and build the app.

```bash
./gradlew clean build -Pvaadin.productionMode
```

Build in Vaadin production mode.

## Notes

- This project uses Vaadin's servlet-based server model through Vaadin Boot.
- The Ktor API is a separate embedded server, not a Vaadin route.
- There is no Spring or Spring Boot dependency.
- Because H2 is running in memory, every app restart starts with a fresh database.
