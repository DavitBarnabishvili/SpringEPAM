package com.gym.crm.cucumber.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.dto.request.TrainerWorkloadRequest;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ActiveMQTestConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMQTestConsumer.class);

    private final List<TrainerWorkloadRequest> receivedMessages = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public ActiveMQTestConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "trainer.workload.queue")
    public void receiveMessage(TrainerWorkloadRequest request) {
        try {
            logger.info("Test consumer received workload request for trainer: {}, action: {}",
                request.getTrainerUsername(), request.getActionType());

            receivedMessages.add(request);

            logger.info("Successfully processed workload message for trainer: {}",
                request.getTrainerUsername());
        } catch (Exception e) {
            logger.error("Error receiving message", e);
        }
    }

    public List<TrainerWorkloadRequest> getReceivedMessages() {
        return new ArrayList<>(receivedMessages);
    }

    public int getMessageCount() {
        return receivedMessages.size();
    }

    public void clearMessages() {
        receivedMessages.clear();
    }

    public TrainerWorkloadRequest getLastMessage() {
        return receivedMessages.isEmpty() ? null : receivedMessages.get(receivedMessages.size() - 1);
    }
}
