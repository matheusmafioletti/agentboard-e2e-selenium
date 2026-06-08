@e2e @auth
Feature: User Registration
  As a new user
  I want to create an account and workspace in AgentBoard
  So that I can start managing my projects

  Scenario: Successful registration with valid data
    Given I am on the registration page
    When I register with name "Alice", email "alice@test.com", password "secret123", and workspace "Alice's Team"
    Then I should be registered and redirected to the board

  Scenario: Registration fails when email is already taken
    Given I am on the registration page
    When I register with name "Bob", email "alice@test.com", password "secret123", and workspace "Bob's Team"
    Then I should see a registration error message
