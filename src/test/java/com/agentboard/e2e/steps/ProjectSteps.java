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

  @Given("I am on the projects page")
  public void iAmOnTheProjectsPage() {
    ProjectsPage page = getOrCreateProjectsPage();
    page.navigate();
    ScenarioContext.set("projectsPage", page);
  }

  @When("I navigate to the projects page")
  public void iNavigateToTheProjectsPage() {
    iAmOnTheProjectsPage();
  }

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

    iAmOnTheProjectsPage();
  }

  @Given("I have 2 projects {string} and {string}")
  public void iHave2Projects(String project1, String project2) {
    String jwt = ScenarioContext.get("currentJwt", String.class);
    String tenantId = ScenarioContext.get("currentTenantId", String.class);

    TestDataService.INSTANCE.createProject(jwt, tenantId, project1);
    TestDataService.INSTANCE.createProject(jwt, tenantId, project2);

    ScenarioContext.set("project1Name", project1);
    ScenarioContext.set("project2Name", project2);
    iAmOnTheProjectsPage();
  }

  @When("I create a project named {string}")
  public void iCreateAProjectNamed(String name) {
    getOrCreateProjectsPage().createProject(name);
    ScenarioContext.set("lastCreatedProject", name);
  }

  @When("I click on the first project")
  public void iClickOnTheFirstProject() {
    ProjectsPage page = getOrCreateProjectsPage();
    java.util.List<String> names = page.getProjectNames();
    assertFalse(names.isEmpty(), "Expected at least one project to exist");
    page.clickProject(names.get(0));
    ScenarioContext.set("clickedProjectName", names.get(0));
  }

  @When("I select {string} via the project selector")
  public void iSelectViaTheProjectSelector(String projectName) {
    WebDriver driver = ScenarioContext.getDriver();
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
      getOrCreateProjectsPage().clickProject(projectName);
    }
    ScenarioContext.set("selectedProject", projectName);
  }

  @Then("the staging smoke project should be visible in the list")
  public void theStagingSmokeProjectShouldBeVisibleInTheList() {
    String projectName = System.getenv().getOrDefault(
        "E2E_STAGING_PROJECT_NAME", "E2E Smoke Project");
    ProjectsPage page = getOrCreateProjectsPage();
    assertTrue(page.isProjectVisible(projectName),
        "Expected seed project '" + projectName + "' to be visible in the projects list");
  }

  @Then("the project {string} should appear in the list")
  public void theProjectShouldAppearInTheList(String name) {
    ProjectsPage page = getOrCreateProjectsPage();
    assertTrue(page.isProjectVisible(name),
        "Expected project '" + name + "' to be visible in the projects list");
  }

  @Then("I should be on the project detail page")
  public void iShouldBeOnTheProjectDetailPage() {
    WebDriver driver = ScenarioContext.getDriver();
    assertTrue(
        driver.getCurrentUrl().contains("/projetos/")
        || driver.getCurrentUrl().contains("/projects/"),
        "Expected to be on a project detail page but URL was: " + driver.getCurrentUrl());
  }

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
