# Library Management System

A server-rendered Spring Boot application for managing books, members, categories, and library circulation with MySQL persistence.

## Technology stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Backend | Spring Boot 3.3.5, Spring MVC |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8; H2 in-memory demo profile |
| Views | Thymeleaf, HTML, CSS, JavaScript |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Containers | Docker, Docker Compose |

## Implemented features

- Add, list, edit, delete, and search books.
- Search books by title, ISBN, author, or category.
- Add and edit members; search by name, email, or membership code.
- Add and list categories.
- Reuse an existing author by name or create an author while saving a book.
- Issue available books to active members with a configurable due date.
- Return loans and restore the related book to `AVAILABLE` status.
- View loan history, active loans, and due dates.
- Dashboard counts for total/available/issued books, active members, active loans, overdue active loans, and categories.
- Seed sample data when the book catalog is empty.
- Run with durable MySQL storage or a disposable H2 demo database.

Important scope: this is a Spring MVC web application, not a REST API. It does not include Spring Security, authentication, or authorization. The `OVERDUE` enum exists, but the application calculates overdue totals from active loans whose due date has passed; it does not automatically change loan status to `OVERDUE`.

## Architecture

```text
Browser
  -> Spring MVC controllers
      -> LibraryService (search, statistics, issue/return transactions)
          -> Spring Data JPA repositories
              -> MySQL or H2
  <- Thymeleaf templates + CSS/JavaScript
```

The project uses a conventional layered structure:

```text
src/main/java/com/codex/lms/
├── config/       # DataSeeder
├── controller/   # Page routes and form handling
├── model/        # JPA entities and status enums
├── repository/   # Spring Data queries
└── service/      # Business rules and dashboard calculations

src/main/resources/
├── templates/    # Thymeleaf views
├── static/       # CSS and JavaScript
├── application.properties
└── application-demo.properties
```

`spring.jpa.open-in-view=false` is configured. Repository fetch joins and entity graphs load relationships needed by the catalog, dashboard, and loan views before rendering.

## Data model

| Entity | Main fields | Relationships / rules |
|---|---|---|
| `Book` | title, ISBN, publisher, year, shelf, cover URL, summary, added date, status | Many-to-one with `Author` and `Category`; ISBN is unique; status values are `AVAILABLE`, `ISSUED`, `RESERVED`, `MAINTENANCE` |
| `Author` | name, country, biography | Referenced by books; no dedicated author page/controller |
| `Category` | name, description, accent color | Referenced by books; name is unique |
| `Member` | full name, email, phone, membership code, joined date, status | Email is unique; status values are `ACTIVE`, `SUSPENDED`, `EXPIRED` |
| `Loan` | issued date, due date, returned date, status | Many-to-one with `Book` and `Member`; status values are `ACTIVE`, `RETURNED`, `OVERDUE` |

## Application routes

| Method | Route | Purpose |
|---|---|---|
| `GET` | `/` | Dashboard and recent circulation information |
| `GET` | `/books?q=` | List or search books |
| `GET` | `/books/new` | Add-book form |
| `GET` | `/books/{id}/edit` | Edit-book form |
| `POST` | `/books` | Create or update a book |
| `POST` | `/books/{id}/delete` | Delete a book |
| `GET` | `/members?q=` | List or search members |
| `GET` | `/members/new` | Add-member form |
| `GET` | `/members/{id}/edit` | Edit-member form |
| `POST` | `/members` | Create or update a member |
| `GET` | `/categories` | List categories and show add form |
| `POST` | `/categories` | Create a category |
| `GET` | `/loans` | List loans |
| `GET` | `/loans/new` | Issue-book form |
| `POST` | `/loans` | Issue a book |
| `POST` | `/loans/{id}/return` | Return a book |

## Local setup with MySQL

Prerequisites:

- Java 17 or newer
- Maven 3.6.3 or newer
- MySQL 8

Create the database:

```sql
CREATE DATABASE lms_db;
```

Set connection variables and start the application.

PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/lms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="your_mysql_user"
$env:DB_PASSWORD="your_mysql_password"
mvn spring-boot:run
```

Bash:

```bash
export DB_URL='jdbc:mysql://localhost:3306/lms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='your_mysql_user'
export DB_PASSWORD='your_mysql_password'
mvn spring-boot:run
```

Open `http://localhost:8081`. Hibernate uses `ddl-auto=update`, and `DataSeeder` inserts sample records when no books exist.

### H2 demo mode

No external database is required. Data is discarded when the application stops.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Open `http://localhost:8081`. The H2 console is at `/h2-console` with JDBC URL `jdbc:h2:mem:lms_demo`, username `sa`, and an empty password.

## Docker

`docker-compose.yml` starts MySQL 8.4 only. It exposes MySQL on host port `3307`, creates `lms_db`, persists data in a named volume, and includes a health check.

```bash
docker compose up -d
docker compose ps
mvn spring-boot:run
```

The application defaults already target `localhost:3307`. For anything beyond local development, override the default password:

```bash
MYSQL_ROOT_PASSWORD=change-me docker compose up -d
```

The repository also includes a multi-stage `Dockerfile`. It builds the Maven JAR and runs it on a Java 17 JRE as a non-root user.

```bash
docker build -t library-management-system .
docker run --rm -p 8081:8081 \
  -e DB_URL='jdbc:mysql://host.docker.internal:3307/lms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  library-management-system
```

## Deploy with managed MySQL

### Railway (recommended for this MySQL project)

1. Push the repository to GitHub and create a Railway project from that repository.
2. Add **MySQL** from **New > Database** in the same Railway project.
3. Open the application service's Variables page and add:
   - `DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
   - `DB_USERNAME=${{MySQL.MYSQLUSER}}`
   - `DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}`
4. Railway detects the `Dockerfile`. Deploy the application service.
5. Under **Settings > Networking**, generate a public domain.
6. Open the domain and check deployment logs. Railway supplies `PORT`, and this application reads it through `server.port=${PORT:8081}`.

Railway's current documentation lists a $0 experimentation plan with limited resources, plus paid plans; both the application and database consume usage. Check [Railway pricing](https://docs.railway.com/pricing) and [Railway MySQL variables](https://docs.railway.com/databases/mysql) before deployment.

### Render

Render can run this Java application from its Dockerfile, but Render does not provide managed MySQL. Its managed relational database is PostgreSQL. To retain MySQL, use an external managed-MySQL provider or operate MySQL as a private Render service with a paid persistent disk.

1. Provision MySQL with an external provider, or follow Render's private MySQL service guide.
2. Create a Render **Web Service** from this repository and select the Docker runtime.
3. Add secret environment variables `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` using the MySQL provider's internal/private connection details.
4. Deploy. Render supplies `PORT`; the application reads it automatically.
5. Verify the root route and logs. Keep the application and database in the same region when possible.

Render has free web-service instances with sleep and usage limitations, but self-hosted MySQL requires a persistent disk and paid infrastructure. See [Render free-instance limits](https://render.com/docs/free), [Render datastore support](https://render.com/docs/faq), and [Render's MySQL guide](https://render.com/docs/deploy-mysql).

## Screenshots

Replace these placeholders with screenshots captured from the running application:

| Dashboard | Book catalog |
|---|---|
| `docs/screenshots/dashboard.png` | `docs/screenshots/books.png` |

| Members | Loans |
|---|---|
| `docs/screenshots/members.png` | `docs/screenshots/loans.png` |

## Design decisions

- **Chosen: server-rendered Spring MVC with Thymeleaf.** This keeps deployment simple and demonstrates a complete Java web application in one codebase. A separate SPA and REST API were not used because they would add a second build/deployment and an API contract that this project's scope does not require.
- **Chosen: transactional service methods for circulation.** Issuing and returning update both `Loan` and `Book` state in one transaction. Putting these rules only in controllers was rejected because it would mix HTTP concerns with business logic.
- **Chosen: lazy entity relationships with explicit fetch queries.** Open Session in View is disabled, while repository entity graphs/fetch joins load view data deliberately. Globally eager relationships were rejected because they can retrieve unnecessary data.
- **Chosen: MySQL for persistent use and H2 for demos.** MySQL represents the intended relational database. H2 is retained for quick interviews and local demonstrations, but its in-memory data is intentionally temporary.
- **Chosen: Hibernate schema update for simple setup.** It reduces onboarding work for a portfolio project. Versioned migrations were not added; they would be the safer choice for controlled production schema evolution.
- **Chosen: synchronous MVC workflow.** Issue/return operations immediately update the database and redirect to a rendered page. Messaging or event-driven processing would add complexity without a demonstrated need at this scale.

## Current limitations

- No Spring Security, login, roles, or authorization.
- No REST API; all endpoints render pages or handle HTML forms.
- No automated tests currently exist under `src/test`.
- No pagination for books, members, or loans.
- No dedicated author-management UI and no category edit/delete workflow.
- Overdue loans are counted but are not automatically transitioned to `OVERDUE`.
- No protection against two concurrent issue requests for the same book.
- No database migration tool; schema changes rely on `ddl-auto=update`.
- Error handling mainly uses redirects or default exceptions; there is no global exception handler.
