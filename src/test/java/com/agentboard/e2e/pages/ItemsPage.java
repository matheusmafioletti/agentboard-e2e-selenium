package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page Object for the AgentBoard items list view ({@code /itens}).
 */
public class ItemsPage extends BasePage {

  /** Main items table element. */
  public static final By ITEMS_TABLE = By.cssSelector(
      "[data-testid='items-table'], table, [class*='items-table'], "
      + "[class*='item-list'], [class*='items-list']");

  /** Type filter dropdown or select element. */
  public static final By TYPE_FILTER = By.cssSelector(
      "[data-testid='type-filter'], select[name='type'], "
      + "[class*='type-filter'], button[aria-label*='type' i], "
      + "[data-testid='filter-type']");

  /** Toggle button that switches between list and tree views. */
  public static final By TREE_VIEW_TOGGLE = By.cssSelector(
      "[data-testid='tree-view-toggle'], button[aria-label*='tree' i], "
      + "button[aria-label*='árvore' i], [class*='tree-toggle'], "
      + "[data-testid='toggle-tree']");

  /** Individual rows in the items table or list. */
  public static final By ITEM_ROWS = By.cssSelector(
      "[data-testid='item-row'], tr[data-testid], tbody tr, "
      + "[class*='item-row'], [class*='items-row']");

  /** Item detail panel or modal shown when an item is clicked. */
  private static final By ITEM_DETAIL = By.cssSelector(
      "[data-testid='item-detail'], [data-testid='work-item-detail'], "
      + "[class*='item-detail'], [class*='detail-panel'], [role='dialog']");

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration
   */
  public ItemsPage(WebDriver driver, Environment env) {
    super(driver, env);
  }

  /**
   * Navigates to the items list page ({@code /itens}).
   */
  public void navigate() {
    navigate("/itens");
  }

  /**
   * Selects the given type in the type filter control.
   *
   * @param type item type to filter by (e.g. {@code FEATURE}, {@code USER_STORY}, {@code TASK})
   */
  public void filterByType(String type) {
    click(TYPE_FILTER);
    By option = By.xpath(
        "//*[@role='option' or @data-testid='filter-option' or self::option]"
        + "[contains(normalize-space(.), '" + type + "')]");
    click(option);
  }

  /**
   * Clears the active type filter by selecting the "All" or default option.
   */
  public void clearFilter() {
    try {
      click(TYPE_FILTER);
      By clearOption = By.xpath(
          "//*[@role='option' or self::option]"
          + "[contains(normalize-space(.), 'All') or contains(normalize-space(.), 'Todos') "
          + "or normalize-space(.)='']");
      click(clearOption);
    } catch (Exception e) {
      By clearButton = By.cssSelector(
          "[data-testid='clear-filter'], button[aria-label*='clear' i]");
      if (isVisible(clearButton)) {
        click(clearButton);
      }
    }
  }

  /**
   * Clicks the item row whose title text contains {@code title}.
   *
   * @param title partial or full title of the work item
   */
  public void clickItem(String title) {
    By itemRow = By.xpath(
        "//*[@data-testid='item-row' or contains(@class,'item-row') or self::tr]"
        + "[contains(normalize-space(.), '" + title + "')]");
    click(itemRow);
  }

  /**
   * Returns the visible title text of all rows in the items list.
   *
   * @return list of item title strings
   */
  public List<String> getVisibleItemTitles() {
    try {
      List<WebElement> rows = driver.findElements(ITEM_ROWS);
      return rows.stream()
          .map(WebElement::getText)
          .map(String::trim)
          .filter(t -> !t.isEmpty())
          .toList();
    } catch (Exception e) {
      return List.of();
    }
  }

  /**
   * Clicks the tree-view toggle button.
   */
  public void toggleTreeView() {
    click(TREE_VIEW_TOGGLE);
  }

  /**
   * Expands the feature node with the given title in tree view.
   *
   * @param title feature title to expand
   */
  public void expandFeature(String title) {
    By expandButton = By.xpath(
        "//*[contains(normalize-space(.), '" + title + "')]"
        + "//*[@data-testid='expand-button' or @aria-label='expand' or @class[contains(.,'expand')]]"
        + " | //*[@data-testid='expand-button' or contains(@class,'expand')]"
        + "[ancestor::*[contains(normalize-space(.), '" + title + "')]]");
    try {
      click(expandButton);
    } catch (Exception e) {
      By featureRow = By.xpath(
          "//*[contains(@class,'feature') or @data-type='FEATURE']"
          + "[contains(normalize-space(.), '" + title + "')]");
      click(featureRow);
    }
  }

  /**
   * Returns {@code true} when the item detail panel or modal is visible.
   *
   * @return detail visibility status
   */
  public boolean isDetailVisible() {
    return isVisible(ITEM_DETAIL);
  }
}
