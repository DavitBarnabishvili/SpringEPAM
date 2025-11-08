package com.gym.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class GymCrmApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Gym CRM Spring Boot Application...");

        ApplicationContext context = SpringApplication.run(GymCrmApplication.class, args);

        Environment env = context.getEnvironment();

        String[] activeProfiles = env.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";

        String port = env.getProperty("server.port", "8080");

        String managementPort = env.getProperty("management.server.port", port);

        boolean h2ConsoleEnabled = Boolean.parseBoolean(env.getProperty("spring.h2.console.enabled", "false"));

        String actuatorBasePath = env.getProperty("management.endpoints.web.base-path", "/actuator");

        logger.info("===========================================");
        logger.info("Gym CRM Application Started Successfully!");
        logger.info("Active Profile(s): {}", activeProfile);
        logger.info("Server Port: {}", port);
        logger.info("===========================================");
        logger.info("Available Endpoints:");
        logger.info("  - Application: http://localhost:{}", port);
        logger.info("  - Swagger UI: http://localhost:{}/swagger-ui.html", port);
        logger.info("  - API Docs: http://localhost:{}/api-docs", port);
        logger.info("  - Metrics Dashboard: http://localhost:{}/metrics-dashboard", port);

        if (h2ConsoleEnabled) {
            String h2Path = env.getProperty("spring.h2.console.path", "/h2-console");
            logger.info("  - H2 Console: http://localhost:{}{} (ENABLED)", port, h2Path);
        } else {
            logger.info("  - H2 Console: DISABLED");
        }

        String exposedEndpoints = env.getProperty("management.endpoints.web.exposure.include", "none");
        if (!"none".equals(exposedEndpoints)) {
            if (!port.equals(managementPort)) {
                logger.info("  - Actuator Health: http://localhost:{}{}/health", managementPort, actuatorBasePath);
                logger.info("  - Actuator Info: http://localhost:{}{}/info", managementPort, actuatorBasePath);
                if (exposedEndpoints.contains("prometheus") || exposedEndpoints.contains("*")) {
                    logger.info("  - Prometheus Metrics: http://localhost:{}{}/prometheus", managementPort, actuatorBasePath);
                }
            } else {
                logger.info("  - Actuator Health: http://localhost:{}{}/health", port, actuatorBasePath);
                logger.info("  - Actuator Info: http://localhost:{}{}/info", port, actuatorBasePath);
                if (exposedEndpoints.contains("prometheus") || exposedEndpoints.contains("*")) {
                    logger.info("  - Prometheus Metrics: http://localhost:{}{}/prometheus", port, actuatorBasePath);
                }
            }
        }

        logger.info("===========================================");

        String dbUrl = env.getProperty("spring.datasource.url", "not configured");
        if (dbUrl.contains("h2:mem")) {
            logger.info("Database: H2 In-Memory");
        } else if (dbUrl.contains("h2:file")) {
            logger.info("Database: H2 File-based");
        } else if (dbUrl.contains("postgresql")) {
            logger.info("Database: PostgreSQL");
        } else if (dbUrl.contains("mysql")) {
            logger.info("Database: MySQL");
        } else {
            logger.info("Database: Configured");
        }

        switch (activeProfile) {
            case "local" -> logger.info("Running in LOCAL mode - All debug features enabled");
            case "dev" -> logger.info("Running in DEVELOPMENT mode");
            case "stg" -> logger.info("Running in STAGING mode - Limited features");
            case "prod" -> logger.warn("Running in PRODUCTION mode - Restricted access");
        }

        logger.info("===========================================");
    }
}