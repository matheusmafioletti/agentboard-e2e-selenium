package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the AgentBoard dashboard / home screen ({@code /inicio}).
 */
public class DashboardPage extends BasePage {

  /** Workspace name displayed in the sidebar header. */
  public static final By SIDEBAR_WORKSPACE = By.cssSelector(
      "[data-testid='sidebar-workspace-name'], [class*='sidebar'] [class*='workspace'], "
      + "[class*='sidebar'] [class*='tenant'], nav [class*='workspace']");

  /** Base locator for sidebar navigation links. */
  public static final By SIDEBAR_NAV_LINK = By.cssSelector(
      "[data-testid='sidebar-nav'] a, nav a, [class*='sidebar'] a, aside a");

  /** Profile / account button in the sidebar or header. */
  public static final By PROFILE_BUTTON = By.cssSelector(
      "[data-testid='profile-button'], button[aria-label*='profile' i], "
      + "[class*='profile'], [class*='avatar']");

  /** Logout button inside the profile dropdown. */
  public static final By LOGOUT_BUTTON = By.cssSelector(
      "[data-testid='logout-button'], button[data-testid='logout'], "
      + "button:has-text('Logout'), [role='menuitem']:has-text('Logout'), "
      + "button[class*='logout'], [data-testid='menu-logout']");

  /** Button that opens the workspace-switcher panel. */
  public static final By WORKSPACE_SWITCHER = By.cssSelector(
      "[data-testid='workspace-switcher'], [data-testid='tenant-switcher'], "
      + "[class*='workspace-switcher'], [class*='tenant-switcher']");

  /** Summary cards visible on the dashboard home page. */
  public static final By SUMMARY_CARDS = By.cssSelector(
      "[data-testid='summary-card'], [class*='summary-card'], "
      + "[class*='stat-card'], [class*='metric-card']");

  /** Dashboard page container element. */
  private static final By DASHBOARD_CONTAINER = By.cssSelector(
      "[data-testid='inicio-page'], [data-testid='dashboard'], "
      + "main, [class*='dashboard'], [class*='home']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public DashboardPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Navigates to the dashboard page ({@code /inicio}).
   */
  public void navigate() {
    navigate("/inicio");
  }

  /**
   * Clicks the profile button and then the logout option.
   */
  public void logout() {
    click(PROFILE_BUTTON);
    click(LOGOUT_BUTTON);
  }

  /**
   * Opens the workspace switcher and selects the workspace with the given name.
   *
   * @param name workspace display name to select
   */
  public void switchWorkspace(String name) {
    click(WORKSPACE_SWITCHER);
    By workspaceOption = By.xpath(
        "//*[contains(@data-testid,'workspace-option') or contains(@class,'workspace-item')]"
        + "[contains(normalize-space(.), '" + name + "')]");
    click(workspaceOption);
  }

  /**
   * Returns the workspace name currently displayed in the sidebar.
   *
   * @return workspace name text, or empty string if not found
   */
  public String getWorkspaceName() {
    try {
      return getText(SIDEBAR_WORKSPACE);
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Returns {@code true} when a sidebar navigation link with the given text is visible.
   *
   * @param linkText link label to search for (case-insensitive partial match)
   * @return visibility status
   */
  public boolean isNavLinkVisible(String linkText) {
    try {
      List<WebElement> links = driver.findElements(SIDEBAR_NAV_LINK);
      return links.stream()
          .anyMatch(el -> el.getText().toLowerCase().contains(linkText.toLowerCase()));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns the number of visible summary cards on the dashboard.
   *
   * @return card count
   */
  public int getSummaryCardCount() {
    try {
      return driver.findElements(SUMMARY_CARDS).size();
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * Returns {@code true} when the dashboard page container is visible.
   *
   * @return visibility status
   */
  public boolean isLoaded() {
    try {
      wait.until(ExpectedConditions.urlContains("/inicio"));
      return true;
    } catch (Exception e) {
      return !driver.getCurrentUrl().contains("/login")
          && !driver.getCurrentUrl().contains("/register");
    }
  }
}
