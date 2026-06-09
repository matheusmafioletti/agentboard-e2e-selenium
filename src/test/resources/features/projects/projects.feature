@e2e @projects
Feature: Project Management

  Background:
    Given I am authenticated as a regular admin user

  # TC-PROJ-001
  Scenario: Create a new project
    Given I am on the projects page
    When I create a project named "E2E Project Alpha"
    Then the project "E2E Project Alpha" should appear in the list

  # TC-PROJ-002
  Scenario: Navigate to project detail
    Given I am on the projects page with at least one project
    When I click on the first project
    Then I should be on the project detail page

  # TC-PROJ-003
  Scenario: Select active project via sidebar selector
    Given I have 2 projects "Project A" and "Project B"
    When I select "Project B" via the project selector
    Then the active project shown in sidebar should be "Project B"
