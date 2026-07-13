package com.agentboard.e2e.api.services;

import com.agentboard.e2e.api.clients.AuthApiClient;
import com.agentboard.e2e.api.clients.BoardApiClient;
import com.agentboard.e2e.api.types.InviteResult;
import com.agentboard.e2e.api.types.ProjectResult;
import com.agentboard.e2e.api.types.TenantResult;
import com.agentboard.e2e.api.types.UserCredentials;
import com.agentboard.e2e.api.types.WorkItemResult;
import com.agentboard.e2e.api.types.WorkItemType;
import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.support.ScenarioContext;
import org.aeonbits.owner.ConfigFactory;

/**
 * High-level test data workflows that hide HTTP client details from step definitions.
 */
public class TestDataService {

  /** Shared singleton used by Cucumber step classes. */
  public static final TestDataService INSTANCE = new TestDataService();

  private TestDataService() {}

  /**
   * Registers and logs in a new user, returning complete credentials.
   *
   * @param email      unique email address
   * @param password   plain-text password
   * @param tenantName workspace display name
   * @return authenticated user credentials
   */
  public UserCredentials createAuthenticatedUser(
      String email, String password, String tenantName) {
    return authClient().createAuthenticatedUser(email, password, tenantName);
  }

  /**
   * Creates a second tenant for an already-authenticated user.
   *
   * @param jwt        bearer token
   * @param tenantName display name for the new tenant
   * @return new tenant identifiers
   */
  public TenantResult createSecondTenant(String jwt, String tenantName) {
    return authClient().createTenant(jwt, tenantName);
  }

  /**
   * Creates an invite for the given email.
   *
   * @param jwt      bearer token of the inviting admin
   * @param tenantId tenant identifier
   * @param email    email address to invite
   * @return invite token and identifier
   */
  public InviteResult createInvite(String jwt, String tenantId, String email) {
    return authClient().createInvite(jwt, tenantId, email);
  }

  /**
   * Accepts an invite for the given email and password.
   *
   * @param token    invite token
   * @param email    invited user email
   * @param password invited user password
   */
  public void acceptInvite(String token, String email, String password) {
    authClient().acceptInvite(token, email, password);
  }

  /**
   * Cancels a pending invite.
   *
   * @param jwt      bearer token
   * @param tenantId tenant identifier
   * @param inviteId invite identifier
   */
  public void cancelInvite(String jwt, String tenantId, String inviteId) {
    authClient().cancelInvite(jwt, tenantId, inviteId);
  }

  /**
   * Creates a project in the given tenant.
   *
   * @param jwt      bearer token
   * @param tenantId tenant identifier
   * @param name     project display name
   * @return created project identifiers
   */
  public ProjectResult createProject(String jwt, String tenantId, String name) {
    return boardClient().createProject(jwt, tenantId, name);
  }

  /**
   * Creates a work item in the given project.
   *
   * @param jwt       bearer token
   * @param tenantId  tenant identifier
   * @param projectId parent project identifier
   * @param title     work item title
   * @param type      work item type
   * @return created work item identifier
   */
  public WorkItemResult createWorkItem(
      String jwt,
      String tenantId,
      String projectId,
      String title,
      WorkItemType type) {
    return boardClient().createWorkItem(jwt, tenantId, projectId, title, type);
  }

  /**
   * Creates a work item with an optional parent in the given project.
   *
   * @param jwt       bearer token
   * @param tenantId  tenant identifier
   * @param projectId parent project identifier
   * @param title     work item title
   * @param type      work item type
   * @param parentId  optional parent work item identifier
   * @return created work item identifier
   */
  public WorkItemResult createWorkItem(
      String jwt,
      String tenantId,
      String projectId,
      String title,
      WorkItemType type,
      String parentId) {
    return boardClient().createWorkItem(jwt, tenantId, projectId, title, type, parentId);
  }

  private AuthApiClient authClient() {
    return new AuthApiClient(environment().authBaseUrl());
  }

  private BoardApiClient boardClient() {
    return new BoardApiClient(environment().boardBaseUrl());
  }

  private Environment environment() {
    Environment env = ScenarioContext.get("env", Environment.class);
    if (env != null) {
      return env;
    }
    return ConfigFactory.create(Environment.class, System.getProperties());
  }
}
