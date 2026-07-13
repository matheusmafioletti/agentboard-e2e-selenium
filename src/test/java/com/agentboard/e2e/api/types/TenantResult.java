package com.agentboard.e2e.api.types;

/** Result of creating a secondary tenant for an authenticated user. */
public record TenantResult(String tenantId, String tenantName) {}
