# AgentBoard — E2E Tests (Selenium + Cucumber)

Selenium 4 + Cucumber 7 + Allure E2E suite for AgentBoard.

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker Compose v2 (for `@local` / profile `e2e`)
- Chrome (WebDriverManager handles driver)

## Setup

```bash
mvn test-compile
npm install   # optional — wraps Maven scripts
```

## Running Tests

| Command | Description |
|---------|-------------|
| `mvn test -Plocal` | Native local URLs (`:5173`) |
| `npm run test:local` | `@local` via compose (`-Pe2e`) |
| `npm run test:staging` | `@staging` smoke (`-Pstaging`) |

### Full stack

```bash
./scripts/run-e2e-local.sh selenium [--reset]
```

## Environment profiles

| Profile | Properties file | Base URL |
|---------|-----------------|----------|
| `local` | `local.properties` | `http://localhost:5173` |
| `e2e` | `e2e.properties` | `http://localhost:8080` |
| `staging` | `staging.properties` | Demo URL |

Tag filter:

```bash
mvn test -Pe2e -Dcucumber.filter.tags="@e2e and @local and not @wip"
```

## CI

See [Playwright README](../agentboard-e2e-playwright/README.md#ci) for dispatch / compose flow.

## Secrets

`GHCR_READ_TOKEN`, `E2E_STAGING_USER_*`, `vars.BASE_URL`, `E2E_STAGING_PROJECT_NAME`.
