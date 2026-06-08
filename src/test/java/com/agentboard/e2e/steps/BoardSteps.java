package com.agentboard.e2e.steps;

import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.BoardPage;
import com.agentboard.e2e.pages.LoginPage;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for Kanban board interactions.
 */
public class BoardSteps {

  private BoardPage boardPage;

  /**
   * Logs in with the given email using the default test password and waits for the board.
   *
   * @param email registered user email
   */
  @Given("I am logged in as {string}")
  public void iAmLoggedInAs(String email) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);

    LoginPage loginPage = new LoginPage(driver, env);
    loginPage.navigate();
    loginPage.login(email, "secret123");

    assertTrue(loginPage.isLoginSuccessful(),
      "Pre-condition failed: could not log in as " + email);

    boardPage = new BoardPage(driver, env);
  }

  /**
   * Asserts that the board page is currently loaded.
   */
  @And("I am on the board page")
  public void iAmOnTheBoardPage() {
    if (boardPage == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      boardPage = new BoardPage(driver, env);
    }
    assertTrue(boardPage.isLoaded(), "Expected the Kanban board to be visible");
  }

  /**
   * Creates a new work item with the given title via the board's creation modal.
   *
   * @param title work item title
   */
  @When("I create a work item titled {string}")
  public void iCreateAWorkItemTitled(String title) {
    boardPage.openCreateWorkItemModal();
    boardPage.fillWorkItemTitle(title);
    boardPage.submitWorkItemForm();
  }

  /**
   * Asserts that a work item card with the given title is visible on the board.
   *
   * @param title expected work item title
   */
  @Then("the work item {string} should appear on the board")
  public void theWorkItemShouldAppearOnTheBoard(String title) {
    assertTrue(
      boardPage.getWorkItemTitles().stream().anyMatch(t -> t.contains(title)),
      "Expected work item '" + title + "' to be visible on the board"
    );
  }

  /**
   * Asserts the number of items in a named board column.
   *
   * @param expectedCount expected card count
   * @param status        column status label (e.g. {@code "TODO"})
   */
  @And("the board should have {int} item(s) in {string} column")
  public void theBoardShouldHaveItemsInColumn(int expectedCount, String status) {
    int actual = boardPage.getColumnCardCount(status);
    assertEquals(
      expectedCount,
      actual,
      "Expected " + expectedCount + " item(s) in column '" + status + "' but found " + actual
    );
  }
}
