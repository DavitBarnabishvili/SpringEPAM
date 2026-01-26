package com.gym.crm.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;

@Configuration
@EnableJms
public class ActiveMQConfig {

    @Value("${activemq.queues.trainer-workload}")
    private String trainerWorkloadQueue;

    @Bean
    public ActiveMQQueue trainerWorkloadQueue() {
        return new ActiveMQQueue(trainerWorkloadQueue);
    }

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate(@Qualifier("jmsConnectionFactory") ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(jacksonJmsMessageConverter());
        template.setDeliveryPersistent(true);
        template.setPriority(9);
        return template;
    }
}
