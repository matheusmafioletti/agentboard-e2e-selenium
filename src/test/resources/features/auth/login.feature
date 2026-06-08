@e2e @auth
Feature: User Login
  As a registered user
  I want to log in to AgentBoard
  So that I can access my projects

  Background:
    Given I am on the login page

  Scenario: Successful login with valid credentials
    When I enter email "alice@test.com" and password "secret123"
    And I click the login button
    Then I should be redirected to the board

  Scenario: Login fails with wrong password
    When I enter email "alice@test.com" and password "wrongpass"
    And I click the login button
    Then I should see an error message "Invalid credentials"

  @wip
  Scenario: Multi-tenant user selects workspace
    Given I am a user with multiple workspaces
    When I enter valid credentials
    And I click the login button
    Then I should see the workspace selection screen
