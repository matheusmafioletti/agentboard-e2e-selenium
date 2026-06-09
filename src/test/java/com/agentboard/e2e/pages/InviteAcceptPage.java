package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the AgentBoard invite acceptance screen ({@code /invite/:token}).
 */
public class InviteAcceptPage extends BasePage {

  /** Full name input on the invite registration form. */
  public static final By NAME_INPUT = By.cssSelector(
      "[data-testid='invite-name-input'], input[name='name'], "
      + "input[placeholder*='name' i], input[placeholder*='nome' i]");

  /** Password input on the invite registration form. */
  public static final By PASSWORD_INPUT = By.cssSelector(
      "[data-testid='invite-password-input'], input[type='password'], "
      + "input[name='password']");

  /** Form submit button. */
  public static final By SUBMIT_BUTTON = By.cssSelector(
      "[data-testid='invite-submit'], button[type='submit'], "
      + "form button:last-of-type");

  /** Error message element shown for invalid or expired invite tokens. */
  public static final By ERROR_MESSAGE = By.cssSelector(
      "[data-testid='invite-error'], [data-testid='auth-error'], "
      + "[class*='error'], [role='alert']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public InviteAcceptPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Navigates directly to the invite acceptance URL for the given token.
   *
   * @param token raw invite token (appended to {@code /invite/})
   */
  public void navigate(String token) {
    navigate("/invite/" + token);
  }

  /**
   * Fills in the new-user registration fields and submits the acceptance form.
   *
   * @param name     display name for the new user
   * @param password chosen password
   */
  public void acceptAsNewUser(String name, String password) {
    type(NAME_INPUT, name);
    type(PASSWORD_INPUT, password);
    click(SUBMIT_BUTTON);
  }

  /**
   * Returns {@code true} when an error message is visible on the page.
   *
   * @return error visibility status
   */
  public boolean isErrorVisible() {
    return isVisible(ERROR_MESSAGE);
  }

  /**
   * Returns {@code true} when the invite acceptance form fields are visible.
   *
   * @return form visibility status
   */
  public boolean isFormVisible() {
    return isVisible(NAME_INPUT) || isVisible(SUBMIT_BUTTON);
  }
}
