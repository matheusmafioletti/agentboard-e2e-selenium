@e2e @board
Feature: Kanban Board

  @staging
  Scenario: Default board shows TASK columns
    Given I am authenticated as staging smoke admin
    When I navigate to the board
    Then the board should show columns: "New, Active, Closed"

  @local
  Scenario Outline: Switch item type changes columns
    Given I am authenticated with a project that has work items
    When I navigate to the board
    And I select item type "<type>"
    Then the board should show <count> columns
    Examples:
      | type       | count |
      | FEATURE    | 9     |
      | USER_STORY | 5     |
      | TASK       | 3     |

  @local
  Scenario: Create work item appears in initial column
    Given I am authenticated with a project that has work items
    When I navigate to the board with type TASK
    And I create a work item titled "My new task"
    Then the work item "My new task" should appear in the "New" column

  @local
  Scenario: Drag card between columns persists status change
    Given I am authenticated with a project that has work items
    And a TASK "Drag Me" exists in "New" column
    When I drag the card "Drag Me" to the "Active" column
    Then the card "Drag Me" should be in the "Active" column
    When I reload the board
    Then the card "Drag Me" should still be in the "Active" column

  @local
  Scenario: Filter by parent shows only child items
    Given I am on the TASK board with multiple user stories
    When I filter by parent "User Story Alpha"
    Then only tasks belonging to "User Story Alpha" should be visible
    When I clear the parent filter
    Then all tasks should be visible

  @local
  Scenario: Card displays ID, type badge and parent reference
    Given a TASK with a parent User Story exists on the board
    When I view the card on the board
    Then the card should display the work item ID
    And the card should display an amber type badge
    And the card should reference its parent User Story

  @local
  Scenario: Open child board from Feature card
    Given a Feature with User Stories exists on the board
    When I click "view child board" on the Feature card
    Then the User Story board should open with that Feature pre-selected as parent filter

  @local
  Scenario: Click on card opens detail modal
    Given a work item exists on the board
    When I click on the card title
    Then the work item detail modal should open
    And the modal should display the correct title and type
