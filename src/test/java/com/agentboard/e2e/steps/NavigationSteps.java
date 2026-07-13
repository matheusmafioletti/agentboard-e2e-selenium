package com.agentboard.e2e.steps;

import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.DashboardPage;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for navigation and sidebar validation.
 */
public class NavigationSteps {


  /**
   * Navigates to the dashboard so the sidebar is visible.
   */
  @When("I view the sidebar")
  public void iViewTheSidebar() {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    DashboardPage dashboard = new DashboardPage(driver, env);
    if (!driver.getCurrentUrl().contains("/inicio")) {
      dashboard.navigate();
    }
    ScenarioContext.set("dashboardPage", dashboard);
  }

  /**
   * Asserts the sidebar contains a navigation link with the given text.
   *
   * @param linkText expected link text (partial match, case-insensitive)
   */
  @Then("the sidebar should contain link {string}")
  public void theSidebarShouldContainLink(String linkText) {
    DashboardPage dashboard = getDashboard();
    assertTrue(dashboard.isNavLinkVisible(linkText),
        "Expected sidebar to contain a link with text '" + linkText + "'");
  }

  /**
   * Asserts the sidebar does not contain a navigation link with the given text.
   *
   * @param linkText link text that should be absent
   */
  @Then("the sidebar should not contain link {string}")
  public void theSidebarShouldNotContainLink(String linkText) {
    DashboardPage dashboard = getDashboard();
    assertFalse(dashboard.isNavLinkVisible(linkText),
        "Expected sidebar to NOT contain link '" + linkText + "' for USER role");
  }


  /**
   * Navigates to the dashboard page.
   */
  @When("I navigate to the dashboard")
  public void iNavigateToTheDashboard() {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    DashboardPage dashboard = new DashboardPage(driver, env);
    dashboard.navigate();
    ScenarioContext.set("dashboardPage", dashboard);
  }

  /**
   * Asserts that at least one summary card with a numeric counter is visible.
   */
  @Then("summary cards should be visible with item counts")
  public void summaryCardsShouldBeVisibleWithItemCounts() {
    DashboardPage dashboard = getDashboard();
    int count = dashboard.getSummaryCardCount();
    assertTrue(count > 0,
        "Expected at least one summary card to be visible on the dashboard but found " + count);
  }


  private DashboardPage getDashboard() {
    DashboardPage dashboard = ScenarioContext.get("dashboardPage", DashboardPage.class);
    if (dashboard == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      dashboard = new DashboardPage(driver, env);
      ScenarioContext.set("dashboardPage", dashboard);
    }
    return dashboard;
  }
}
