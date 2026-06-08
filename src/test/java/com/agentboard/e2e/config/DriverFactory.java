package com.agentboard.e2e.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Creates and configures {@link WebDriver} instances for use in test scenarios.
 *
 * <p>Each call to {@link #createDriver(Environment)} returns a new, independently configured
 * driver. Callers are responsible for storing it in {@link com.agentboard.e2e.support.ScenarioContext}
 * and quitting it after the scenario completes.
 */
public final class DriverFactory {

  private DriverFactory() {}

  /**
   * Creates a {@link WebDriver} configured according to the supplied {@link Environment}.
   *
   * @param env resolved environment configuration
   * @return a ready-to-use {@link WebDriver} instance
   * @throws IllegalArgumentException if the configured browser is not supported
   */
  public static WebDriver createDriver(Environment env) {
    WebDriver driver = switch (env.browser().toLowerCase()) {
      case "chrome" -> createChromeDriver(env);
      case "firefox" -> createFirefoxDriver(env);
      default -> throw new IllegalArgumentException("Unsupported browser: " + env.browser());
    };

    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(env.implicitWaitSeconds()));
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(env.pageLoadTimeoutSeconds()));
    driver.manage().window().maximize();

    return driver;
  }

  private static WebDriver createChromeDriver(Environment env) {
    WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    if (env.headless()) {
      options.addArguments("--headless=new");
    }
    options.addArguments(
      "--disable-gpu",
      "--no-sandbox",
      "--disable-dev-shm-usage",
      "--window-size=1920,1080"
    );
    return new ChromeDriver(options);
  }

  private static WebDriver createFirefoxDriver(Environment env) {
    WebDriverManager.firefoxdriver().setup();
    FirefoxOptions options = new FirefoxOptions();
    if (env.headless()) {
      options.addArguments("--headless");
    }
    return new FirefoxDriver(options);
  }
}
