package com.agentboard.e2e.steps;

import com.agentboard.e2e.api.clients.AuthApiClient;
import com.agentboard.e2e.api.clients.AuthApiClient;
import com.agentboard.e2e.api.services.TestDataService;
import com.agentboard.e2e.api.types.UserCredentials;
import com.agentboard.e2e.api.types.UserInfo;
import com.agentboard.e2e.api.types.WorkItemType;
import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.BoardPage;
import com.agentboard.e2e.pages.DashboardPage;
import com.agentboard.e2e.support.BrowserAuth;
import com.agentboard.e2e.support.Generators;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

/**
 * Cucumber step definitions shared across multiple feature files.
 *
 * <p>Authentication pre-conditions use {@link TestDataService} to provision users and set
 * {@code localStorage} directly, bypassing the login UI for non-auth scenarios.
 */
public class CommonSteps {

  /**
   * Authenticates via API using the staging smoke seed user credentials.
   */
  @Given("I am authenticated as staging smoke admin")
  public void iAmAuthenticatedAsStagingSmokeAdmin() {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String email = System.getenv().getOrDefault(
        "E2E_STAGING_USER_EMAIL", "staging-smoke@agentboard.dev");
    String password = System.getenv().getOrDefault(
        "E2E_STAGING_USER_PASSWORD", "StagingSmoke123!");
    String tenantName = System.getenv().getOrDefault(
        "E2E_STAGING_TENANT_NAME", "E2E Smoke Workspace");

    AuthApiClient authClient = new AuthApiClient(env.authBaseUrl());
    var loginData = authClient.login(email, password);
    String jwt = loginData.getString("token");
    String tenantId = loginData.getString("tenantId");
    String role = loginData.optString("role", "ADMIN");

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(
        driver,
        jwt,
        new UserInfo(email, email, tenantId, tenantName, role));

    ScenarioContext.set("currentJwt", jwt);
    ScenarioContext.set("currentTenantId", tenantId);
    ScenarioContext.set("currentEmail", email);
    ScenarioContext.set("currentTenantName", tenantName);
  }

  /**
   * Creates a new user with ADMIN role, creates a project, and authenticates via localStorage.
   * The user, project, and token are stored in {@link ScenarioContext} for downstream steps.
   */
  @Given("I am authenticated as a regular admin user")
  public void iAmAuthenticatedAsRegularAdminUser() {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String email = Generators.generateEmail();
    String tenantName = Generators.generateTenantName();
    UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(
        email, "Abc12345!", tenantName);

    String projectId = TestDataService.INSTANCE.createProject(
        user.jwt(), user.tenantId(), "Default Project").id();

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(driver, user.jwt(), user.toUserInfo());

    ScenarioContext.set("currentJwt", user.jwt());
    ScenarioContext.set("currentTenantId", user.tenantId());
    ScenarioContext.set("currentEmail", user.email());
    ScenarioContext.set("currentTenantName", tenantName);
    ScenarioContext.set("currentProjectId", projectId);

    DashboardPage dashboard = new DashboardPage(driver, env);
    ScenarioContext.set("dashboardPage", dashboard);
  }

  /**
   * Creates a user, project, and a set of work items (FEATURE, USER_STORY, TASK) via API,
   * then authenticates via localStorage.
   */
  @Given("I am authenticated with a project that has work items")
  public void iAmAuthenticatedWithProjectThatHasWorkItems() {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String email = Generators.generateEmail();
    String tenantName = Generators.generateTenantName();
    UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(
        email, "Abc12345!", tenantName);

    String projectId = TestDataService.INSTANCE.createProject(
        user.jwt(), user.tenantId(), "Test Project").id();

    String featureId = TestDataService.INSTANCE.createWorkItem(
        user.jwt(), user.tenantId(), projectId, "Sample Feature", WorkItemType.FEATURE).id();
    String storyId = TestDataService.INSTANCE.createWorkItem(
        user.jwt(), user.tenantId(), projectId, "Sample Story", WorkItemType.USER_STORY).id();
    TestDataService.INSTANCE.createWorkItem(
        user.jwt(), user.tenantId(), projectId, "Sample Task", WorkItemType.TASK);

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(driver, user.jwt(), user.toUserInfo());

    ScenarioContext.set("currentJwt", user.jwt());
    ScenarioContext.set("currentTenantId", user.tenantId());
    ScenarioContext.set("currentEmail", user.email());
    ScenarioContext.set("currentTenantName", tenantName);
    ScenarioContext.set("currentProjectId", projectId);
    ScenarioContext.set("currentFeatureId", featureId);
    ScenarioContext.set("currentStoryId", storyId);

    BoardPage board = new BoardPage(driver, env);
    ScenarioContext.set("boardPage", board);
  }

  /**
   * Creates a user, project, and a sample set of Features, User Stories and Tasks via API,
   * then authenticates via localStorage.
   */
  @Given("I am authenticated with a project containing Features, User Stories and Tasks")
  public void iAmAuthenticatedWithProjectContainingAllTypes() {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String email = Generators.generateEmail();
    String tenantName = Generators.generateTenantName();
    UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(
        email, "Abc12345!", tenantName);

    String projectId = TestDataService.INSTANCE.createProject(
        user.jwt(), user.tenantId(), "Items Project").id();

    String featureId = TestDataService.INSTANCE.createWorkItem(
        user.jwt(), user.tenantId(), projectId, "Feature Root", WorkItemType.FEATURE).id();
    TestDataService.INSTANCE.createWorkItem(
        user.jwt(), user.tenantId(), projectId, "Detail Test Item", WorkItemType.FEATURE);
    TestDataService.INSTANCE.createWorkItem(
        user.jwt(), user.tenantId(), projectId, "Story Under Feature", WorkItemType.USER_STORY);
    TestDataService.INSTANCE.createWorkItem(
        user.jwt(), user.tenantId(), projectId, "Standalone Task", WorkItemType.TASK);

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(driver, user.jwt(), user.toUserInfo());

    ScenarioContext.set("currentJwt", user.jwt());
    ScenarioContext.set("currentTenantId", user.tenantId());
    ScenarioContext.set("currentProjectId", projectId);
    ScenarioContext.set("currentFeatureId", featureId);
  }

  /**
   * Creates an ADMIN user, project, and authenticates via localStorage.
   */
  @Given("I am authenticated as an ADMIN")
  public void iAmAuthenticatedAsAdmin() {
    iAmAuthenticatedAsRegularAdminUser();
  }

  @Given("I am authenticated as staging smoke admin")
  public void iAmAuthenticatedAsStagingSmokeAdmin() {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String email = System.getenv().getOrDefault(
        "E2E_STAGING_USER_EMAIL", "staging-smoke@agentboard.dev");
    String password = System.getenv().getOrDefault(
        "E2E_STAGING_USER_PASSWORD", "StagingSmoke123!");
    String tenantName = System.getenv().getOrDefault(
        "E2E_STAGING_TENANT_NAME", "E2E Smoke Workspace");

    AuthApiClient authClient = new AuthApiClient(env.authBaseUrl());
    org.json.JSONObject loginData = authClient.login(email, password);
    String jwt = loginData.getString("token");
    String tenantId = loginData.optString("tenantId", "");

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(
        driver,
        jwt,
        new UserInfo(email, email, tenantId, tenantName, loginData.optString("role", "ADMIN")));

    ScenarioContext.set("currentJwt", jwt);
    ScenarioContext.set("currentTenantId", tenantId);
    ScenarioContext.set("currentEmail", email);
    ScenarioContext.set("currentTenantName", tenantName);
  }

  /**
   * Creates an ADMIN user and navigates to the users management page.
   */
  @Given("I am authenticated as an ADMIN on the users page")
  public void iAmAuthenticatedAsAdminOnUsersPage() {
    iAmAuthenticatedAsRegularAdminUser();
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    driver.get(env.appBaseUrl() + "/usuarios");
  }

  /**
   * Creates a USER-role user and authenticates via localStorage.
   *
   * <p>NOTE: the API does not currently allow setting USER role during registration;
   * the role is set only in {@code localStorage} to simulate the restricted access check.
   */
  @Given("I am authenticated as a USER \\(non-admin\\)")
  public void iAmAuthenticatedAsUserNonAdmin() {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String email = Generators.generateEmail();
    String tenantName = Generators.generateTenantName();
    UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(
        email, "Abc12345!", tenantName);

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(
        driver,
        user.jwt(),
        new UserInfo(user.userId(), user.email(), user.tenantId(), tenantName, "USER"));

    ScenarioContext.set("currentJwt", user.jwt());
    ScenarioContext.set("currentTenantId", user.tenantId());
    ScenarioContext.set("currentEmail", user.email());
    ScenarioContext.set("currentTenantName", tenantName);
  }

  /**
   * Creates an ADMIN user with a pending invite for the given email.
   *
   * @param email email address to pre-create a pending invite for
   */
  @Given("I am authenticated as an ADMIN with a pending invite for {string}")
  public void iAmAuthenticatedAsAdminWithPendingInviteFor(String email) {
    iAmAuthenticatedAsRegularAdminUser();
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String inviteToken = TestDataService.INSTANCE.createInvite(jwt, tenantId, email).token();
    ScenarioContext.set("pendingInviteEmail", email);
    ScenarioContext.set("pendingInviteToken", inviteToken);

    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    driver.get(env.appBaseUrl() + "/usuarios");
  }

  /**
   * Navigates the browser directly to the given path.
   *
   * @param path relative URL path (e.g. {@code "/board"})
   */
  @When("I navigate directly to {string}")
  public void iNavigateDirectlyTo(String path) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    driver.get(env.appBaseUrl() + path);
  }
}
