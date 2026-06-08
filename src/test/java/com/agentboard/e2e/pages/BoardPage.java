package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the AgentBoard Kanban board screen ({@code /board} or {@code /}).
 */
public class BoardPage extends BasePage {

  private static final By BOARD_CONTAINER = By.cssSelector("[data-testid='board-page'], .board-container, [class*='board']");
  private static final By CREATE_BUTTON = By.cssSelector("[data-testid='create-work-item'], button[aria-label*='create' i], button[aria-label*='add' i]");
  private static final By MODAL_TITLE_INPUT = By.cssSelector("[data-testid='work-item-title'], input[name='title'], input[placeholder*='title' i]");
  private static final By MODAL_SUBMIT = By.cssSelector("[data-testid='submit-work-item'], button[type='submit'], form button[class*='primary' i]");
  private static final By WORK_ITEM_CARDS = By.cssSelector("[data-testid='work-item-card'], .work-item-card, [class*='card']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public BoardPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Returns {@code true} when the Kanban board is fully visible.
   *
   * @return board visibility status
   */
  public boolean isLoaded() {
    return isVisible(BOARD_CONTAINER);
  }

  /**
   * Returns the number of work item cards currently visible in the column whose header
   * contains {@code status} (case-insensitive).
   *
   * @param status column status label, e.g. {@code "TODO"} or {@code "IN_PROGRESS"}
   * @return card count in the matching column
   */
  public int getColumnCardCount(String status) {
    String columnSelector = String.format(
      "[data-testid='column-%s'] [data-testid='work-item-card'], .column-%s .work-item-card",
      status.toLowerCase().replace("_", "-"),
      status.toLowerCase().replace("_", "-")
    );
    By columnCards = By.cssSelector(columnSelector);
    try {
      return driver.findElements(columnCards).size();
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * Clicks the button that opens the "Create work item" modal.
   */
  public void openCreateWorkItemModal() {
    click(CREATE_BUTTON);
  }

  /**
   * Types the given title into the work item title field inside the open modal.
   *
   * @param title title text for the new work item
   */
  public void fillWorkItemTitle(String title) {
    type(MODAL_TITLE_INPUT, title);
  }

  /**
   * Submits the work item creation form.
   */
  public void submitWorkItemForm() {
    click(MODAL_SUBMIT);
  }

  /**
   * Returns the visible text labels of all work item cards currently on the board.
   *
   * @return list of work item title strings
   */
  public List<String> getWorkItemTitles() {
    return driver.findElements(WORK_ITEM_CARDS).stream()
      .map(WebElement::getText)
      .map(String::trim)
      .filter(t -> !t.isEmpty())
      .toList();
  }
}
