package com.agentboard.e2e.support;

import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.WebDriver;

/**
 * Thread-local state container shared across step definitions within a single Cucumber scenario.
 *
 * <p>Each test thread receives its own isolated context, enabling safe parallel execution.
 * The context must be cleared in the {@code @After} hook to prevent memory leaks.
 */
public final class ScenarioContext {

  private static final ThreadLocal<Map<String, Object>> CONTEXT =
    ThreadLocal.withInitial(HashMap::new);

  private static final String DRIVER_KEY = "__webdriver__";

  private ScenarioContext() {}

  /**
   * Stores an arbitrary value under {@code key} for the current scenario.
   *
   * @param key   context key
   * @param value value to store
   */
  public static void set(String key, Object value) {
    CONTEXT.get().put(key, value);
  }

  /**
   * Retrieves a previously stored value cast to {@code type}.
   *
   * @param key  context key
   * @param type expected type
   * @param <T>  return type
   * @return stored value, or {@code null} if absent
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(String key, Class<T> type) {
    return (T) CONTEXT.get().get(key);
  }

  /**
   * Stores the {@link WebDriver} instance for the current scenario.
   *
   * @param driver active driver
   */
  public static void setDriver(WebDriver driver) {
    CONTEXT.get().put(DRIVER_KEY, driver);
  }

  /**
   * Returns the {@link WebDriver} stored for the current scenario.
   *
   * @return active driver, or {@code null} if not yet initialised
   */
  public static WebDriver getDriver() {
    return (WebDriver) CONTEXT.get().get(DRIVER_KEY);
  }

  /**
   * Removes all entries from the current scenario's context and cleans up the ThreadLocal.
   */
  public static void clear() {
    CONTEXT.get().clear();
    CONTEXT.remove();
  }
}
