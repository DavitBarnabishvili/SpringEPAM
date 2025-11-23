package com.gym.crm.service.messaging;

import com.gym.crm.dto.request.TrainerWorkloadRequest;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

@Service
public class WorkloadMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadMessageProducer.class);

    private final JmsTemplate jmsTemplate;

    @Value("${activemq.queues.trainer-workload}")
    private String queueName;

    public WorkloadMessageProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendWorkloadUpdate(TrainerWorkloadRequest request) {
        try {
            logger.info("Sending workload message to queue: {} for trainer: {}",
                    queueName, request.getTrainerUsername());

            jmsTemplate.convertAndSend(queueName, request);

            logger.info("Successfully sent workload message for trainer: {}",
                    request.getTrainerUsername());
        } catch (JmsException e) {
            logger.error("Failed to send workload message for trainer: {}. Error: {}",
                    request.getTrainerUsername(), e.getMessage(), e);
            //fire-and-forget pattern
        }
    }
}
