@e2e @auth
Feature: Authentication — Login
  As a registered user I want to log in to AgentBoard

  Background:
    Given I am on the login page

  # TC-AUTH-003
  Scenario: Single tenant login redirects directly to dashboard
    Given a user "alice@test.com" with password "Abc12345!" and workspace "Alice WS" exists
    When I enter email "alice@test.com" and password "Abc12345!"
    And I click the login button
    Then I should be redirected to the dashboard
    And the workspace "Alice WS" should be shown in the sidebar

  # TC-AUTH-004
  Scenario: Multi-tenant login shows workspace picker
    Given a user "bob@test.com" with password "Abc12345!" belongs to 2 workspaces
    When I enter email "bob@test.com" and password "Abc12345!"
    And I click the login button
    Then the workspace selection screen should be displayed
    When I select workspace "bob WS 1"
    Then I should be authenticated in workspace "bob WS 1"

  # TC-AUTH-005
  Scenario: Invalid credentials shows generic error
    When I enter email "unknown@test.com" and password "wrongpass"
    And I click the login button
    Then I should see a login error message

  # TC-AUTH-006
  Scenario: Protected route redirects unauthenticated user to login
    Given I am not authenticated
    When I navigate directly to "/board"
    Then I should be redirected to login

  # TC-AUTH-007
  Scenario: Logout ends session and protects routes
    Given I am authenticated as "alice@test.com"
    When I logout
    Then I should be on the login page
    When I navigate directly to "/inicio"
    Then I should be redirected to login
