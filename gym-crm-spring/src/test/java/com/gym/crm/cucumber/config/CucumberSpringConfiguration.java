package com.gym.crm.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.activemq.broker-url=vm://localhost?broker.persistent=false"
    }
)
@ActiveProfiles("cucumber")
@Import(TestActiveMQConfig.class)
public class CucumberSpringConfiguration {
}
