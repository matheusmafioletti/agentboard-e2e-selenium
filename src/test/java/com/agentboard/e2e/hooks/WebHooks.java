package com.agentboard.e2e.hooks;

import com.agentboard.e2e.config.DriverFactory;
import com.agentboard.e2e.config.Environment;
import com.agentboard.e2e.pages.BasePage;
import com.agentboard.e2e.support.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import org.aeonbits.owner.ConfigFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Cucumber lifecycle hooks that manage the {@link WebDriver} for each scenario.
 *
 * <p>A fresh driver is created before every scenario and torn down unconditionally after it,
 * with a screenshot attached to the Allure report when the scenario fails.
 */
public class WebHooks {

  /**
   * Initialises a {@link WebDriver} and stores it in {@link ScenarioContext} before each scenario.
   *
   * @param scenario the current Cucumber scenario (used for logging)
   */
  @Before
  public void setUp(Scenario scenario) {
    Environment env = ConfigFactory.create(Environment.class, System.getProperties());
    WebDriver driver = DriverFactory.createDriver(env);
    ScenarioContext.setDriver(driver);
    ScenarioContext.set("env", env);
  }

  /**
   * Quits the {@link WebDriver} after each scenario, capturing a failure screenshot first.
   *
   * @param scenario the current Cucumber scenario
   */
  @After
  public void tearDown(Scenario scenario) {
    WebDriver driver = ScenarioContext.getDriver();
    if (driver == null) {
      return;
    }
    try {
      if (scenario.isFailed()) {
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(
          "Failure screenshot — " + scenario.getName(),
          "image/png",
          new ByteArrayInputStream(screenshot),
          ".png"
        );
      }
    } finally {
      driver.quit();
      ScenarioContext.clear();
    }
  }
}
