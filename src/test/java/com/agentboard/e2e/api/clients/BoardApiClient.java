package com.agentboard.e2e.api.clients;

import com.agentboard.e2e.api.types.ProjectResult;
import com.agentboard.e2e.api.types.WorkItemResult;
import com.agentboard.e2e.api.types.WorkItemType;
import org.json.JSONObject;

/**
 * HTTP client for the AgentBoard board service.
 */
public class BoardApiClient extends BaseApiClient {

  /**
   * @param boardBaseUrl board-service base URL from environment config
   */
  public BoardApiClient(String boardBaseUrl) {
    super(boardBaseUrl);
  }

  /**
   * Creates a project in the given tenant.
   *
   * @param jwt        bearer token
   * @param tenantId   tenant identifier
   * @param name       project display name
   * @return created project identifiers
   */
  public ProjectResult createProject(String jwt, String tenantId, String name) {
    JSONObject body = new JSONObject().put("name", name);
    JSONObject response = post(
        "/api/v1/projects", body.toString(), jwt, tenantId, "Create project");
    return new ProjectResult(response.getString("id"), name);
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
    return createWorkItem(jwt, tenantId, projectId, title, type, null);
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
    JSONObject body = new JSONObject().put("title", title).put("type", type.name());
    if (parentId != null && !parentId.isBlank()) {
      body.put("parentId", parentId);
    }

    JSONObject response = post(
        "/api/v1/work-items?projectId=" + projectId,
        body.toString(),
        jwt,
        tenantId,
        "Create work item");
    return new WorkItemResult(response.getString("id"));
  }
}
