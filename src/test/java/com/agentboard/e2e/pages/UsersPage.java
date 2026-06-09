package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the AgentBoard user management screen ({@code /usuarios}).
 *
 * <p>This page is only accessible to users with the {@code ADMIN} role.
 */
public class UsersPage extends BasePage {

  /** Container element for the active members list. */
  public static final By MEMBERS_LIST = By.cssSelector(
      "[data-testid='members-list'], [class*='members-list'], "
      + "[class*='member-list'], [data-testid='users-list']");

  /** Container element for the pending invites list. */
  public static final By PENDING_INVITES_LIST = By.cssSelector(
      "[data-testid='pending-invites'], [class*='pending-invites'], "
      + "[class*='invite-list'], [data-testid='invites-list']");

  /** Button that opens the invite creation form or modal. */
  public static final By CREATE_INVITE_BUTTON = By.cssSelector(
      "[data-testid='create-invite-button'], button[data-testid='invite-user'], "
      + "button[aria-label*='invite' i], button[aria-label*='convidar' i], "
      + "button:has-text('Invite'), button:has-text('Convidar')");

  /** Email address input inside the invite form. */
  public static final By INVITE_EMAIL_INPUT = By.cssSelector(
      "[data-testid='invite-email-input'], input[name='email'], "
      + "input[type='email'], input[placeholder*='email' i]");

  /** Submit button inside the invite form. */
  public static final By INVITE_SUBMIT_BUTTON = By.cssSelector(
      "[data-testid='invite-submit'], form button[type='submit'], "
      + "[class*='modal'] button[type='submit'], [class*='invite'] button[type='submit']");

  /** Element shown when access to this page is blocked (403 / redirect). */
  private static final By ACCESS_BLOCKED_INDICATOR = By.cssSelector(
      "[data-testid='access-denied'], [data-testid='forbidden'], "
      + "[class*='forbidden'], [class*='access-denied']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public UsersPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Navigates to the users management page ({@code /usuarios}).
   */
  public void navigate() {
    navigate("/usuarios");
  }

  /**
   * Opens the invite form and submits an invitation for the given email address.
   *
   * @param email email address to invite
   */
  public void createInvite(String email) {
    click(CREATE_INVITE_BUTTON);
    type(INVITE_EMAIL_INPUT, email);
    click(INVITE_SUBMIT_BUTTON);
  }

  /**
   * Finds and clicks the "cancel" action for the pending invite matching {@code email}.
   *
   * @param email email address of the invite to cancel
   */
  public void cancelInvite(String email) {
    By cancelButton = By.xpath(
        "//*[@data-testid='pending-invite-item' or contains(@class,'invite-item')]"
        + "[contains(normalize-space(.), '" + email + "')]"
        + "//*[contains(@data-testid,'cancel') or contains(@aria-label,'cancel') "
        + "or contains(normalize-space(.),'Cancel') or contains(normalize-space(.),'Cancelar')]");
    click(cancelButton);
  }

  /**
   * Returns the list of email addresses shown in the active members section.
   *
   * @return member email strings
   */
  public List<String> getMemberEmails() {
    return extractEmailsFrom(MEMBERS_LIST);
  }

  /**
   * Returns the list of email addresses shown in the pending invites section.
   *
   * @return pending invite email strings
   */
  public List<String> getPendingInviteEmails() {
    return extractEmailsFrom(PENDING_INVITES_LIST);
  }

  /**
   * Returns {@code true} when the browser is denied access to this page (non-admin user).
   *
   * @return access-blocked status
   */
  public boolean isAccessBlocked() {
    try {
      wait.until(ExpectedConditions.or(
          ExpectedConditions.visibilityOfElementLocated(ACCESS_BLOCKED_INDICATOR),
          ExpectedConditions.urlContains("/inicio"),
          ExpectedConditions.urlContains("/board")));
      if (!driver.getCurrentUrl().contains("/usuarios")) {
        return true;
      }
      return isVisible(ACCESS_BLOCKED_INDICATOR);
    } catch (Exception e) {
      return !driver.getCurrentUrl().contains("/usuarios");
    }
  }

  private List<String> extractEmailsFrom(By containerLocator) {
    try {
      WebElement container = waitFor(containerLocator);
      List<WebElement> items = container.findElements(
          By.cssSelector("[data-testid='member-email'], [class*='email'], "
              + "[data-email], td:nth-child(2), [class*='member-item']"));
      if (!items.isEmpty()) {
        return items.stream()
            .map(WebElement::getText)
            .map(String::trim)
            .filter(t -> !t.isEmpty() && t.contains("@"))
            .toList();
      }
      return container.findElements(By.xpath(".//*[contains(normalize-space(.),'@')]"))
          .stream()
          .map(WebElement::getText)
          .map(String::trim)
          .filter(t -> !t.isEmpty() && t.contains("@"))
          .toList();
    } catch (Exception e) {
      return List.of();
    }
  }
}
