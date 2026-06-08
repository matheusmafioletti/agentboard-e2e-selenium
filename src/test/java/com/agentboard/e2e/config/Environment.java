package com.agentboard.e2e.config;

import org.aeonbits.owner.Config;

/**
 * Typed configuration loaded from {@code classpath:environments/${env}.properties}.
 *
 * <p>The {@code env} token is resolved at runtime from the {@code env} system property
 * (default: {@code local}). Override with {@code -Denv=staging} on the Maven command line.
 */
@Config.Sources("classpath:environments/${env}.properties")
public interface Environment extends Config {

  /** Base URL of the AgentBoard frontend application. */
  @Key("app.base.url")
  @DefaultValue("http://localhost:5173")
  String appBaseUrl();

  /** Browser to use: {@code chrome} or {@code firefox}. */
  @Key("browser")
  @DefaultValue("chrome")
  String browser();

  /** Whether to run the browser in headless mode. */
  @Key("headless")
  @DefaultValue("true")
  boolean headless();

  /** Implicit wait applied globally to the {@link org.openqa.selenium.WebDriver}. */
  @Key("implicit.wait.seconds")
  @DefaultValue("10")
  int implicitWaitSeconds();

  /** Page load timeout for the {@link org.openqa.selenium.WebDriver}. */
  @Key("page.load.timeout.seconds")
  @DefaultValue("30")
  int pageLoadTimeoutSeconds();
}
