# agentboard-e2e-selenium

[![E2E Tests](https://github.com/your-org/agentboard-e2e-selenium/actions/workflows/ci.yml/badge.svg)](https://github.com/your-org/agentboard-e2e-selenium/actions/workflows/ci.yml)

End-to-end test suite for [AgentBoard](http://localhost:5173) — a multi-tenant Kanban system — using **Selenium 4**, **Cucumber 7 (BDD/Gherkin)**, **JUnit 5**, and **Allure 2** reports.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 21 |
| Maven | 3.9+ |
| Google Chrome | latest stable |
| AgentBoard frontend | running at `http://localhost:5173` |
| AgentBoard backend | running at `http://localhost:8080` / `8081` |

> Chrome driver is downloaded automatically by **WebDriverManager** — no manual driver setup required.

---

## Running Tests

### All E2E tests, headless (CI default)

```bash
mvn test -Denv=local -Dheadless=true
```

### Specific tag filter

```bash
mvn test -Dcucumber.filter.tags="@auth and not @wip"
```

### Headed browser (local debugging)

```bash
mvn test -P local -Dheadless=false
```

### Firefox

```bash
mvn test -Dbrowser=firefox -Dheadless=true
```

### Staging environment

```bash
APP_BASE_URL=https://staging.agentboard.example.com mvn test -P staging
```

---

## Generating the Allure Report

```bash
mvn allure:report
# Open target/site/allure-maven-plugin/index.html in your browser

# Or serve it locally
mvn allure:serve
```

---

## Project Structure

```
src/test/
├── java/com/agentboard/e2e/
│   ├── api/
│   │   ├── clients/
│   │   │   ├── BaseApiClient.java    # Shared HTTP transport, JSON headers
│   │   │   ├── AuthApiClient.java    # Register, login, tenants, invites
│   │   │   └── BoardApiClient.java   # Projects, work-items
│   │   ├── services/
│   │   │   └── TestDataService.java  # High-level test setup workflows (singleton INSTANCE)
│   │   └── types/
│   │       ├── UserCredentials.java  # Authenticated user + JWT
│   │       ├── ProjectResult.java
│   │       └── WorkItemType.java     # FEATURE | USER_STORY | TASK
│   ├── config/
│   │   ├── Environment.java      # Owner @Config — typed .properties binding
│   │   └── DriverFactory.java    # Chrome / Firefox WebDriver creation
│   ├── pages/
│   │   ├── BasePage.java         # Explicit WebDriverWait helpers
│   │   ├── LoginPage.java
│   │   ├── RegisterPage.java
│   │   └── BoardPage.java
│   ├── hooks/
│   │   └── WebHooks.java         # @Before/@After driver lifecycle + failure screenshot
│   ├── steps/
│   │   ├── AuthSteps.java        # Login / register step definitions
│   │   ├── BoardSteps.java       # Kanban board step definitions
│   │   └── CommonSteps.java      # Shared authentication pre-conditions
│   ├── support/
│   │   ├── BrowserAuth.java      # localStorage auth injection
│   │   ├── Generators.java       # Unique email / tenant name generators
│   │   └── ScenarioContext.java  # ThreadLocal state (safe for parallel runs)
│   └── runners/
│       └── CucumberRunner.java   # JUnit Platform Suite entry point
└── resources/
    ├── features/
    │   ├── auth/
    │   │   ├── login.feature
    │   │   └── register.feature
    │   └── board/
    │       └── kanban-flow.feature
    ├── environments/
    │   ├── local.properties
    │   └── staging.properties
    ├── allure.properties
    └── cucumber.properties
```

### API Clients & Test Data Service (`api/`)

HTTP calls are organized into service-specific clients that read base URLs from `Environment` config internally — step definitions never pass raw URLs:

| Layer | Path | Responsibility |
|---|---|---|
| `BaseApiClient` | `api/clients/BaseApiClient.java` | Shared HTTP transport, JSON headers, error handling |
| `AuthApiClient` | `api/clients/AuthApiClient.java` | Register, login, tenants, invites |
| `BoardApiClient` | `api/clients/BoardApiClient.java` | Projects, work-items |
| `TestDataService` | `api/services/TestDataService.java` | High-level test setup workflows |

Step classes use the singleton `TestDataService.INSTANCE` for API setup — bypassing UI forms for fast, deterministic pre-conditions:

```java
import com.agentboard.e2e.api.services.TestDataService;
import com.agentboard.e2e.api.types.UserCredentials;
import com.agentboard.e2e.support.Generators;
import com.agentboard.e2e.support.BrowserAuth;

UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(
    Generators.generateEmail(), "Abc12345!", Generators.generateTenantName());
String projectId = TestDataService.INSTANCE.createProject(
    user.jwt(), user.tenantId(), "My Project").id();
driver.get(env.appBaseUrl() + "/login");
BrowserAuth.setAuthInLocalStorage(driver, user.jwt(), user.toUserInfo());
```

Domain types live in `api/types/`. Generators and browser helpers remain in `support/Generators.java` and `support/BrowserAuth.java`.

---

## Configuration

Environment-specific settings live in `src/test/resources/environments/`.
All values can be overridden at runtime via `-D` system properties or Maven profiles.

| Property | Default | Description |
|----------|---------|-------------|
| `app.base.url` | `http://localhost:5173` | Frontend base URL |
| `browser` | `chrome` | `chrome` or `firefox` |
| `headless` | `true` | Headless mode flag |
| `implicit.wait.seconds` | `10` | Global implicit wait |
| `page.load.timeout.seconds` | `30` | Page load timeout |

---

## Maven Profiles

| Profile | Activation | Purpose |
|---------|-----------|---------|
| `local` | default | Development with headed Chrome |
| `staging` | `-P staging` | CI against staging URL |
| `headless` | `-P headless` | Force headless on any profile |

---

## Why Selenium instead of Playwright?

| Dimension | Selenium 4 (this project) | Playwright |
|-----------|--------------------------|------------|
| Protocol | W3C WebDriver — standard across all browsers | CDP / browser-specific protocol |
| Language | Java 21 — same stack as the backend | Node.js / Python / Java (newer) |
| BDD | Cucumber 7 native, mature ecosystem | Requires extra glue |
| Reporting | Allure 2 — rich, standard in Java CI | Custom or Playwright HTML |
| Parallel | ThreadLocal + JUnit Platform | Built-in but Node-centric |
| Ecosystem | Vast Java QA tooling (TestContainers, WireMock) | Growing, mostly Node |

For a Java-first team already using Spring Boot + JUnit, Selenium keeps the entire test stack in one language and integrates naturally with existing CI pipelines and Allure dashboards.
