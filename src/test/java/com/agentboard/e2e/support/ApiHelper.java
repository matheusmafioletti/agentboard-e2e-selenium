package com.agentboard.e2e.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Static helpers that provision test data via the AgentBoard REST APIs.
 *
 * <p>All methods are synchronous and use {@link java.net.http.HttpClient}. Failures are wrapped
 * in {@link RuntimeException} so that Cucumber step definitions surface them as test errors.
 */
public final class ApiHelper {

  private static final HttpClient HTTP = HttpClient.newHttpClient();

  private ApiHelper() {}

  /**
   * Registers a new user and returns a map containing {@code token}, {@code tenantId},
   * {@code role}, and {@code email}.
   *
   * @param authUrl    base URL of the auth-service (e.g. {@code http://localhost:8080})
   * @param email      unique email address for the new user
   * @param password   plain-text password (must satisfy the app's strength rules)
   * @param tenantName name for the new workspace / tenant
   * @return map with keys: {@code token}, {@code tenantId}, {@code role}, {@code email}
   */
  public static Map<String, String> createUser(
      String authUrl, String email, String password, String tenantName) {
    JSONObject body = new JSONObject()
        .put("name", "Test User")
        .put("email", email)
        .put("password", password)
        .put("tenantName", tenantName);

    JSONObject response = post(authUrl + "/auth/register", body.toString(), null, null);

    Map<String, String> result = new HashMap<>();
    result.put("token", response.getString("token"));
    result.put("tenantId", response.getString("tenantId"));
    result.put("role", response.optString("role", "ADMIN"));
    result.put("email", email);
    result.put("tenantName", tenantName);
    return result;
  }

  /**
   * Creates a project and returns its {@code id}.
   *
   * @param boardUrl    base URL of the board-service
   * @param jwt         JWT token for the authenticated user
   * @param tenantId    tenant identifier
   * @param projectName display name for the new project
   * @return project {@code id} as a string
   */
  public static String createProject(
      String boardUrl, String jwt, String tenantId, String projectName) {
    JSONObject body = new JSONObject().put("name", projectName);
    JSONObject response = post(boardUrl + "/projects", body.toString(), jwt, tenantId);
    return response.getString("id");
  }

  /**
   * Creates a work item and returns its {@code id}.
   *
   * @param boardUrl  base URL of the board-service
   * @param jwt       JWT token
   * @param tenantId  tenant identifier
   * @param projectId parent project identifier
   * @param title     work item title
   * @param type      work item type: {@code FEATURE}, {@code USER_STORY}, or {@code TASK}
   * @return work item {@code id} as a string
   */
  public static String createWorkItem(
      String boardUrl, String jwt, String tenantId,
      String projectId, String title, String type) {
    JSONObject body = new JSONObject().put("title", title).put("type", type);
    JSONObject response = post(
        boardUrl + "/projects/" + projectId + "/work-items",
        body.toString(), jwt, tenantId);
    return response.getString("id");
  }

  /**
   * Creates a second tenant for an already-authenticated user and returns the new tenant's
   * {@code tenantId}.
   *
   * @param authUrl    base URL of the auth-service
   * @param jwt        JWT token of the authenticated user
   * @param tenantName display name for the new tenant
   * @return new tenant {@code id} as a string
   */
  public static String createTenant(String authUrl, String jwt, String tenantName) {
    JSONObject body = new JSONObject().put("tenantName", tenantName);
    JSONObject response = post(authUrl + "/auth/tenants", body.toString(), jwt, null);
    return response.getString("id");
  }

  /**
   * Creates an invite for the given email and returns the invite token.
   *
   * @param authUrl  base URL of the auth-service
   * @param jwt      JWT token of the authenticated admin
   * @param tenantId tenant identifier
   * @param email    email address to invite
   * @return raw invite token extracted from the {@code inviteUrl} or {@code token} field
   */
  public static String createInvite(
      String authUrl, String jwt, String tenantId, String email) {
    JSONObject body = new JSONObject().put("email", email);
    JSONObject response = post(
        authUrl + "/auth/tenants/" + tenantId + "/invites",
        body.toString(), jwt, null);

    if (response.has("token")) {
      return response.getString("token");
    }
    String inviteUrl = response.getString("inviteUrl");
    return inviteUrl.substring(inviteUrl.lastIndexOf('/') + 1);
  }

  /**
   * Performs a login and returns membership options when the response contains
   * {@code requiresTenantSelection: true}, or a single-element list otherwise.
   *
   * @param authUrl  base URL of the auth-service
   * @param email    user email
   * @param password user password
   * @return list of membership maps with keys {@code tenantId} and {@code tenantName}
   */
  public static java.util.List<Map<String, String>> getMemberships(
      String authUrl, String email, String password) {
    JSONObject body = new JSONObject().put("email", email).put("password", password);
    JSONObject response = post(authUrl + "/auth/login", body.toString(), null, null);

    java.util.List<Map<String, String>> memberships = new java.util.ArrayList<>();
    if (response.optBoolean("requiresTenantSelection", false)) {
      JSONArray arr = response.getJSONArray("memberships");
      for (int i = 0; i < arr.length(); i++) {
        JSONObject m = arr.getJSONObject(i);
        Map<String, String> entry = new HashMap<>();
        entry.put("tenantId", m.getString("tenantId"));
        entry.put("tenantName", m.getString("tenantName"));
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
   * Completes the tenant-selection step for multi-tenant logins and returns authentication
   * details.
   *
   * @param authUrl  base URL of the auth-service
   * @param email    user email
   * @param password user password
   * @param tenantId target tenant identifier
   * @return map with keys: {@code token}, {@code tenantId}, {@code role}
   */
  public static Map<String, String> selectTenant(
      String authUrl, String email, String password, String tenantId) {
    JSONObject body = new JSONObject()
        .put("email", email)
        .put("password", password)
        .put("tenantId", tenantId);
    JSONObject response = post(authUrl + "/auth/select-tenant", body.toString(), null, null);

    Map<String, String> result = new HashMap<>();
    result.put("token", response.getString("token"));
    result.put("tenantId", response.getString("tenantId"));
    result.put("role", response.optString("role", "USER"));
    return result;
  }

  /**
   * Injects authentication into the browser's {@code localStorage} so that subsequent
   * navigations are treated as authenticated without going through the login UI.
   *
   * @param driver     active WebDriver instance
   * @param jwt        JWT token
   * @param userId     user identifier (can be email)
   * @param email      user email address
   * @param tenantId   active tenant identifier
   * @param tenantName display name of the active workspace
   * @param role       user role: {@code ADMIN} or {@code USER}
   */
  public static void setAuthInLocalStorage(
      WebDriver driver, String jwt, String userId,
      String email, String tenantId, String tenantName, String role) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("localStorage.setItem('agentboard_token', arguments[0])", jwt);

    JSONObject user = new JSONObject()
        .put("userId", userId)
        .put("email", email)
        .put("tenantId", tenantId)
        .put("tenantName", tenantName)
        .put("role", role);
    js.executeScript(
        "localStorage.setItem('agentboard_user', arguments[0])",
        user.toString());
  }

  /**
   * Clears all AgentBoard authentication keys from {@code localStorage}.
   *
   * @param driver active WebDriver instance
   */
  public static void clearAuthFromLocalStorage(WebDriver driver) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript(
        "localStorage.removeItem('agentboard_token');"
        + "localStorage.removeItem('agentboard_user');");
  }

  /**
   * Generates a unique email address safe for test use.
   *
   * @return email string of the form {@code test-<timestamp>@agentboard.test}
   */
  public static String generateEmail() {
    return "test-" + System.currentTimeMillis() + "@agentboard.test";
  }

  /**
   * Generates a unique workspace name safe for test use.
   *
   * @return workspace name of the form {@code Tenant-<timestamp>}
   */
  public static String generateTenantName() {
    return "Tenant-" + System.currentTimeMillis();
  }

  // -------------------------------------------------------------------------
  // Private HTTP helpers
  // -------------------------------------------------------------------------

  private static JSONObject post(
      String url, String jsonBody, String jwt, String tenantId) {
    try {
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json");

      if (jwt != null) {
        builder.header("Authorization", "Bearer " + jwt);
      }
      if (tenantId != null) {
        builder.header("X-Tenant-Id", tenantId);
      }

      HttpRequest request = builder
          .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
          .build();

      HttpResponse<String> response = HTTP.send(
          request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() >= 400) {
        throw new RuntimeException(
            "API call to " + url + " failed with status "
            + response.statusCode() + ": " + response.body());
      }

      return new JSONObject(response.body());
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("HTTP request to " + url + " failed: " + e.getMessage(), e);
    }
  }
}
