package com.agentboard.e2e.api.types;

/** Result of creating a tenant invite. */
public record InviteResult(String token, String inviteId) {}
