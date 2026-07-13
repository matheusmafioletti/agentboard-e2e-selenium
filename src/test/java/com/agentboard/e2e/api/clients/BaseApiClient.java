package com.agentboard.e2e.api.clients;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 * Shared HTTP transport for AgentBoard REST API clients.
 */
public class BaseApiClient {

  private static final HttpClient HTTP = HttpClient.newHttpClient();

  private final String baseUrl;

  /**
   * @param baseUrl service base URL (e.g. {@code http://localhost:8080})
   */
  public BaseApiClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Sends a JSON POST request and returns the parsed response body.
   *
   * @param path         API path relative to {@link #baseUrl}
   * @param jsonBody     request body
   * @param jwt          optional bearer token
   * @param tenantId     optional tenant header
   * @param errorContext human-readable context for failure messages
   * @return parsed JSON response
   */
  protected JSONObject post(
      String path, String jsonBody, String jwt, String tenantId, String errorContext) {
    try {
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + path))
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
            errorContext + " failed (" + response.statusCode() + "): " + response.body());
      }

      return new JSONObject(response.body());
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(errorContext + " failed: " + e.getMessage(), e);
    }
  }

  /**
   * Sends a JSON DELETE request.
   *
   * @param path         API path relative to {@link #baseUrl}
   * @param jwt          optional bearer token
   * @param tenantId     optional tenant header
   * @param errorContext human-readable context for failure messages
   */
  protected void delete(String path, String jwt, String tenantId, String errorContext) {
    try {
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(baseUrl + path))
          .DELETE();

      if (jwt != null) {
        builder.header("Authorization", "Bearer " + jwt);
      }
      if (tenantId != null) {
        builder.header("X-Tenant-Id", tenantId);
      }

      HttpResponse<String> response = HTTP.send(
          builder.build(), HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() >= 400) {
        throw new RuntimeException(
            errorContext + " failed (" + response.statusCode() + "): " + response.body());
      }
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(errorContext + " failed: " + e.getMessage(), e);
    }
  }

  /**
   * Builds standard JSON request headers with optional auth and tenant context.
   *
   * @param jwt      optional bearer token
   * @param tenantId optional tenant identifier
   * @return header map for documentation parity with the Playwright client layer
   */
  protected Map<String, String> jsonHeaders(String jwt, String tenantId) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    if (jwt != null) {
      headers.put("Authorization", "Bearer " + jwt);
    }
    if (tenantId != null) {
      headers.put("X-Tenant-Id", tenantId);
    }
    return headers;
  }
}
