package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the AgentBoard login screen ({@code /login}).
 */
public class LoginPage extends BasePage {

  private static final By EMAIL_INPUT = By.cssSelector(
      "input[type='email'], input[name='email']");
  private static final By PASSWORD_INPUT = By.cssSelector(
      "input[type='password'], input[name='password']");
  private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
  private static final By ERROR_MESSAGE = By.cssSelector(
      "[data-testid='auth-error'], .error-message, [role='alert']");
  private static final By BOARD_HEADING = By.cssSelector(
      "[data-testid='board-page'], .board-container, h1");

  /** Container shown when the user belongs to multiple tenants and must pick one. */
  private static final By WORKSPACE_SELECTION_CONTAINER = By.cssSelector(
      "[data-testid='workspace-selection'], [data-testid='tenant-selection'], "
      + "[class*='workspace-selection'], [class*='tenant-selection']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public LoginPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Opens the login page directly by URL.
   */
  public void navigate() {
    navigate("/login");
  }

  /**
   * Fills in credentials and submits the login form.
   *
   * @param email    user email address
   * @param password user password
   */
  public void login(String email, String password) {
    type(EMAIL_INPUT, email);
    type(PASSWORD_INPUT, password);
    click(SUBMIT_BUTTON);
  }

  /**
   * Fills in credentials without submitting.
   *
   * @param email    user email address
   * @param password user password
   */
  public void fillCredentials(String email, String password) {
    type(EMAIL_INPUT, email);
    type(PASSWORD_INPUT, password);
  }

  /**
   * Submits the login form (assumes credentials have already been entered).
   */
  public void submit() {
    click(SUBMIT_BUTTON);
  }

  /**
   * Returns the visible error message text shown after a failed login attempt.
   *
   * @return error message text, or empty string if not present
   */
  public String getErrorMessage() {
    try {
      return waitFor(ERROR_MESSAGE).getText().trim();
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Returns {@code true} when the workspace selection screen is visible after login.
   *
   * @return workspace selection visibility
   */
  public boolean isWorkspaceSelectionVisible() {
    try {
      wait.until(ExpectedConditions.or(
          ExpectedConditions.visibilityOfElementLocated(WORKSPACE_SELECTION_CONTAINER),
          ExpectedConditions.urlContains("/workspace"),
          ExpectedConditions.urlContains("/select-tenant")));
      return true;
    } catch (Exception e) {
      return isVisible(WORKSPACE_SELECTION_CONTAINER);
    }
  }

  /**
   * Selects the workspace with the given name on the tenant-selection screen.
   *
   * @param workspaceName workspace display name to select
   */
  public void selectWorkspace(String workspaceName) {
    By option = By.xpath(
        "//*[@data-testid='workspace-option' or @data-testid='tenant-option' "
        + "or contains(@class,'workspace-option') or contains(@class,'tenant-option')]"
        + "[contains(normalize-space(.), '" + workspaceName + "')]");
    click(option);
  }

  /**
   * Returns {@code true} when the browser has navigated away from the login page
   * to the main application after a successful login.
   *
   * @return login success indicator
   */
  public boolean isLoginSuccessful() {
    try {
      waitFor(BOARD_HEADING);
      return !driver.getCurrentUrl().contains("/login");
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns {@code true} when the browser is on a page other than {@code /login} or
   * the workspace selection screen, indicating successful workspace authentication.
   *
   * @return authentication success indicator
   */
  public boolean isAuthenticatedInWorkspace() {
    try {
      wait.until(ExpectedConditions.not(
          ExpectedConditions.urlContains("/login")));
      String url = driver.getCurrentUrl();
      return !url.contains("/login") && !url.contains("/workspace")
          && !url.contains("/select-tenant");
    } catch (Exception e) {
      return false;
    }
  }
}
