package com.agentboard.e2e.support;

import com.agentboard.e2e.api.types.UserInfo;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Browser-side helpers for authentication state in Selenium scenarios.
 */
public final class BrowserAuth {

  private BrowserAuth() {}

  /**
   * Injects authentication into the browser's {@code localStorage}.
   *
   * <p>IMPORTANT: navigate to the app origin before calling — localStorage requires same origin.
   *
   * @param driver active WebDriver instance
   * @param jwt    JWT token
   * @param user   user identity stored in {@code agentboard_user}
   */
  public static void setAuthInLocalStorage(WebDriver driver, String jwt, UserInfo user) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("localStorage.setItem('agentboard_token', arguments[0])", jwt);

    JSONObject userJson = new JSONObject()
        .put("userId", user.userId())
        .put("email", user.email())
        .put("tenantId", user.tenantId())
        .put("tenantName", user.tenantName())
        .put("role", user.role());
    js.executeScript(
        "localStorage.setItem('agentboard_user', arguments[0])",
        userJson.toString());
  }

  /**
   * Clears all AgentBoard authentication keys from {@code localStorage}.
   *
   * <p>IMPORTANT: navigate to the app origin before calling — localStorage requires same origin.
   *
   * @param driver active WebDriver instance
   */
  public static void clearAuthFromLocalStorage(WebDriver driver) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript(
        "localStorage.removeItem('agentboard_token');"
        + "localStorage.removeItem('agentboard_user');");
  }
}
