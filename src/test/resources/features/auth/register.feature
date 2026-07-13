@e2e @auth
Feature: Authentication — Register
  As a new user I want to create an account with a workspace
  Scenario: Successful registration creates user and workspace
    Given I am on the registration page
    When I register with a unique email, password "Abc12345!" and workspace "My WS"
    Then I should be redirected to the dashboard
    And the workspace "My WS" should be shown in the sidebar
  Scenario: Registration with existing email shows error
    Given a user with email "existing@test.com" already exists
    And I am on the registration page
    When I try to register with email "existing@test.com" and password "Abc12345!"
    Then I should see a registration error
    And I should remain on the register page
