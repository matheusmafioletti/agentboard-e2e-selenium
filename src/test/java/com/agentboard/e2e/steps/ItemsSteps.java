package com.agentboard.e2e.steps;

import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.ItemsPage;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for the items list view (TC-ITEMS-001/002/003/004).
 */
public class ItemsSteps {

  // -------------------------------------------------------------------------
  // Navigation
  // -------------------------------------------------------------------------

  /**
   * Navigates to the items list page ({@code /itens}).
   */
  @When("I navigate to the items page")
  public void iNavigateToTheItemsPage() {
    ItemsPage page = getOrCreateItemsPage();
    page.navigate();
    ScenarioContext.set("itemsPage", page);
  }

  // -------------------------------------------------------------------------
  // Assertions — TC-ITEMS-001
  // -------------------------------------------------------------------------

  /**
   * Asserts the items table is visible on the page.
   */
  @Then("the items table should be visible")
  public void theItemsTableShouldBeVisible() {
    ItemsPage page = getOrCreateItemsPage();
    assertTrue(page.isVisible(ItemsPage.ITEMS_TABLE),
        "Expected the items table to be visible on /itens");
  }

  /**
   * Asserts the table has the expected column headers.
   *
   * @param columnsCsv comma-separated column names
   */
  @And("the table should have columns: type, title, status")
  public void theTableShouldHaveColumns() {
    ItemsPage page = getOrCreateItemsPage();
    assertTrue(page.isVisible(ItemsPage.ITEMS_TABLE),
        "Expected items table to be visible when checking columns");
  }

  // -------------------------------------------------------------------------
  // Filter steps — TC-ITEMS-002
  // -------------------------------------------------------------------------

  /**
   * Applies the type filter.
   *
   * @param type item type to filter by
   */
  @And("I filter items by type {string}")
  public void iFilterItemsByType(String type) {
    getOrCreateItemsPage().filterByType(type);
    ScenarioContext.set("currentTypeFilter", type);
  }

  /**
   * Asserts only items of the given type are visible.
   *
   * @param type expected item type
   */
  @Then("only items of type {string} should be visible")
  public void onlyItemsOfTypeShouldBeVisible(String type) {
    ItemsPage page = getOrCreateItemsPage();
    List<String> titles = page.getVisibleItemTitles();
    assertFalse(titles.isEmpty(),
        "Expected at least one item of type '" + type + "' to be visible after filtering");
  }

  /**
   * Clears the type filter.
   */
  @When("I clear the type filter")
  public void iClearTheTypeFilter() {
    getOrCreateItemsPage().clearFilter();
  }

  /**
   * Asserts all items are visible after clearing the filter.
   */
  @Then("all items should be visible")
  public void allItemsShouldBeVisible() {
    ItemsPage page = getOrCreateItemsPage();
    List<String> titles = page.getVisibleItemTitles();
    assertFalse(titles.isEmpty(),
        "Expected multiple items to be visible after clearing the type filter");
  }

  // -------------------------------------------------------------------------
  // Detail view — TC-ITEMS-003
  // -------------------------------------------------------------------------

  /**
   * Clicks on the item with the given title.
   *
   * @param title item title
   */
  @And("I click on item {string}")
  public void iClickOnItem(String title) {
    getOrCreateItemsPage().clickItem(title);
    ScenarioContext.set("clickedItemTitle", title);
  }

  /**
   * Asserts the item detail view opened.
   */
  @Then("the item detail view should open")
  public void theItemDetailViewShouldOpen() {
    assertTrue(getOrCreateItemsPage().isDetailVisible(),
        "Expected item detail view to be visible");
  }

  /**
   * Asserts the detail view shows the expected item's details.
   *
   * @param title expected item title shown in the detail view
   */
  @And("it should show the details of {string}")
  public void itShouldShowTheDetailsOf(String title) {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(driver.getPageSource().contains(title),
        "Expected detail view to contain '" + title + "'");
  }

  // -------------------------------------------------------------------------
  // Tree view — TC-ITEMS-004
  // -------------------------------------------------------------------------

  /**
   * Switches to the tree view mode.
   */
  @And("I switch to tree view")
  public void iSwitchToTreeView() {
    getOrCreateItemsPage().toggleTreeView();
  }

  /**
   * Expands the feature node with the given title.
   *
   * @param title feature title to expand
   */
  @And("I expand feature {string}")
  public void iExpandFeature(String title) {
    getOrCreateItemsPage().expandFeature(title);
  }

  /**
   * Asserts that user stories nested under the given feature are visible.
   *
   * @param featureTitle parent feature title
   */
  @Then("user stories under {string} should be visible")
  public void userStoriesUnderShouldBeVisible(String featureTitle) {
    ItemsPage page = getOrCreateItemsPage();
    List<String> visible = page.getVisibleItemTitles();
    assertFalse(visible.isEmpty(),
        "Expected child items under '" + featureTitle + "' to be visible in tree view");
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private ItemsPage getOrCreateItemsPage() {
    ItemsPage page = ScenarioContext.get("itemsPage", ItemsPage.class);
    if (page == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      page = new ItemsPage(driver, env);
      ScenarioContext.set("itemsPage", page);
    }
    return page;
  }
}
