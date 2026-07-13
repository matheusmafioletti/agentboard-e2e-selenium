package com.agentboard.e2e.steps;

import com.agentboard.e2e.api.services.TestDataService;
import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.ProjectsPage;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for the project management screens.
 */
public class ProjectSteps {


  /**
   * Navigates to the projects listing page.
   */
  @Given("I am on the projects page")
  public void iAmOnTheProjectsPage() {
    ProjectsPage page = getOrCreateProjectsPage();
    page.navigate();
    ScenarioContext.set("projectsPage", page);
  }

  /**
   * Ensures at least one project exists (creates one via API if needed) and navigates to
   * the projects page.
   */
  @Given("I am on the projects page with at least one project")
  public void iAmOnTheProjectsPageWithAtLeastOneProject() {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);
    String projectId = ScenarioContext.get("currentProjectId", String.class);

    if (projectId == null && jwt != null) {
      projectId = TestDataService.INSTANCE.createProject(
          jwt, tenantId, "Pre-existing Project").id();
      ScenarioContext.set("currentProjectId", projectId);
    }

    ProjectsPage page = getOrCreateProjectsPage();
    page.navigate();
    ScenarioContext.set("projectsPage", page);
  }

  /**
   * Creates two named projects via API and navigates to the projects page.
   *
   * @param project1 first project name
   * @param project2 second project name
   */
  @Given("I have 2 projects {string} and {string}")
  public void iHave2Projects(String project1, String project2) {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);

    TestDataService.INSTANCE.createProject(jwt, tenantId, project1);
    TestDataService.INSTANCE.createProject(jwt, tenantId, project2);

    ScenarioContext.set("project1Name", project1);
    ScenarioContext.set("project2Name", project2);

    ProjectsPage page = getOrCreateProjectsPage();
    page.navigate();
    ScenarioContext.set("projectsPage", page);
  }


  /**
   * Creates a new project with the given name via the UI.
   *
   * @param name project display name
   */
  @When("I create a project named {string}")
  public void iCreateAProjectNamed(String name) {
    getOrCreateProjectsPage().createProject(name);
    ScenarioContext.set("lastCreatedProject", name);
  }

  /**
   * Clicks on the first project in the list.
   */
  @When("I click on the first project")
  public void iClickOnTheFirstProject() {
    ProjectsPage page = getOrCreateProjectsPage();
    java.util.List<String> names = page.getProjectNames();
    assertFalse(names.isEmpty(), "Expected at least one project to exist");
    page.clickProject(names.get(0));
    ScenarioContext.set("clickedProjectName", names.get(0));
  }

  /**
   * Selects the project with the given name via the project selector (sidebar or dropdown).
   *
   * @param projectName project to select
   */
  @When("I select {string} via the project selector")
  public void iSelectViaTheProjectSelector(String projectName) {
    WebDriver driver = ScenarioContext.getDriver();
    Environment env = ScenarioContext.get("env", Environment.class);
    org.openqa.selenium.By selectorButton = org.openqa.selenium.By.cssSelector(
        "[data-testid='project-selector'], [data-testid='active-project'], "
        + "[class*='project-selector']");
    try {
      driver.findElement(selectorButton).click();
      org.openqa.selenium.By option = org.openqa.selenium.By.xpath(
          "//*[@role='option' or contains(@class,'project-option')]"
          + "[contains(normalize-space(.), '" + projectName + "')]");
      driver.findElement(option).click();
    } catch (Exception e) {
      ProjectsPage page = getOrCreateProjectsPage();
      page.clickProject(projectName);
    }
    ScenarioContext.set("selectedProject", projectName);
  }


  /**
   * Asserts the newly created project appears in the project list.
   *
   * @param name expected project name
   */
  @Then("the project {string} should appear in the list")
  public void theProjectShouldAppearInTheList(String name) {
    ProjectsPage page = getOrCreateProjectsPage();
    assertTrue(page.isProjectVisible(name),
        "Expected project '" + name + "' to be visible in the projects list");
  }

  /**
   * Asserts the browser navigated to the project detail page.
   */
  @Then("I should be on the project detail page")
  public void iShouldBeOnTheProjectDetailPage() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(
        driver.getCurrentUrl().contains("/projetos/")
        || driver.getCurrentUrl().contains("/projects/"),
        "Expected to be on a project detail page but URL was: " + driver.getCurrentUrl());
  }

  /**
   * Asserts the sidebar shows the expected active project.
   *
   * @param projectName expected project name
   */
  @Then("the active project shown in sidebar should be {string}")
  public void theActiveProjectShownInSidebarShouldBe(String projectName) {
    WebDriver driver = ScenarioContext.getDriver();
    org.openqa.selenium.By sidebarProject = org.openqa.selenium.By.cssSelector(
        "[data-testid='active-project-name'], [class*='active-project'], "
        + "[class*='current-project']");
    try {
      String actual = driver.findElement(sidebarProject).getText().trim();
      assertTrue(actual.contains(projectName) || projectName.contains(actual),
          "Expected active project '" + projectName + "' but sidebar shows: '" + actual + "'");
    } catch (Exception e) {
      assertTrue(driver.getPageSource().contains(projectName),
          "Expected project name '" + projectName + "' to appear on the page");
    }
  }


  private ProjectsPage getOrCreateProjectsPage() {
    ProjectsPage page = ScenarioContext.get("projectsPage", ProjectsPage.class);
    if (page == null) {
      WebDriver driver = ScenarioContext.getDriver();
      Environment env = ScenarioContext.get("env", Environment.class);
      page = new ProjectsPage(driver, env);
      ScenarioContext.set("projectsPage", page);
    }
    return page;
  }
}
