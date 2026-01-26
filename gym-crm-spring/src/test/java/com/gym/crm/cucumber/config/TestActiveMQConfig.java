package com.gym.crm.cucumber.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@TestConfiguration
@EnableJms
public class TestActiveMQConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean(name = "jmsConnectionFactory")
    @Primary
    public ConnectionFactory jmsConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("vm://localhost?broker.persistent=false");
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    public ActiveMQQueue testTrainerWorkloadQueue() {
        return new ActiveMQQueue("trainer.workload.queue");
    }

    @Bean
    @Primary
    public JmsTemplate testJmsTemplate(@Qualifier("jmsConnectionFactory") ConnectionFactory jmsConnectionFactory) {
        JmsTemplate template = new JmsTemplate(jmsConnectionFactory);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper); // Use Spring Boot's auto-configured ObjectMapper

        template.setMessageConverter(converter);
        template.setDeliveryPersistent(true);
        template.setPriority(9);

        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            @Qualifier("jmsConnectionFactory") ConnectionFactory jmsConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(jmsConnectionFactory);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper); // Use Spring Boot's auto-configured ObjectMapper

        factory.setMessageConverter(converter);
        return factory;
    }
}
