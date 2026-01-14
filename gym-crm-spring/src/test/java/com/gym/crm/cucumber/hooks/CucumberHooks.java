package com.gym.crm.cucumber.hooks;

import com.gym.crm.cucumber.support.ActiveMQTestConsumer;
import com.gym.crm.cucumber.support.TestContext;
import com.gym.crm.security.BruteForceProtectionService;
import com.gym.crm.security.JwtTokenInvalidationService;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class CucumberHooks {

    private static final Logger logger = LoggerFactory.getLogger(CucumberHooks.class);

    @Autowired
    private TestContext testContext;

    @Autowired
    private ActiveMQTestConsumer testConsumer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BruteForceProtectionService bruteForceProtectionService;

    @Autowired
    private JwtTokenInvalidationService jwtTokenInvalidationService;

    @Before
    public void beforeScenario(Scenario scenario) {
        logger.info("Starting scenario: {}", scenario.getName());

        cleanDatabase();
        clearSecurityState();

        testContext.reset();
        testConsumer.clearMessages();
    }

    private void cleanDatabase() {
        try {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

            jdbcTemplate.execute("DELETE FROM trainee_trainer_assignment");
            jdbcTemplate.execute("DELETE FROM training");
            jdbcTemplate.execute("DELETE FROM trainee");
            jdbcTemplate.execute("DELETE FROM trainer");
            jdbcTemplate.execute("DELETE FROM user");

            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

            logger.debug("Database cleaned successfully");
        } catch (Exception e) {
            logger.warn("Error cleaning database: {}", e.getMessage());
        }
    }

    private void clearSecurityState() {
        try {
            bruteForceProtectionService.clearAllAttempts();
            jwtTokenInvalidationService.clearAllInvalidatedTokens();

            logger.debug("Security state cleared successfully");
        } catch (Exception e) {
            logger.warn("Error clearing security state: {}", e.getMessage());
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        logger.info("Finished scenario: {} - Status: {}", scenario.getName(), scenario.getStatus());

        if (scenario.isFailed()) {
            if (testContext.getLastResponse() != null) {
                scenario.attach(
                    testContext.getLastResponse().getBody().asString().getBytes(),
                    "application/json",
                    "Last Response"
                );
            }
        }
    }
}
