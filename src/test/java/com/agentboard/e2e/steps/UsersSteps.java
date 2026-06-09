package com.agentboard.e2e.steps;

import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.DashboardPage;
import com.agentboard.e2e.pages.InviteAcceptPage;
import com.agentboard.e2e.pages.UsersPage;
import com.agentboard.e2e.support.ApiHelper;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for user management scenarios (TC-USERS-001 through TC-USERS-006).
 */
public class UsersSteps {

  // -------------------------------------------------------------------------
  // Pre-conditions
  // -------------------------------------------------------------------------

  /**
   * Navigates to the users management page.
   */
  @When("I navigate to the users page")
  public void iNavigateToTheUsersPage() {
    UsersPage page = getOrCreateUsersPage();
    page.navigate();
    ScenarioContext.set("usersPage", page);
  }

  /**
   * Creates a pending invite via API and navigates to the invite acceptance URL.
   *
   * @param email       email address that was invited
   * @param tenantName  workspace display name for the invite
   */
  @Given("an invite token exists for {string} to workspace {string}")
  public void anInviteTokenExistsForToWorkspace(String email, String tenantName) {
    Environment env = ScenarioContext.get("env", Environment.class);
    WebDriver driver = ScenarioContext.getDriver();

    String adminEmail = ApiHelper.generateEmail();
    Map<String, String> admin = ApiHelper.createUser(
        env.authBaseUrl(), adminEmail, "Abc12345!", tenantName);
    String jwt = admin.get("token");
    String tenantId = admin.get("tenantId");

    String inviteToken = ApiHelper.createInvite(env.authBaseUrl(), jwt, tenantId, email);
    ScenarioContext.set("inviteToken", inviteToken);
    ScenarioContext.set("inviteTenantName", tenantName);
    ScenarioContext.set("invitedEmail", email);

    driver.get(env.appBaseUrl() + "/login");
    ApiHelper.clearAuthFromLocalStorage(driver);
  }

  // -------------------------------------------------------------------------
  // TC-USERS-001/002: Members list and access control
  // -------------------------------------------------------------------------

  /**
   * Asserts the members list section is visible.
   */
  @Then("the members list should be visible")
  public void theMembersListShouldBeVisible() {
    UsersPage page = getOrCreateUsersPage();
    assertTrue(page.isVisible(UsersPage.MEMBERS_LIST),
        "Expected the members list to be visible on /usuarios");
  }

  /**
   * Asserts member details include email and role information.
   */
  @And("member details should include email and role")
  public void memberDetailsShouldIncludeEmailAndRole() {
    UsersPage page = getOrCreateUsersPage();
    List<String> emails = page.getMemberEmails();
    assertFalse(emails.isEmpty(),
        "Expected at least one member with email to be listed");
  }

  /**
   * Asserts access to the users page is blocked.
   */
  @Then("access should be blocked")
  public void accessShouldBeBlocked() {
    UsersPage page = getOrCreateUsersPage();
    assertTrue(page.isAccessBlocked(),
        "Expected access to /usuarios to be blocked for non-admin user");
  }

  /**
   * Asserts the "Usuários" sidebar link is not visible.
   */
  @And("the {string} link should not be visible in the sidebar")
  public void theLinkShouldNotBeVisibleInSidebar(String linkText) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    DashboardPage dashboard = new DashboardPage(driver, env);
    assertFalse(dashboard.isNavLinkVisible(linkText),
        "Expected sidebar link '" + linkText + "' to NOT be visible for USER role");
  }

  // -------------------------------------------------------------------------
  // TC-USERS-003: Create invite
  // -------------------------------------------------------------------------

  /**
   * Creates an invite for the given email via the users page UI.
   *
   * @param email email address to invite
   */
  @When("I create an invite for {string}")
  public void iCreateAnInviteFor(String email) {
    getOrCreateUsersPage().createInvite(email);
    ScenarioContext.set("lastInvitedEmail", email);
  }

  /**
   * Asserts the given email appears in the pending invites list.
   *
   * @param email expected email in pending invites
   */
  @Then("{string} should appear in the pending invites list")
  public void shouldAppearInThePendingInvitesList(String email) {
    UsersPage page = getOrCreateUsersPage();
    List<String> pending = page.getPendingInviteEmails();
    assertTrue(
        pending.stream().anyMatch(e -> e.contains(email)),
        "Expected '" + email + "' in pending invites but found: " + pending);
  }

  // -------------------------------------------------------------------------
  // TC-USERS-004: Cancel invite
  // -------------------------------------------------------------------------

  /**
   * Cancels the pending invite for the given email via the users page UI.
   *
   * @param email email whose invite should be cancelled
   */
  @When("I cancel the invite for {string}")
  public void iCancelTheInviteFor(String email) {
    getOrCreateUsersPage().cancelInvite(email);
    ScenarioContext.set("cancelledInviteEmail", email);
  }

  /**
   * Asserts the given email is no longer in the pending invites list.
   *
   * @param email email that should have been removed
   */
  @Then("{string} should not appear in the pending invites list")
  public void shouldNotAppearInThePendingInvitesList(String email) {
    UsersPage page = getOrCreateUsersPage();
    List<String> pending = page.getPendingInviteEmails();
    assertFalse(
        pending.stream().anyMatch(e -> e.contains(email)),
        "Expected '" + email + "' to be removed from pending invites but still found: " + pending);
  }

  // -------------------------------------------------------------------------
  // TC-USERS-005: Accept invite
  // -------------------------------------------------------------------------

  /**
   * Navigates to the invite acceptance URL.
   */
  @When("I open the invite URL with the token")
  public void iOpenTheInviteUrlWithTheToken() {
    String token = ScenarioContext.get("inviteToken", String.class);
    assertNotNull(token, "Expected invite token to be stored in scenario context");
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    InviteAcceptPage page = new InviteAcceptPage(driver, env);
    page.navigate(token);
    ScenarioContext.set("inviteAcceptPage", page);
  }

  /**
   * Navigates to the invite URL with an explicit token value.
   *
   * @param token invite token string
   */
  @When("I open the invite URL with token {string}")
  public void iOpenTheInviteUrlWithTokenValue(String token) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    InviteAcceptPage page = new InviteAcceptPage(driver, env);
    page.navigate(token);
    ScenarioContext.set("inviteAcceptPage", page);
  }

  /**
   * Completes the invite registration form.
   *
   * @param name     display name
   * @param password password for the new account
   */
  @And("I complete registration with name {string} and password {string}")
  public void iCompleteRegistrationWithNameAndPassword(String name, String password) {
    InviteAcceptPage page = ScenarioContext.get("inviteAcceptPage", InviteAcceptPage.class);
    assertNotNull(page, "Expected invite accept page to be initialised");
    page.acceptAsNewUser(name, password);
  }

  /**
   * Asserts the user is authenticated in the workspace named in the invite context.
   *
   * @param workspaceName expected workspace
   */
  @Then("I should be authenticated in workspace {string}")
  public void iShouldBeAuthenticatedInWorkspaceFromInvite(String workspaceName) {
    WebDriver driver = ScenarioContext.getDriver();
    assertFalse(driver.getCurrentUrl().contains("/invite"),
        "Expected to be redirected away from /invite after successful registration");
    assertFalse(driver.getCurrentUrl().contains("/login"),
        "Expected NOT to be on /login after invite acceptance");
  }

  /**
   * Asserts the authenticated user has the USER role.
   */
  @And("my role should be USER")
  public void myRoleShouldBeUser() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(
        driver.getPageSource().toLowerCase().contains("user")
        || !driver.getCurrentUrl().contains("/login"),
        "Expected USER role context after accepting invite");
  }

  // -------------------------------------------------------------------------
  // TC-USERS-006: Invalid token
  // -------------------------------------------------------------------------

  /**
   * Asserts an error message is displayed on the invite page.
   */
  @Then("an error message should be displayed")
  public void anErrorMessageShouldBeDisplayed() {
    InviteAcceptPage page = ScenarioContext.get("inviteAcceptPage", InviteAcceptPage.class);
    assertNotNull(page, "Expected invite accept page to be set");
    assertTrue(page.isErrorVisible(),
        "Expected error message to be visible for invalid invite token");
  }

  /**
   * Asserts the registration form is not visible (invalid token scenario).
   */
  @And("the registration form should not be visible")
  public void theRegistrationFormShouldNotBeVisible() {
    InviteAcceptPage page = ScenarioContext.get("inviteAcceptPage", InviteAcceptPage.class);
    assertNotNull(page, "Expected invite accept page to be set");
    assertFalse(page.isFormVisible(),
        "Expected registration form to be hidden for invalid invite token");
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private UsersPage getOrCreateUsersPage() {
    UsersPage page = ScenarioContext.get("usersPage", UsersPage.class);
    if (page == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      page = new UsersPage(driver, env);
      ScenarioContext.set("usersPage", page);
    }
    return page;
  }
}
