package com.agentboard.e2e.pages;

import com.agentboard.e2e.config.Environment;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base class for all Page Objects.
 *
 * <p>Provides fluent helper methods built on explicit {@link WebDriverWait} so that
 * individual page objects never resort to {@code Thread.sleep}.
 */
public abstract class BasePage {

  protected final WebDriver driver;
  protected final WebDriverWait wait;
  protected final Environment env;

  /**
   * @param driver active WebDriver instance
   * @param env    resolved environment configuration used for timeouts and base URL
   */
  protected BasePage(WebDriver driver, Environment env) {
    this.driver = driver;
    this.env = env;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(env.implicitWaitSeconds()));
  }

  /**
   * Navigates to a path relative to {@link Environment#appBaseUrl()}.
   *
   * @param path relative path, e.g. {@code "/login"}
   */
  protected void navigate(String path) {
    driver.get(env.appBaseUrl() + path);
  }

  /**
   * Waits until the element identified by {@code locator} is visible in the DOM.
   *
   * @param locator element locator
   * @return the visible {@link WebElement}
   */
  protected WebElement waitFor(By locator) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  /**
   * Waits until the element identified by {@code locator} is clickable.
   *
   * @param locator element locator
   * @return the clickable {@link WebElement}
   */
  protected WebElement waitForClickable(By locator) {
    return wait.until(ExpectedConditions.elementToBeClickable(locator));
  }

  /**
   * Clicks the element identified by {@code locator} after waiting for it to be clickable.
   *
   * @param locator element locator
   */
  protected void click(By locator) {
    waitForClickable(locator).click();
  }

  /**
   * Clears and types {@code text} into the element identified by {@code locator}.
   *
   * @param locator element locator
   * @param text    text to enter
   */
  protected void type(By locator, String text) {
    WebElement element = waitForClickable(locator);
    element.clear();
    element.sendKeys(text);
  }

  /**
   * Returns the visible text of the element identified by {@code locator}.
   *
   * @param locator element locator
   * @return trimmed text content
   */
  protected String getText(By locator) {
    return waitFor(locator).getText().trim();
  }

  /**
   * Returns {@code true} if the element is present and visible without throwing.
   *
   * @param locator element locator
   * @return visibility status
   */
  protected boolean isVisible(By locator) {
    try {
      return driver.findElement(locator).isDisplayed();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Captures a PNG screenshot of the current browser state.
   *
   * @return screenshot bytes, or an empty array if capture fails
   */
  public byte[] takeScreenshot() {
    try {
      return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    } catch (Exception e) {
      return new byte[0];
    }
  }
}
