package com.agentboard.e2e.api.clients;

import com.agentboard.e2e.api.types.InviteResult;
import com.agentboard.e2e.api.types.TenantResult;
import com.agentboard.e2e.api.types.UserCredentials;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * HTTP client for the AgentBoard auth service.
 */
public class AuthApiClient extends BaseApiClient {

  /**
   * @param authBaseUrl auth-service base URL from environment config
   */
  public AuthApiClient(String authBaseUrl) {
    super(authBaseUrl);
  }

  /**
   * Registers a new user and workspace.
   *
   * @param email      unique email address
   * @param password   plain-text password
   * @param tenantName workspace display name
   * @return registration response fields
   */
  public JSONObject register(String email, String password, String tenantName) {
    JSONObject body = new JSONObject()
        .put("name", "Test User")
        .put("email", email)
        .put("password", password)
        .put("tenantName", tenantName);
    return post("/auth/register", body.toString(), null, null, "Registration");
  }

  /**
   * Logs in with email and password.
   *
   * @param email    user email
   * @param password user password
   * @return login response fields
   */
  public JSONObject login(String email, String password) {
    JSONObject body = new JSONObject().put("email", email).put("password", password);
    return post("/auth/login", body.toString(), null, null, "Login");
  }

  /**
   * Registers and logs in, returning a complete {@link UserCredentials} record.
   *
   * @param email      unique email address
   * @param password   plain-text password
   * @param tenantName workspace display name
   * @return authenticated user credentials
   */
  public UserCredentials createAuthenticatedUser(
      String email, String password, String tenantName) {
    JSONObject registerData = register(email, password, tenantName);
    JSONObject loginData = login(email, password);

    String jwt = loginData.getString("token");
    Map<String, Object> payload = decodeJwtPayload(jwt);

    String userId = registerData.optString("userId", null);
    if (userId == null || userId.isBlank()) {
      Object sub = payload.get("sub");
      userId = sub instanceof String s ? s : email;
    }

    String role = registerData.optString("role", null);
    if (role == null || role.isBlank()) {
      role = loginData.optString("role", null);
    }
    if (role == null || role.isBlank()) {
      Object payloadRole = payload.get("role");
      role = payloadRole instanceof String s ? s : "ADMIN";
    }

    return new UserCredentials(
        email,
        password,
        tenantName,
        jwt,
        registerData.getString("tenantId"),
        userId,
        role);
  }

  /**
   * Creates a second tenant for an already-authenticated user.
   *
   * @param jwt        bearer token
   * @param tenantName display name for the new tenant
   * @return new tenant identifiers
   */
  public TenantResult createTenant(String jwt, String tenantName) {
    JSONObject body = new JSONObject().put("tenantName", tenantName);
    JSONObject response = post("/auth/tenants", body.toString(), jwt, null, "Create second tenant");

    String tenantId = response.optString("tenantId", null);
    if (tenantId == null || tenantId.isBlank()) {
      tenantId = response.getString("id");
    }

    String resolvedName = response.optString("tenantName", tenantName);
    return new TenantResult(tenantId, resolvedName);
  }

  /**
   * Creates an invite for the given email and returns the invite token.
   *
   * @param jwt      bearer token of the inviting admin
   * @param tenantId tenant identifier
   * @param email    email address to invite
   * @return invite token and identifier
   */
  public InviteResult createInvite(String jwt, String tenantId, String email) {
    JSONObject body = new JSONObject().put("email", email);
    JSONObject response = post(
        "/auth/tenants/" + tenantId + "/invites",
        body.toString(),
        jwt,
        null,
        "Create invite");

    String token = response.optString("token", null);
    String inviteId = response.optString("inviteId", null);
    if (inviteId == null || inviteId.isBlank()) {
      inviteId = response.optString("id", null);
    }
    if (inviteId == null || inviteId.isBlank()) {
      String inviteUrl = response.optString("inviteUrl", "");
      inviteId = extractLastPathSegment(inviteUrl);
    }
    if (token == null || token.isBlank()) {
      token = inviteId;
    }

    return new InviteResult(token, inviteId);
  }

  /**
   * Accepts an invite for the given email and password.
   *
   * @param token    invite token
   * @param email    invited user email
   * @param password invited user password
   */
  public void acceptInvite(String token, String email, String password) {
    JSONObject body = new JSONObject().put("email", email).put("password", password);
    post("/auth/invites/" + token + "/accept", body.toString(), null, null, "Accept invite");
  }

  /**
   * Cancels a pending invite.
   *
   * @param jwt      bearer token
   * @param tenantId tenant identifier
   * @param inviteId invite identifier
   */
  public void cancelInvite(String jwt, String tenantId, String inviteId) {
    delete(
        "/auth/tenants/" + tenantId + "/invites/" + inviteId,
        jwt,
        null,
        "Cancel invite");
  }

  /**
   * Performs a login and returns membership options when tenant selection is required.
   *
   * @param email    user email
   * @param password user password
   * @return list of membership maps with {@code tenantId} and {@code tenantName}
   */
  public List<Map<String, String>> getMemberships(String email, String password) {
    JSONObject body = new JSONObject().put("email", email).put("password", password);
    JSONObject response = post("/auth/login", body.toString(), null, null, "Login");

    List<Map<String, String>> memberships = new ArrayList<>();
    if (response.optBoolean("requiresTenantSelection", false)) {
      JSONArray arr = response.getJSONArray("memberships");
      for (int i = 0; i < arr.length(); i++) {
        JSONObject membership = arr.getJSONObject(i);
        Map<String, String> entry = new HashMap<>();
        entry.put("tenantId", membership.getString("tenantId"));
        entry.put("tenantName", membership.getString("tenantName"));
        memberships.add(entry);
      }
    } else {
      Map<String, String> single = new HashMap<>();
      single.put("tenantId", response.getString("tenantId"));
      single.put("token", response.getString("token"));
      single.put("role", response.optString("role", "ADMIN"));
      memberships.add(single);
    }
    return memberships;
  }

  /**
   * Completes the tenant-selection step for multi-tenant logins.
   *
   * @param email    user email
   * @param password user password
   * @param tenantId target tenant identifier
   * @return map with keys {@code token}, {@code tenantId}, and {@code role}
   */
  public Map<String, String> selectTenant(
      String email, String password, String tenantId) {
    JSONObject body = new JSONObject()
        .put("email", email)
        .put("password", password)
        .put("tenantId", tenantId);
    JSONObject response = post(
        "/auth/select-tenant", body.toString(), null, null, "Select tenant");

    Map<String, String> result = new HashMap<>();
    result.put("token", response.getString("token"));
    result.put("tenantId", response.getString("tenantId"));
    result.put("role", response.optString("role", "USER"));
    return result;
  }

  private static Map<String, Object> decodeJwtPayload(String token) {
    String[] parts = token.split("\\.");
    if (parts.length < 2) {
      return Map.of();
    }
    String json = new String(Base64.getUrlDecoder().decode(parts[1]));
    JSONObject object = new JSONObject(json);
    Map<String, Object> payload = new HashMap<>();
    for (String key : object.keySet()) {
      payload.put(key, object.get(key));
    }
    return payload;
  }

  private static String extractLastPathSegment(String url) {
    if (url == null || url.isBlank()) {
      return "";
    }
    int slash = url.lastIndexOf('/');
    return slash >= 0 ? url.substring(slash + 1) : url;
  }
}
