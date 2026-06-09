package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the AgentBoard projects listing screen ({@code /projetos}).
 */
public class ProjectsPage extends BasePage {

  /** Button that opens the "Create project" modal. */
  public static final By CREATE_PROJECT_BUTTON = By.cssSelector(
      "[data-testid='create-project-button'], button[aria-label*='project' i], "
      + "button[aria-label*='projeto' i], button:has-text('New Project'), "
      + "button:has-text('Novo Projeto'), button:has-text('Create')");

  /** List of project items rendered on the page. */
  public static final By PROJECT_LIST_ITEMS = By.cssSelector(
      "[data-testid='project-item'], [data-testid='project-card'], "
      + "[class*='project-item'], [class*='project-card'], "
      + "[class*='project-list'] li, [class*='projects'] [class*='item']");

  /** Project name input field inside the creation modal. */
  public static final By MODAL_PROJECT_NAME_INPUT = By.cssSelector(
      "[data-testid='project-name-input'], input[name='name'], "
      + "input[placeholder*='project' i], input[placeholder*='projeto' i]");

  /** Submit button inside the project creation modal. */
  public static final By MODAL_SUBMIT_BUTTON = By.cssSelector(
      "[data-testid='modal-submit'], form button[type='submit'], "
      + "[class*='modal'] button[type='submit'], [class*='dialog'] button[type='submit']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public ProjectsPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Navigates to the projects listing page ({@code /projetos}).
   */
  public void navigate() {
    navigate("/projetos");
  }

  /**
   * Opens the creation modal and creates a new project with the given name.
   *
   * @param name project display name
   */
  public void createProject(String name) {
    click(CREATE_PROJECT_BUTTON);
    type(MODAL_PROJECT_NAME_INPUT, name);
    click(MODAL_SUBMIT_BUTTON);
  }

  /**
   * Clicks the project card or row whose visible text matches {@code name}.
   *
   * @param name project name to click
   */
  public void clickProject(String name) {
    By projectLink = By.xpath(
        "//*[@data-testid='project-item' or @data-testid='project-card' "
        + "or contains(@class,'project-item') or contains(@class,'project-card')]"
        + "[contains(normalize-space(.), '" + name + "')]");
    click(projectLink);
  }

  /**
   * Returns all visible project names on the listing page.
   *
   * @return list of project name strings
   */
  public List<String> getProjectNames() {
    try {
      return driver.findElements(PROJECT_LIST_ITEMS).stream()
          .map(WebElement::getText)
          .map(String::trim)
          .filter(t -> !t.isEmpty())
          .toList();
    } catch (Exception e) {
      return List.of();
    }
  }

  /**
   * Returns {@code true} when a project with the given name is visible on the page.
   *
   * @param name project name to check
   * @return visibility status
   */
  public boolean isProjectVisible(String name) {
    try {
      wait.until(ExpectedConditions.visibilityOfElementLocated(
          By.xpath("//*[contains(normalize-space(.), '" + name + "')]")));
      return getProjectNames().stream()
          .anyMatch(n -> n.contains(name));
    } catch (Exception e) {
      return false;
    }
  }
}
