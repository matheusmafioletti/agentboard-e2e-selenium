package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the AgentBoard login screen ({@code /login}).
 */
public class LoginPage extends BasePage {

  private static final By EMAIL_INPUT = By.cssSelector("input[type='email'], input[name='email']");
  private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'], input[name='password']");
  private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
  private static final By ERROR_MESSAGE = By.cssSelector("[data-testid='auth-error'], .error-message, [role='alert']");
  private static final By BOARD_HEADING = By.cssSelector("[data-testid='board-page'], .board-container, h1");

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
   * Returns {@code true} when the browser has navigated away from the login page
   * to the main board view after a successful login.
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
}
