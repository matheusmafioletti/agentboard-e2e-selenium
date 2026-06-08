@e2e @board
Feature: Kanban Board Flow
  As an authenticated user
  I want to manage work items on the board
  So that I can track my project progress

  Background:
    Given I am logged in as "alice@test.com"
    And I am on the board page

  Scenario: Create a new work item
    When I create a work item titled "Implement login feature"
    Then the work item "Implement login feature" should appear on the board
    And the board should have 1 item(s) in "TODO" column
