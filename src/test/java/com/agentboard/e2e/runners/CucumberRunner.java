package com.agentboard.e2e.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * JUnit Platform Suite entry point that launches all Cucumber features.
 *
 * <p>Tag expressions and plugin configuration are read from
 * {@code src/test/resources/cucumber.properties} and can be overridden on the Maven
 * command line with {@code -Dcucumber.filter.tags="@smoke"}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm,pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.agentboard.e2e")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class CucumberRunner {
}
