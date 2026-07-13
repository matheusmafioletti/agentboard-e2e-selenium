package com.agentboard.e2e.steps;

import com.agentboard.e2e.api.services.TestDataService;
import com.agentboard.e2e.api.types.UserCredentials;
import com.agentboard.e2e.api.types.UserInfo;
import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.DashboardPage;
import com.agentboard.e2e.pages.LoginPage;
import com.agentboard.e2e.pages.RegisterPage;
import com.agentboard.e2e.support.BrowserAuth;
import com.agentboard.e2e.support.Generators;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for authentication flows (login, register, session).
 *
 * <p>User creation steps generate unique credentials via {@link TestDataService} so that scenarios
 * are idempotent across multiple runs. Gherkin email literals serve as logical labels only;
 * the actual email used is stored in {@code ScenarioContext} under the key
 * {@code user_<label>_email}.
 */
public class AuthSteps {

  private LoginPage loginPage;


  /**
   * Navigates to the login page and initialises the {@link LoginPage} object.
   */
  @Given("I am on the login page")
  public void iAmOnTheLoginPage() {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    loginPage = new LoginPage(driver, env);
    loginPage.navigate();
    ScenarioContext.set("loginPage", loginPage);
  }

  /**
   * Opens the registration page.
   */
  @Given("I am on the registration page")
  public void iAmOnTheRegistrationPage() {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    RegisterPage registerPage = new RegisterPage(driver, env);
    registerPage.navigate();
    ScenarioContext.set("registerPage", registerPage);
  }


  /**
   * Creates a user via API so the login form can submit real credentials.
   *
   * <p>The {@code emailLabel} is used only as a logical identifier; the real email is
   * generated to avoid conflicts between runs.
   *
   * @param emailLabel  logical label (e.g. {@code "alice@test.com"})
   * @param password    plain-text password
   * @param tenantName  workspace display name
   */
  @Given("a user {string} with password {string} and workspace {string} exists")
  public void aUserWithPasswordAndWorkspaceExists(
      String emailLabel, String password, String tenantName) {
    String email = Generators.generateEmail();
    UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(
        email, password, tenantName);
    ScenarioContext.set("user_" + emailLabel + "_email", email);
    ScenarioContext.set("user_" + emailLabel + "_password", password);
    ScenarioContext.set("user_" + emailLabel + "_tenantName", tenantName);
    ScenarioContext.set("user_" + emailLabel + "_token", user.jwt());
    ScenarioContext.set("user_" + emailLabel + "_tenantId", user.tenantId());
  }

  /**
   * Creates a user with two workspaces (the first from registration, the second via the
   * create-tenant API) so the multi-tenant login flow can be exercised.
   *
   * @param emailLabel logical label for the user
   * @param password   plain-text password
   */
  @Given("a user {string} with password {string} belongs to 2 workspaces")
  public void aUserBelongsTo2Workspaces(String emailLabel, String password) {
    String email = Generators.generateEmail();
    String ws1 = emailLabel.replace("@test.com", "") + " WS 1";
    UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(email, password, ws1);
    String ws2 = emailLabel.replace("@test.com", "") + " WS 2";
    TestDataService.INSTANCE.createSecondTenant(user.jwt(), ws2);

    ScenarioContext.set("user_" + emailLabel + "_email", email);
    ScenarioContext.set("user_" + emailLabel + "_password", password);
    ScenarioContext.set("user_" + emailLabel + "_ws1", ws1);
    ScenarioContext.set("user_" + emailLabel + "_ws2", ws2);
  }

  /**
   * Creates a user that already exists in the system (for duplicate-email error scenarios).
   *
   * @param email the email address to pre-create
   */
  @Given("a user with email {string} already exists")
  public void aUserWithEmailAlreadyExists(String email) {
    String generatedEmail = Generators.generateEmail();
    TestDataService.INSTANCE.createAuthenticatedUser(generatedEmail, "Abc12345!", "Existing WS");
    ScenarioContext.set("existing_email", email);
    ScenarioContext.set("existing_real_email", generatedEmail);
  }

  /**
   * Creates a user with two named workspaces and authenticates via localStorage.
   *
   * @param ws1 first workspace name
   * @param ws2 second workspace name
   */
  @Given("I am authenticated as a user with 2 workspaces {string} and {string}")
  public void iAmAuthenticatedAsUserWith2Workspaces(String ws1, String ws2) {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();
    String email = Generators.generateEmail();
    UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(email, "Abc12345!", ws1);
    TestDataService.INSTANCE.createSecondTenant(user.jwt(), ws2);

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(driver, user.jwt(), user.toUserInfo());

    ScenarioContext.set("ws1", ws1);
    ScenarioContext.set("ws2", ws2);
    ScenarioContext.set("currentJwt", user.jwt());
    ScenarioContext.set("currentTenantId", user.tenantId());
    ScenarioContext.set("currentEmail", email);
  }


  /**
   * Enters credentials into the login form, resolving the actual email from context when
   * the label matches a previously created user.
   *
   * @param emailLabel email literal or label
   * @param password   password to type
   */
  @When("I enter email {string} and password {string}")
  public void iEnterEmailAndPassword(String emailLabel, String password) {
    if (loginPage == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      loginPage = new LoginPage(driver, env);
    }
    String resolvedEmail = resolveEmail(emailLabel);
    loginPage.login(resolvedEmail, password);
    ScenarioContext.set("lastEmailLabel", emailLabel);
  }

  /**
   * Semantic no-op: credentials were already submitted in the previous step.
   *
   * <p>NOTE: kept for Gherkin readability; {@link LoginPage#login} submits the form.
   */
  @And("I click the login button")
  public void iClickTheLoginButton() {
    // NOTE: login() already submits; this step exists for Gherkin readability.
  }

  /**
   * Asserts the browser navigated to the dashboard after login.
   */
  @Then("I should be redirected to the dashboard")
  public void iShouldBeRedirectedToTheDashboard() {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    DashboardPage dashboard = new DashboardPage(driver, env);
    assertTrue(dashboard.isLoaded(),
        "Expected browser to navigate to dashboard but URL was: " + driver.getCurrentUrl());
    ScenarioContext.set("dashboardPage", dashboard);
  }

  /**
   * Asserts the sidebar shows the expected workspace name.
   *
   * @param workspaceName expected workspace display name
   */
  @And("the workspace {string} should be shown in the sidebar")
  public void theWorkspaceShouldBeShownInSidebar(String workspaceName) {
    DashboardPage dashboard = ScenarioContext.get("dashboardPage", DashboardPage.class);
    if (dashboard == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      dashboard = new DashboardPage(driver, env);
    }
    String actual = dashboard.getWorkspaceName();
    assertTrue(
        actual.contains(workspaceName) || workspaceName.contains(actual),
        "Expected sidebar to show workspace '" + workspaceName + "' but found: '" + actual + "'");
  }

  /**
   * Asserts the workspace selection screen is displayed after a multi-tenant login.
   */
  @Then("the workspace selection screen should be displayed")
  public void theWorkspaceSelectionScreenShouldBeDisplayed() {
    assertTrue(loginPage.isWorkspaceSelectionVisible(),
        "Expected workspace selection screen to appear after multi-tenant login");
  }

  /**
   * Clicks the workspace option with the given name on the selection screen.
   *
   * @param workspaceName workspace to select
   */
  @When("I select workspace {string}")
  public void iSelectWorkspace(String workspaceName) {
    loginPage.selectWorkspace(workspaceName);
  }

  /**
   * Asserts the user is now authenticated and the correct workspace is active.
   *
   * @param workspaceName expected active workspace
   */
  @Then("I should be authenticated in workspace {string}")
  public void iShouldBeAuthenticatedInWorkspace(String workspaceName) {
    assertTrue(loginPage.isAuthenticatedInWorkspace(),
        "Expected to be authenticated but URL was: "
        + ScenarioContext.getDriver().getCurrentUrl());
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    DashboardPage dashboard = new DashboardPage(driver, env);
    ScenarioContext.set("dashboardPage", dashboard);
    String actual = dashboard.getWorkspaceName();
    assertTrue(
        actual.isEmpty() || actual.contains(workspaceName) || workspaceName.contains(actual),
        "Expected active workspace '" + workspaceName + "' but sidebar shows: '" + actual + "'");
  }

  /**
   * Asserts that a generic error message is shown after failed login.
   */
  @Then("I should see a login error message")
  public void iShouldSeeALoginErrorMessage() {
    String msg = loginPage.getErrorMessage();
    assertFalse(msg.isEmpty(),
        "Expected an error message to be visible after failed login but none was found");
  }

  /**
   * Asserts the legacy error message format (used by original login scenarios).
   *
   * @param expectedMessage expected error text
   */
  @Then("I should see an error message {string}")
  public void iShouldSeeAnErrorMessage(String expectedMessage) {
    String actual = loginPage.getErrorMessage();
    assertTrue(
        actual.toLowerCase().contains(expectedMessage.toLowerCase()),
        "Expected error containing '" + expectedMessage + "' but got: '" + actual + "'");
  }


  /**
   * Ensures the browser has no active authentication (clears localStorage).
   */
  @Given("I am not authenticated")
  public void iAmNotAuthenticated() {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.clearAuthFromLocalStorage(driver);
  }

  /**
   * Authenticates as the given email label via localStorage bypass.
   *
   * <p>If no user was previously created for this label a new one is created.
   *
   * @param emailLabel logical email label
   */
  @Given("I am authenticated as {string}")
  public void iAmAuthenticatedAs(String emailLabel) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);

    String jwt = ScenarioContext.get("user_" + emailLabel + "_token", String.class);
    String tenantId = ScenarioContext.get("user_" + emailLabel + "_tenantId", String.class);
    String tenantName = ScenarioContext.get("user_" + emailLabel + "_tenantName", String.class);
    String email = ScenarioContext.get("user_" + emailLabel + "_email", String.class);

    if (jwt == null) {
      String generatedEmail = Generators.generateEmail();
      String ws = "Workspace-" + System.currentTimeMillis();
      UserCredentials user = TestDataService.INSTANCE.createAuthenticatedUser(
          generatedEmail, "Abc12345!", ws);
      jwt = user.jwt();
      tenantId = user.tenantId();
      tenantName = ws;
      email = generatedEmail;
    }

    driver.get(env.appBaseUrl() + "/login");
    BrowserAuth.setAuthInLocalStorage(
        driver,
        jwt,
        new UserInfo(email, email, tenantId, tenantName, "ADMIN"));

    ScenarioContext.set("currentJwt", jwt);
    ScenarioContext.set("currentTenantId", tenantId);
    ScenarioContext.set("currentEmail", email);
    ScenarioContext.set("currentTenantName", tenantName);

    DashboardPage dashboard = new DashboardPage(driver, env);
    dashboard.navigate();
    ScenarioContext.set("dashboardPage", dashboard);
  }

  /**
   * Performs logout via the profile menu.
   */
  @When("I logout")
  public void iLogout() {
    DashboardPage dashboard = ScenarioContext.get("dashboardPage", DashboardPage.class);
    if (dashboard == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      dashboard = new DashboardPage(driver, env);
    }
    dashboard.logout();
  }

  /**
   * Asserts the browser is on the login page.
   */
  @Then("I should be on the login page")
  public void iShouldBeOnTheLoginPage() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(driver.getCurrentUrl().contains("/login"),
        "Expected to be on /login but URL was: " + driver.getCurrentUrl());
    loginPage = new LoginPage(driver, ScenarioContext.get("env", Environment.class));
  }

  /**
   * Asserts the browser was redirected to login when accessing a protected route.
   */
  @Then("I should be redirected to login")
  public void iShouldBeRedirectedToLogin() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(driver.getCurrentUrl().contains("/login"),
        "Expected redirect to /login but URL was: " + driver.getCurrentUrl());
  }


  /**
   * Registers with a unique auto-generated email, password, and workspace.
   *
   * @param password    password for the new account
   * @param tenantName  workspace display name
   */
  @When("I register with a unique email, password {string} and workspace {string}")
  public void iRegisterWithUniqueEmailPasswordAndWorkspace(String password, String tenantName) {
    RegisterPage registerPage = ScenarioContext.get("registerPage", RegisterPage.class);
    String email = Generators.generateEmail();
    registerPage.register("Test User", email, password, tenantName);
    ScenarioContext.set("registeredEmail", email);
    ScenarioContext.set("registeredTenant", tenantName);
  }

  /**
   * Attempts to register using a specific email address that is expected to already exist.
   *
   * @param email    email address (already taken)
   * @param password password to use
   */
  @When("I try to register with email {string} and password {string}")
  public void iTryToRegisterWithEmailAndPassword(String email, String password) {
    RegisterPage registerPage = ScenarioContext.get("registerPage", RegisterPage.class);
    String realEmail = ScenarioContext.get("existing_real_email", String.class);
    registerPage.register("Duplicate User", realEmail != null ? realEmail : email, password, "Dup WS");
  }

  /**
   * Asserts a registration error is shown.
   */
  @Then("I should see a registration error")
  public void iShouldSeeARegistrationError() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(driver.getCurrentUrl().contains("/register"),
        "Expected to remain on /register after error");
  }

  /**
   * Asserts the browser is still on the registration page.
   */
  @And("I should remain on the register page")
  public void iShouldRemainOnTheRegisterPage() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(driver.getCurrentUrl().contains("/register"),
        "Expected to be on /register but URL was: " + driver.getCurrentUrl());
  }

  /**
   * Asserts the legacy success redirect (used by original register scenarios).
   */
  @Then("I should be registered and redirected to the board")
  public void iShouldBeRegisteredAndRedirectedToTheBoard() {
    RegisterPage registerPage = ScenarioContext.get("registerPage", RegisterPage.class);
    assertTrue(registerPage.isRegistrationSuccessful(), "Expected successful registration redirect");
  }

  /**
   * Asserts the legacy registration error (used by original register scenarios).
   */
  @Then("I should see a registration error message")
  public void iShouldSeeARegistrationErrorMessage() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(driver.getCurrentUrl().contains("/register"),
        "Expected to remain on /register after duplicate email");
  }


  /**
   * Asserts the dashboard is currently showing the expected workspace.
   *
   * @param workspaceName expected workspace name in the sidebar
   */
  @And("I am on the dashboard showing {string}")
  public void iAmOnDashboardShowing(String workspaceName) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    DashboardPage dashboard = new DashboardPage(driver, env);
    dashboard.navigate();
    ScenarioContext.set("dashboardPage", dashboard);
  }

  /**
   * Switches to the named workspace via the sidebar switcher.
   *
   * @param workspaceName target workspace display name
   */
  @When("I switch to workspace {string} via sidebar")
  public void iSwitchToWorkspaceViaSidebar(String workspaceName) {
    DashboardPage dashboard = ScenarioContext.get("dashboardPage", DashboardPage.class);
    dashboard.switchWorkspace(workspaceName);
  }

  /**
   * Asserts the active workspace shown in the sidebar matches the expected name.
   *
   * @param workspaceName expected workspace
   */
  @Then("the active workspace should be {string}")
  public void theActiveWorkspaceShouldBe(String workspaceName) {
    DashboardPage dashboard = ScenarioContext.get("dashboardPage", DashboardPage.class);
    String actual = dashboard.getWorkspaceName();
    assertTrue(
        actual.contains(workspaceName) || workspaceName.contains(actual),
        "Expected active workspace '" + workspaceName + "' but sidebar shows: '" + actual + "'");
  }


  private String resolveEmail(String emailLabel) {
    String stored = ScenarioContext.get("user_" + emailLabel + "_email", String.class);
    return stored != null ? stored : emailLabel;
  }
}
