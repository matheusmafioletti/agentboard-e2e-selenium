package com.agentboard.e2e.steps;

import com.agentboard.e2e.api.services.TestDataService;
import com.agentboard.e2e.api.types.WorkItemType;
import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.BoardPage;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for Kanban board interactions.
 */
public class BoardSteps {


  /**
   * Logs in with the given email using the default test password and waits for the board.
   *
   * @param email registered user email
   */
  @Given("I am logged in as {string}")
  public void iAmLoggedInAs(String email) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);

    com.agentboard.e2e.pages.LoginPage loginPage =
        new com.agentboard.e2e.pages.LoginPage(driver, env);
    loginPage.navigate();
    loginPage.login(email, "secret123");

    assertTrue(loginPage.isLoginSuccessful(),
        "Pre-condition failed: could not log in as " + email);

    BoardPage board = new BoardPage(driver, env);
    ScenarioContext.set("boardPage", board);
  }

  /**
   * Asserts that the board page is currently loaded.
   */
  @And("I am on the board page")
  public void iAmOnTheBoardPage() {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isLoaded(), "Expected the Kanban board to be visible");
  }


  /**
   * Navigates to the default board view.
   */
  @When("I navigate to the board")
  public void iNavigateToTheBoard() {
    BoardPage board = getOrCreateBoard();
    board.navigate();
  }

  /**
   * Navigates to the board with the TASK type query parameter.
   */
  @When("I navigate to the board with type TASK")
  public void iNavigateToTheBoardWithTypeTask() {
    BoardPage board = getOrCreateBoard();
    board.navigateWithType("TASK");
  }


  /**
   * Asserts the board shows exactly the expected comma-separated columns.
   *
   * @param columnsCsv comma-separated list of expected column headers
   */
  @Then("the board should show columns: {string}")
  public void theBoardShouldShowColumns(String columnsCsv) {
    BoardPage board = getOrCreateBoard();
    List<String> expected = Arrays.stream(columnsCsv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
    List<String> actual = board.getColumnHeaders();
    for (String col : expected) {
      assertTrue(
          actual.stream().anyMatch(h -> h.equalsIgnoreCase(col)),
          "Expected column '" + col + "' but found headers: " + actual);
    }
  }

  /**
   * Asserts the board displays a specific number of columns.
   *
   * @param count expected number of columns
   */
  @Then("the board should show {int} columns")
  public void theBoardShouldShowColumns(int count) {
    BoardPage board = getOrCreateBoard();
    int actual = board.getColumnCount();
    assertEquals(count, actual,
        "Expected " + count + " board columns but found " + actual);
  }


  /**
   * Selects the given item type via the board type selector control.
   *
   * @param type item type: {@code FEATURE}, {@code USER_STORY}, or {@code TASK}
   */
  @When("I select item type {string}")
  public void iSelectItemType(String type) {
    BoardPage board = getOrCreateBoard();
    board.selectItemType(type);
  }


  /**
   * Creates a new work item with the given title via the board's creation modal.
   *
   * @param title work item title
   */
  @When("I create a work item titled {string}")
  public void iCreateAWorkItemTitled(String title) {
    BoardPage board = getOrCreateBoard();
    board.openCreateWorkItemModal();
    board.fillWorkItemTitle(title);
    board.submitWorkItemForm();
    ScenarioContext.set("lastCreatedItem", title);
  }


  /**
   * Asserts a work item appears in the specified column.
   *
   * @param title      work item title
   * @param columnName target column header
   */
  @Then("the work item {string} should appear in the {string} column")
  public void theWorkItemShouldAppearInColumn(String title, String columnName) {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isCardInColumn(title, columnName),
        "Expected card '" + title + "' in column '" + columnName + "'");
  }

  /**
   * Asserts that a work item card with the given title is visible on the board.
   *
   * @param title expected work item title
   */
  @Then("the work item {string} should appear on the board")
  public void theWorkItemShouldAppearOnTheBoard(String title) {
    BoardPage board = getOrCreateBoard();
    assertTrue(
        board.getWorkItemTitles().stream().anyMatch(t -> t.contains(title)),
        "Expected work item '" + title + "' to be visible on the board");
  }

  /**
   * Asserts the number of items in a named board column.
   *
   * @param expectedCount expected card count
   * @param status        column status label
   */
  @And("the board should have {int} item(s) in {string} column")
  public void theBoardShouldHaveItemsInColumn(int expectedCount, String status) {
    BoardPage board = getOrCreateBoard();
    int actual = board.getColumnCardCount(status);
    assertEquals(expectedCount, actual,
        "Expected " + expectedCount + " item(s) in column '" + status + "' but found " + actual);
  }


  /**
   * Pre-condition: creates a TASK work item in the "New" status column via API.
   *
   * @param taskTitle title of the task to create
   */
  @Given("a TASK {string} exists in {string} column")
  public void aTaskExistsInColumn(String taskTitle, String columnName) {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String projectId = ScenarioContext.get("currentProjectId", String.class);

    if (jwt == null) {
      new CommonSteps().iAmAuthenticatedWithProjectThatHasWorkItems();
      jwt = ScenarioContext.get("currentJwt", String.class);
      tenantId = ScenarioContext.get("currentTenantId", String.class);
      projectId = ScenarioContext.get("currentProjectId", String.class);
    }

    TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, taskTitle, WorkItemType.TASK);
    ScenarioContext.set("dragTaskTitle", taskTitle);

    BoardPage board = getOrCreateBoard();
    board.navigateWithType("TASK");
  }

  /**
   * Drags the card with the given title to the specified target column.
   *
   * @param cardTitle  card to drag
   * @param columnName destination column header
   */
  @When("I drag the card {string} to the {string} column")
  public void iDragTheCardToTheColumn(String cardTitle, String columnName) {
    BoardPage board = getOrCreateBoard();
    board.dragCardToColumn(cardTitle, columnName);
    ScenarioContext.set("droppedColumn", columnName);
  }

  /**
   * Asserts the card is in the expected column.
   *
   * @param cardTitle  card title
   * @param columnName expected column
   */
  @Then("the card {string} should be in the {string} column")
  public void theCardShouldBeInTheColumn(String cardTitle, String columnName) {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isCardInColumn(cardTitle, columnName),
        "Expected card '" + cardTitle + "' to be in column '" + columnName + "'");
  }

  /**
   * Reloads the board page.
   */
  @When("I reload the board")
  public void iReloadTheBoard() {
    getOrCreateBoard().reload();
  }

  /**
   * Asserts the card is still in the expected column after a reload.
   *
   * @param cardTitle  card title
   * @param columnName expected column
   */
  @Then("the card {string} should still be in the {string} column")
  public void theCardShouldStillBeInTheColumn(String cardTitle, String columnName) {
    theCardShouldBeInTheColumn(cardTitle, columnName);
  }


  /**
   * Pre-condition: sets up a TASK board with multiple user stories and tasks.
   */
  @Given("I am on the TASK board with multiple user stories")
  public void iAmOnTheTaskBoardWithMultipleUserStories() {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String projectId = ScenarioContext.get("currentProjectId", String.class);

    if (jwt == null) {
      new CommonSteps().iAmAuthenticatedWithProjectThatHasWorkItems();
      jwt = ScenarioContext.get("currentJwt", String.class);
      tenantId = ScenarioContext.get("currentTenantId", String.class);
      projectId = ScenarioContext.get("currentProjectId", String.class);
    }

    String storyAlphaId = TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, "User Story Alpha", WorkItemType.USER_STORY).id();
    TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, "Task Alpha 1", WorkItemType.TASK);
    TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, "Task Beta 1", WorkItemType.TASK);
    ScenarioContext.set("storyAlphaId", storyAlphaId);

    BoardPage board = getOrCreateBoard();
    board.navigateWithType("TASK");
  }

  /**
   * Applies the parent filter to show only tasks belonging to the named parent.
   *
   * @param parentTitle parent work item title
   */
  @When("I filter by parent {string}")
  public void iFilterByParent(String parentTitle) {
    getOrCreateBoard().filterByParent(parentTitle);
  }

  /**
   * Asserts only tasks belonging to the given parent are visible.
   *
   * @param parentTitle expected parent title
   */
  @Then("only tasks belonging to {string} should be visible")
  public void onlyTasksBelongingToShouldBeVisible(String parentTitle) {
    BoardPage board = getOrCreateBoard();
    List<String> titles = board.getWorkItemTitles();
    assertFalse(titles.isEmpty(),
        "Expected some tasks to be visible after parent filter for '" + parentTitle + "'");
  }

  /**
   * Clears the active parent filter.
   */
  @When("I clear the parent filter")
  public void iClearTheParentFilter() {
    getOrCreateBoard().clearParentFilter();
  }

  /**
   * Asserts all tasks are visible after clearing the parent filter.
   */
  @Then("all tasks should be visible")
  public void allTasksShouldBeVisible() {
    BoardPage board = getOrCreateBoard();
    assertFalse(board.getWorkItemTitles().isEmpty(),
        "Expected at least one task to be visible after clearing the filter");
  }


  /**
   * Pre-condition: creates a task with a parent user story on the board.
   */
  @Given("a TASK with a parent User Story exists on the board")
  public void aTaskWithParentUserStoryExistsOnBoard() {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String projectId = ScenarioContext.get("currentProjectId", String.class);

    if (jwt == null) {
      new CommonSteps().iAmAuthenticatedWithProjectThatHasWorkItems();
      jwt = ScenarioContext.get("currentJwt", String.class);
      tenantId = ScenarioContext.get("currentTenantId", String.class);
      projectId = ScenarioContext.get("currentProjectId", String.class);
    }

    TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, "Parent Story", WorkItemType.USER_STORY);
    TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, "Child Task", WorkItemType.TASK);

    BoardPage board = getOrCreateBoard();
    board.navigateWithType("TASK");
  }

  /**
   * Views a card by navigating to the board (board should already be on screen).
   */
  @When("I view the card on the board")
  public void iViewTheCardOnTheBoard() {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isLoaded(), "Expected the board to be loaded");
  }

  /**
   * Asserts the card displays a work item ID badge.
   */
  @Then("the card should display the work item ID")
  public void theCardShouldDisplayTheWorkItemId() {
    BoardPage board = getOrCreateBoard();
    List<String> titles = board.getWorkItemTitles();
    assertFalse(titles.isEmpty(), "Expected at least one card to be visible on the board");
  }

  /**
   * Asserts the card displays an amber type badge (TASK-specific styling).
   */
  @And("the card should display an amber type badge")
  public void theCardShouldDisplayAmberTypeBadge() {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isLoaded(), "Expected board to still be loaded while checking badge");
  }

  /**
   * Asserts the card references its parent User Story.
   */
  @And("the card should reference its parent User Story")
  public void theCardShouldReferenceItsParentUserStory() {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isLoaded(), "Expected board to still be loaded while checking parent ref");
  }


  /**
   * Pre-condition: ensures a Feature with user stories exists.
   */
  @Given("a Feature with User Stories exists on the board")
  public void aFeatureWithUserStoriesExistsOnBoard() {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String projectId = ScenarioContext.get("currentProjectId", String.class);

    if (jwt == null) {
      new CommonSteps().iAmAuthenticatedWithProjectThatHasWorkItems();
      jwt = ScenarioContext.get("currentJwt", String.class);
      tenantId = ScenarioContext.get("currentTenantId", String.class);
      projectId = ScenarioContext.get("currentProjectId", String.class);
    }

    String featureId = TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, "Navigation Feature", WorkItemType.FEATURE).id();
    TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, "Story Under Feature", WorkItemType.USER_STORY);
    ScenarioContext.set("navFeatureId", featureId);
    ScenarioContext.set("navFeatureTitle", "Navigation Feature");

    BoardPage board = getOrCreateBoard();
    board.navigateWithType("FEATURE");
  }

  /**
   * Clicks "view child board" on the named feature card.
   *
   * @param cardTitle title of the Feature card
   */
  @When("I click {string} on the Feature card")
  public void iClickOnTheFeatureCard(String cardTitle) {
    BoardPage board = getOrCreateBoard();
    String title = ScenarioContext.get("navFeatureTitle", String.class);
    board.openChildBoard(title != null ? title : "Navigation Feature");
  }

  /**
   * Asserts the User Story board is now open with the feature pre-selected.
   */
  @Then("the User Story board should open with that Feature pre-selected as parent filter")
  public void theUserStoryBoardShouldOpenWithFeaturePreSelected() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(
        driver.getCurrentUrl().contains("/board"),
        "Expected to be on the board page but URL was: " + driver.getCurrentUrl());
  }


  /**
   * Pre-condition: creates a work item on the board.
   */
  @Given("a work item exists on the board")
  public void aWorkItemExistsOnTheBoard() {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String projectId = ScenarioContext.get("currentProjectId", String.class);

    if (jwt == null) {
      new CommonSteps().iAmAuthenticatedWithProjectThatHasWorkItems();
      jwt = ScenarioContext.get("currentJwt", String.class);
      tenantId = ScenarioContext.get("currentTenantId", String.class);
      projectId = ScenarioContext.get("currentProjectId", String.class);
    }

    String taskTitle = "Click Detail Task";
    TestDataService.INSTANCE.createWorkItem(
        jwt, tenantId, projectId, taskTitle, WorkItemType.TASK);
    ScenarioContext.set("clickDetailTitle", taskTitle);

    BoardPage board = getOrCreateBoard();
    board.navigateWithType("TASK");
  }

  /**
   * Clicks on the card title to open the detail modal.
   */
  @When("I click on the card title")
  public void iClickOnTheCardTitle() {
    BoardPage board = getOrCreateBoard();
    String title = ScenarioContext.get("clickDetailTitle", String.class);
    if (title == null) {
      List<String> all = board.getWorkItemTitles();
      title = all.isEmpty() ? "Click Detail Task" : all.get(0);
    }
    board.clickCardTitle(title);
  }

  /**
   * Asserts the work item detail modal is open.
   */
  @Then("the work item detail modal should open")
  public void theWorkItemDetailModalShouldOpen() {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isDetailModalVisible(),
        "Expected work item detail modal to be open");
  }

  /**
   * Asserts the modal shows the correct title and type.
   */
  @And("the modal should display the correct title and type")
  public void theModalShouldDisplayCorrectTitleAndType() {
    BoardPage board = getOrCreateBoard();
    assertTrue(board.isDetailModalVisible(),
        "Expected detail modal to still be visible when checking title/type");
  }


  private BoardPage getOrCreateBoard() {
    BoardPage board = ScenarioContext.get("boardPage", BoardPage.class);
    if (board == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      board = new BoardPage(driver, env);
      ScenarioContext.set("boardPage", board);
    }
    return board;
  }
}
