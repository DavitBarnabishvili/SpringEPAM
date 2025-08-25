package com.gym.crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class GymCrmApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Gym CRM Spring Boot Application...");

        ApplicationContext context = SpringApplication.run(GymCrmApplication.class, args);

        logger.info("===========================================");
        logger.info("Gym CRM Application Started Successfully!");
        logger.info("Swagger UI: http://localhost:8080/swagger-ui.html");
        logger.info("H2 Console: http://localhost:8080/h2-console");
        logger.info("===========================================");
    }
}