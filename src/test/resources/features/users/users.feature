@e2e @users
Feature: User Management

  # TC-USERS-001
  Scenario: Admin can access user management page
    Given I am authenticated as an ADMIN
    When I navigate to the users page
    Then the members list should be visible
    And member details should include email and role

  # TC-USERS-002
  Scenario: USER role cannot access user management
    Given I am authenticated as a USER (non-admin)
    When I navigate directly to "/usuarios"
    Then access should be blocked
    And the "Usuários" link should not be visible in the sidebar

  # TC-USERS-003
  Scenario: Admin creates an invite
    Given I am authenticated as an ADMIN on the users page
    When I create an invite for "newmember@test.com"
    Then "newmember@test.com" should appear in the pending invites list

  # TC-USERS-004
  Scenario: Admin cancels a pending invite
    Given I am authenticated as an ADMIN with a pending invite for "cancel@test.com"
    When I cancel the invite for "cancel@test.com"
    Then "cancel@test.com" should not appear in the pending invites list

  # TC-USERS-005
  Scenario: New user accepts invite via token
    Given an invite token exists for "invited@test.com" to workspace "Invite WS"
    When I open the invite URL with the token
    And I complete registration with name "Invited User" and password "Abc12345!"
    Then I should be authenticated in workspace "Invite WS"
    And my role should be USER

  # TC-USERS-006
  Scenario: Invalid invite token shows error
    When I open the invite URL with token "invalid-token-xyz"
    Then an error message should be displayed
    And the registration form should not be visible
