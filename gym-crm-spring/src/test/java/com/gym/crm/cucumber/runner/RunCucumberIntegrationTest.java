package com.gym.crm.cucumber.runner;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/integration")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/integration.html, json:target/cucumber-reports/integration.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.gym.crm.cucumber")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@Integration and not @Ignore")
public class RunCucumberIntegrationTest {
}
