package com.agentboard.e2e.api.types;

/**
 * Authenticated user identity stored in browser {@code localStorage}.
 */
public record UserInfo(
    String userId,
    String email,
    String tenantId,
    String tenantName,
    String role
) {}
