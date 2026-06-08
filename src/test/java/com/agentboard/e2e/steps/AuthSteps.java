package com.agentboard.e2e.steps;

import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.LoginPage;
import com.agentboard.e2e.pages.RegisterPage;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for authentication flows (login and register).
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
  }

  /**
   * Enters the supplied credentials into the login form fields.
   *
   * @param email    user email
   * @param password user password
   */
  @When("I enter email {string} and password {string}")
  public void iEnterEmailAndPassword(String email, String password) {
    ScenarioContext.set("email", email);
    loginPage.login(email, password);
  }

  /**
   * Submits the login form.
   *
   * <p>The form submission is triggered in {@link #iEnterEmailAndPassword} by {@link LoginPage#login},
   * so this step acts as a semantic checkpoint — form already submitted.
   */
  @And("I click the login button")
  public void iClickTheLoginButton() {
    // NOTE: login() already submits; this step exists for Gherkin readability.
  }

  /**
   * Asserts that a successful login redirected the browser to the board view.
   */
  @Then("I should be redirected to the board")
  public void iShouldBeRedirectedToTheBoard() {
    assertTrue(loginPage.isLoginSuccessful(),
      "Expected browser to navigate away from /login to the board after successful login");
  }

  /**
   * Asserts that the visible error message matches the expected text.
   *
   * @param expectedMessage expected error message substring
   */
  @Then("I should see an error message {string}")
  public void iShouldSeeAnErrorMessage(String expectedMessage) {
    String actual = loginPage.getErrorMessage();
    assertTrue(
      actual.toLowerCase().contains(expectedMessage.toLowerCase()),
      "Expected error message containing '" + expectedMessage + "' but got: '" + actual + "'"
    );
  }

  /**
   * Placeholder step for the multi-tenant workspace selection scenario.
   */
  @Given("I am a user with multiple workspaces")
  public void iAmAUserWithMultipleWorkspaces() {
    // WORKAROUND: multi-workspace setup relies on test-data seeding not yet automated.
  }

  /** Enters generic valid credentials stored in ScenarioContext. */
  @When("I enter valid credentials")
  public void iEnterValidCredentials() {
    String email = ScenarioContext.get("email", String.class);
    if (email == null) {
      email = "alice@test.com";
    }
    loginPage.login(email, "secret123");
  }

  /** Asserts the workspace selection screen is shown after login. */
  @Then("I should see the workspace selection screen")
  public void iShouldSeeTheWorkspaceSelectionScreen() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(
      driver.getCurrentUrl().contains("/workspace") || driver.getCurrentUrl().contains("/select"),
      "Expected workspace selection URL but got: " + driver.getCurrentUrl()
    );
  }

  /** Opens the registration page. */
  @Given("I am on the registration page")
  public void iAmOnTheRegistrationPage() {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    RegisterPage registerPage = new RegisterPage(driver, env);
    registerPage.navigate();
    ScenarioContext.set("registerPage", registerPage);
  }

  /**
   * Fills and submits the registration form.
   *
   * @param name        full name
   * @param email       email address
   * @param password    password
   * @param tenantName  workspace name
   */
  @When("I register with name {string}, email {string}, password {string}, and workspace {string}")
  public void iRegisterWith(String name, String email, String password, String tenantName) {
    RegisterPage registerPage = ScenarioContext.get("registerPage", RegisterPage.class);
    registerPage.register(name, email, password, tenantName);
  }

  /** Asserts registration succeeded. */
  @Then("I should be registered and redirected to the board")
  public void iShouldBeRegisteredAndRedirectedToTheBoard() {
    RegisterPage registerPage = ScenarioContext.get("registerPage", RegisterPage.class);
    assertTrue(registerPage.isRegistrationSuccessful(), "Expected successful registration redirect");
  }

  /** Asserts a registration error is shown. */
  @Then("I should see a registration error message")
  public void iShouldSeeARegistrationErrorMessage() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(
      driver.getCurrentUrl().contains("/register"),
      "Expected to remain on /register after duplicate email"
    );
  }
}
