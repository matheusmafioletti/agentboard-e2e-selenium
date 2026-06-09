package com.agentboard.e2e.steps;

import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.BoardPage;
import com.agentboard.e2e.pages.DashboardPage;
import com.agentboard.e2e.support.ApiHelper;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.util.Map;
import org.openqa.selenium.WebDriver;

/**
 * Cucumber step definitions shared across multiple feature files.
 *
 * <p>Authentication pre-conditions use {@link ApiHelper} to provision users and set
 * {@code localStorage} directly, bypassing the login UI for non-auth scenarios.
 */
public class CommonSteps {

  /**
   * Creates a new user with ADMIN role, creates a project, and authenticates via localStorage.
   * The user, project, and token are stored in {@link ScenarioContext} for downstream steps.
   */
  @Given("I am authenticated as a regular admin user")
  public void iAmAuthenticatedAsRegularAdminUser() {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String email = ApiHelper.generateEmail();
    String tenantName = ApiHelper.generateTenantName();
    Map<String, String> user = ApiHelper.createUser(
        env.authBaseUrl(), email, "Abc12345!", tenantName);

    String jwt = user.get("token");
    String tenantId = user.get("tenantId");

    String projectId = ApiHelper.createProject(
        env.boardBaseUrl(), jwt, tenantId, "Default Project");

    driver.get(env.appBaseUrl() + "/login");
    ApiHelper.setAuthInLocalStorage(driver, jwt, email, email, tenantId, tenantName, "ADMIN");

    ScenarioContext.set("currentJwt", jwt);
    ScenarioContext.set("currentTenantId", tenantId);
    ScenarioContext.set("currentEmail", email);
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

    String email = ApiHelper.generateEmail();
    String tenantName = ApiHelper.generateTenantName();
    Map<String, String> user = ApiHelper.createUser(
        env.authBaseUrl(), email, "Abc12345!", tenantName);

    String jwt = user.get("token");
    String tenantId = user.get("tenantId");
    String projectId = ApiHelper.createProject(
        env.boardBaseUrl(), jwt, tenantId, "Test Project");

    String featureId = ApiHelper.createWorkItem(
        env.boardBaseUrl(), jwt, tenantId, projectId, "Sample Feature", "FEATURE");
    String storyId = ApiHelper.createWorkItem(
        env.boardBaseUrl(), jwt, tenantId, projectId, "Sample Story", "USER_STORY");
    ApiHelper.createWorkItem(
        env.boardBaseUrl(), jwt, tenantId, projectId, "Sample Task", "TASK");

    driver.get(env.appBaseUrl() + "/login");
    ApiHelper.setAuthInLocalStorage(driver, jwt, email, email, tenantId, tenantName, "ADMIN");

    ScenarioContext.set("currentJwt", jwt);
    ScenarioContext.set("currentTenantId", tenantId);
    ScenarioContext.set("currentEmail", email);
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

    String email = ApiHelper.generateEmail();
    String tenantName = ApiHelper.generateTenantName();
    Map<String, String> user = ApiHelper.createUser(
        env.authBaseUrl(), email, "Abc12345!", tenantName);

    String jwt = user.get("token");
    String tenantId = user.get("tenantId");
    String projectId = ApiHelper.createProject(
        env.boardBaseUrl(), jwt, tenantId, "Items Project");

    String featureId = ApiHelper.createWorkItem(
        env.boardBaseUrl(), jwt, tenantId, projectId, "Feature Root", "FEATURE");
    String detailFeatureId = ApiHelper.createWorkItem(
        env.boardBaseUrl(), jwt, tenantId, projectId, "Detail Test Item", "FEATURE");
    ApiHelper.createWorkItem(
        env.boardBaseUrl(), jwt, tenantId, projectId, "Story Under Feature", "USER_STORY");
    ApiHelper.createWorkItem(
        env.boardBaseUrl(), jwt, tenantId, projectId, "Standalone Task", "TASK");

    driver.get(env.appBaseUrl() + "/login");
    ApiHelper.setAuthInLocalStorage(driver, jwt, email, email, tenantId, tenantName, "ADMIN");

    ScenarioContext.set("currentJwt", jwt);
    ScenarioContext.set("currentTenantId", tenantId);
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

    String email = ApiHelper.generateEmail();
    String tenantName = ApiHelper.generateTenantName();
    Map<String, String> user = ApiHelper.createUser(
        env.authBaseUrl(), email, "Abc12345!", tenantName);

    String jwt = user.get("token");
    String tenantId = user.get("tenantId");

    driver.get(env.appBaseUrl() + "/login");
    ApiHelper.setAuthInLocalStorage(driver, jwt, email, email, tenantId, tenantName, "USER");

    ScenarioContext.set("currentJwt", jwt);
    ScenarioContext.set("currentTenantId", tenantId);
    ScenarioContext.set("currentEmail", email);
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
    Environment env = ScenarioContext.get("env", Environment.class);
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String inviteToken = ApiHelper.createInvite(env.authBaseUrl(), jwt, tenantId, email);
    ScenarioContext.set("pendingInviteEmail", email);
    ScenarioContext.set("pendingInviteToken", inviteToken);

    WebDriver driver = ScenarioContext.getDriver();
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
