package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the AgentBoard registration screen ({@code /register}).
 */
public class RegisterPage extends BasePage {

  private static final By NAME_INPUT = By.cssSelector("input[name='name'], input[placeholder*='name' i]");
  private static final By EMAIL_INPUT = By.cssSelector("input[type='email'], input[name='email']");
  private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'], input[name='password']");
  private static final By TENANT_NAME_INPUT = By.cssSelector("input[name='tenantName'], input[placeholder*='workspace' i], input[placeholder*='organization' i]");
  private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");
  private static final By SUCCESS_INDICATOR = By.cssSelector("[data-testid='board-page'], .board-container, [data-testid='registration-success']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public RegisterPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Opens the registration page directly by URL.
   */
  public void navigate() {
    navigate("/register");
  }

  /**
   * Fills in all registration fields and submits the form.
   *
   * @param name       full name of the user
   * @param email      email address
   * @param password   password
   * @param tenantName name of the new workspace / tenant
   */
  public void register(String name, String email, String password, String tenantName) {
    type(NAME_INPUT, name);
    type(EMAIL_INPUT, email);
    type(PASSWORD_INPUT, password);
    type(TENANT_NAME_INPUT, tenantName);
    click(SUBMIT_BUTTON);
  }

  /**
   * Returns {@code true} when the registration completes and the user lands on the board
   * or a success confirmation page.
   *
   * @return registration success indicator
   */
  public boolean isRegistrationSuccessful() {
    try {
      waitFor(SUCCESS_INDICATOR);
      String url = driver.getCurrentUrl();
      return !url.contains("/register");
    } catch (Exception e) {
      return false;
    }
  }
}
