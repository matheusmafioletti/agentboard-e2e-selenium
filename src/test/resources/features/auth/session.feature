@e2e @auth
Feature: Session Management

  # TC-AUTH-008
  @wip
  Scenario: Switch workspace without logout
    Given I am authenticated as a user with 2 workspaces "WS A" and "WS B"
    And I am on the dashboard showing "WS A"
    When I switch to workspace "WS B" via sidebar
    Then the active workspace should be "WS B"
