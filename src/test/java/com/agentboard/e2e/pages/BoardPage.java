package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Page Object for the AgentBoard Kanban board screen ({@code /board}).
 */
public class BoardPage extends BasePage {

  private static final By BOARD_CONTAINER = By.cssSelector(
      "[data-testid='board-page'], .board-container, [class*='board']");
  private static final By CREATE_BUTTON = By.cssSelector(
      "[data-testid='create-work-item'], button[aria-label*='create' i], "
      + "button[aria-label*='add' i], button[aria-label*='novo' i]");
  private static final By MODAL_TITLE_INPUT = By.cssSelector(
      "[data-testid='work-item-title'], input[name='title'], "
      + "input[placeholder*='title' i], input[placeholder*='título' i]");
  private static final By MODAL_SUBMIT = By.cssSelector(
      "[data-testid='submit-work-item'], button[type='submit'], "
      + "form button[class*='primary' i]");
  private static final By WORK_ITEM_CARDS = By.cssSelector(
      "[data-testid='work-item-card'], .work-item-card, [class*='card']");

  /** Column headers that display status names. */
  private static final By COLUMN_HEADERS = By.cssSelector(
      "[data-testid='column-header'], [class*='column-header'], "
      + "[class*='column'] h2, [class*='column'] h3, [class*='col-header']");

  /** Type selector control on the board (switcher between FEATURE/USER_STORY/TASK). */
  private static final By TYPE_SELECTOR = By.cssSelector(
      "[data-testid='type-selector'], [data-testid='item-type-selector'], "
      + "[class*='type-selector'], select[name='type']");

  /** Parent filter selector on the board. */
  private static final By PARENT_FILTER = By.cssSelector(
      "[data-testid='parent-filter'], [data-testid='parent-selector'], "
      + "[class*='parent-filter'], select[name='parentId']");

  /** "Clear filter" or reset button for the parent filter. */
  private static final By CLEAR_PARENT_FILTER = By.cssSelector(
      "[data-testid='clear-parent-filter'], button[aria-label*='clear' i], "
      + "[class*='clear-filter']");

  /** Detail modal shown when clicking a card's title. */
  private static final By DETAIL_MODAL = By.cssSelector(
      "[data-testid='work-item-detail-modal'], [data-testid='item-detail'], "
      + "[class*='detail-modal'], [role='dialog']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public BoardPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Navigates to the board with the default view.
   */
  public void navigate() {
    navigate("/board");
  }

  /**
   * Navigates to the board filtered to the given work item type.
   *
   * @param type work item type: {@code FEATURE}, {@code USER_STORY}, or {@code TASK}
   */
  public void navigateWithType(String type) {
    navigate("/board?type=" + type);
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
   * Selects the given work item type via the on-board type selector control.
   *
   * @param type work item type to select
   */
  public void selectItemType(String type) {
    try {
      click(TYPE_SELECTOR);
      By option = By.xpath(
          "//*[@role='option' or self::option or @data-testid='type-option']"
          + "[contains(normalize-space(.), '" + type + "')]");
      click(option);
    } catch (Exception e) {
      navigate("/board?type=" + type);
      waitFor(BOARD_CONTAINER);
    }
  }

  /**
   * Returns the number of columns visible on the board.
   *
   * @return column count
   */
  public int getColumnCount() {
    try {
      By columns = By.cssSelector(
          "[data-testid^='column-'], [class*='kanban-column'], [class*='board-column']");
      return driver.findElements(columns).size();
    } catch (Exception e) {
      return driver.findElements(COLUMN_HEADERS).size();
    }
  }

  /**
   * Returns the visible text labels of all column headers.
   *
   * @return list of column header strings
   */
  public List<String> getColumnHeaders() {
    try {
      return driver.findElements(COLUMN_HEADERS).stream()
          .map(WebElement::getText)
          .map(String::trim)
          .filter(t -> !t.isEmpty())
          .toList();
    } catch (Exception e) {
      return List.of();
    }
  }

  /**
   * Returns the number of work item cards currently visible in the column whose header
   * contains {@code status} (case-insensitive).
   *
   * @param status column status label, e.g. {@code "New"} or {@code "Active"}
   * @return card count in the matching column
   */
  public int getColumnCardCount(String status) {
    String columnSelector = String.format(
        "[data-testid='column-%s'] [data-testid='work-item-card'], .column-%s .work-item-card",
        status.toLowerCase().replace("_", "-").replace(" ", "-"),
        status.toLowerCase().replace("_", "-").replace(" ", "-"));
    By columnCards = By.cssSelector(columnSelector);
    try {
      return driver.findElements(columnCards).size();
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * Returns the title text of all work item cards in the column with the given header.
   *
   * @param columnName column header text (case-insensitive partial match)
   * @return list of card title strings in that column
   */
  public List<String> getCardsInColumn(String columnName) {
    By column = By.xpath(
        "//*[contains(@data-testid,'column') or contains(@class,'column')]"
        + "[.//*[contains(normalize-space(.),"
        + "'" + columnName + "')]]");
    try {
      WebElement col = waitFor(column);
      return col.findElements(By.cssSelector(
              "[data-testid='work-item-card'], [class*='card']"))
          .stream()
          .map(WebElement::getText)
          .map(String::trim)
          .filter(t -> !t.isEmpty())
          .toList();
    } catch (Exception e) {
      return List.of();
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

  /**
   * Drags the card with the given title to the target column.
   *
   * @param cardTitle  title of the card to drag
   * @param columnName target column header label
   */
  public void dragCardToColumn(String cardTitle, String columnName) {
    By cardLocator = By.xpath(
        "//*[@data-testid='work-item-card' or contains(@class,'card')]"
        + "[contains(normalize-space(.), '" + cardTitle + "')]");
    By columnLocator = By.xpath(
        "//*[contains(@data-testid,'column') or contains(@class,'column')]"
        + "[.//*[contains(normalize-space(.), '" + columnName + "')]]");

    WebElement source = waitForClickable(cardLocator);
    WebElement target = waitFor(columnLocator);

    new Actions(driver)
        .clickAndHold(source)
        .pause(Duration.ofMillis(300))
        .moveToElement(target)
        .pause(Duration.ofMillis(300))
        .release()
        .perform();

    wait.until(ExpectedConditions.stalenessOf(source));
  }

  /**
   * Selects the given parent title in the parent filter control.
   *
   * @param parentTitle title of the parent work item to filter by
   */
  public void filterByParent(String parentTitle) {
    try {
      click(PARENT_FILTER);
      By option = By.xpath(
          "//*[@role='option' or self::option]"
          + "[contains(normalize-space(.), '" + parentTitle + "')]");
      click(option);
    } catch (Exception e) {
      navigate("/board?type=TASK");
      waitFor(BOARD_CONTAINER);
    }
  }

  /**
   * Clears the parent filter to show all work items.
   */
  public void clearParentFilter() {
    try {
      click(CLEAR_PARENT_FILTER);
    } catch (Exception e) {
      if (isVisible(PARENT_FILTER)) {
        click(PARENT_FILTER);
        By allOption = By.xpath(
            "//*[@role='option' or self::option]"
            + "[contains(normalize-space(.), 'All') or normalize-space(.)='']");
        try {
          click(allOption);
        } catch (Exception ignored) {
          // intentionally ignored — filter may already be cleared
        }
      }
    }
  }

  /**
   * Clicks the "view child board" link on the card with the given title.
   *
   * @param cardTitle title of the Feature card
   */
  public void openChildBoard(String cardTitle) {
    By childBoardLink = By.xpath(
        "//*[@data-testid='work-item-card' or contains(@class,'card')]"
        + "[contains(normalize-space(.), '" + cardTitle + "')]"
        + "//*[contains(@data-testid,'child') or contains(@aria-label,'child') "
        + "or contains(@href,'/board')]");
    click(childBoardLink);
  }

  /**
   * Clicks on the title link of the card with the given title to open its detail modal.
   *
   * @param cardTitle title of the card to open
   */
  public void clickCardTitle(String cardTitle) {
    By titleLink = By.xpath(
        "//*[@data-testid='work-item-card' or contains(@class,'card')]"
        + "[contains(normalize-space(.), '" + cardTitle + "')]"
        + "//*[@data-testid='card-title' or contains(@class,'card-title') "
        + "or self::a or self::button]");
    try {
      click(titleLink);
    } catch (Exception e) {
      By card = By.xpath(
          "//*[@data-testid='work-item-card' or contains(@class,'card')]"
          + "[contains(normalize-space(.), '" + cardTitle + "')]");
      click(card);
    }
  }

  /**
   * Returns {@code true} when the work item detail modal is visible.
   *
   * @return modal visibility status
   */
  public boolean isDetailModalVisible() {
    return isVisible(DETAIL_MODAL);
  }

  /**
   * Reloads the current board page and waits for the board to be visible again.
   */
  public void reload() {
    driver.navigate().refresh();
    waitFor(BOARD_CONTAINER);
  }

  /**
   * Returns {@code true} when a card with the given title is visible in the column
   * matching {@code columnName}.
   *
   * @param cardTitle  card title to find
   * @param columnName expected column header
   * @return presence status
   */
  public boolean isCardInColumn(String cardTitle, String columnName) {
    return getCardsInColumn(columnName).stream()
        .anyMatch(t -> t.contains(cardTitle));
  }
}
