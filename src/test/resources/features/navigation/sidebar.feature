@e2e @navigation
Feature: Navigation and Sidebar

  # TC-NAV-001
  Scenario: Admin sidebar shows all navigation links
    Given I am authenticated as an ADMIN
    When I view the sidebar
    Then the sidebar should contain link "Usuários"
    And the sidebar should contain link "Board"
    And the sidebar should contain link "Itens"

  # TC-NAV-002
  Scenario: USER sidebar does not show Usuários link
    Given I am authenticated as a USER (non-admin)
    When I view the sidebar
    Then the sidebar should not contain link "Usuários"
    And the sidebar should contain link "Board"

  # TC-NAV-003
  Scenario: Dashboard shows project summary with counters
    Given I am authenticated with a project that has work items
    When I navigate to the dashboard
    Then summary cards should be visible with item counts
