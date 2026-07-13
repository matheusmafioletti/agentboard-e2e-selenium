package com.agentboard.e2e.api.types;

/**
 * Fully provisioned test user with JWT and password for API and UI flows.
 */
public record UserCredentials(
    String email,
    String password,
    String tenantName,
    String jwt,
    String tenantId,
    String userId,
    String role
) {

  /** Returns the {@link UserInfo} subset required for browser authentication. */
  public UserInfo toUserInfo() {
    return new UserInfo(userId, email, tenantId, tenantName, role);
  }
}
