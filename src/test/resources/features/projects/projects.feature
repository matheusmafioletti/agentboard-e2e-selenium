@e2e @projects
Feature: Project Management

  @staging
  Scenario: Projects page lists seed project without creating via UI
    Given I am authenticated as staging smoke admin
    When I navigate to the projects page
    Then the staging smoke project should be visible in the list

  @local
  Scenario: Create a new project
    Given I am authenticated as a regular admin user
    And I am on the projects page
    When I create a project named "E2E Project Alpha"
    Then the project "E2E Project Alpha" should appear in the list

  @local
  Scenario: Navigate to project detail
    Given I am authenticated as a regular admin user
    And I am on the projects page with at least one project
    When I click on the first project
    Then I should be on the project detail page

  @local
  Scenario: Select active project via sidebar selector
    Given I am authenticated as a regular admin user
    And I have 2 projects "Project A" and "Project B"
    When I select "Project B" via the project selector
    Then the active project shown in sidebar should be "Project B"
