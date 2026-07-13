@e2e @items
Feature: Items List View

  @staging
  Scenario: Items list shows all work items
    Given I am authenticated as staging smoke admin
    When I navigate to the items page
    Then the items table should be visible
    And the table should have columns: type, title, status

  @local
  Scenario Outline: Filter items by type
    Given I am authenticated with a project containing Features, User Stories and Tasks
    When I navigate to the items page
    And I filter items by type "<type>"
    Then only items of type "<type>" should be visible
    When I clear the type filter
    Then all items should be visible
    Examples:
      | type       |
      | FEATURE    |
      | USER_STORY |
      | TASK       |

  @local
  Scenario: Click item opens detail view
    Given I am authenticated with a project containing Features, User Stories and Tasks
    When I navigate to the items page
    And I click on item "Detail Test Item"
    Then the item detail view should open
    And it should show the details of "Detail Test Item"

  @local
  Scenario: Tree view expands Feature hierarchy
    Given I am authenticated with a project containing Features, User Stories and Tasks
    When I navigate to the items page
    And I switch to tree view
    And I expand feature "Feature Root"
    Then user stories under "Feature Root" should be visible
