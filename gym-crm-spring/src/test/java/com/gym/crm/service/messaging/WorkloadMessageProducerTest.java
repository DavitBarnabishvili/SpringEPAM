package com.gym.crm.service.messaging;

import com.gym.crm.dto.request.TrainerWorkloadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageProducerTest {

    @Mock
    private JmsTemplate jmsTemplate;

    private WorkloadMessageProducer workloadMessageProducer;

    private TrainerWorkloadRequest testRequest;
    private static final String TEST_QUEUE_NAME = "trainer.workload.queue";

    @BeforeEach
    void setUp() {
        workloadMessageProducer = new WorkloadMessageProducer(jmsTemplate);
        ReflectionTestUtils.setField(workloadMessageProducer, "queueName", TEST_QUEUE_NAME);

        testRequest = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 1, 15))
                .trainingDuration(60)
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();
    }

    @Test
    void sendWorkloadUpdate_ShouldSendMessageToQueue_WhenValidRequest() {
        workloadMessageProducer.sendWorkloadUpdate(testRequest);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(eq(TEST_QUEUE_NAME), requestCaptor.capture());

        TrainerWorkloadRequest captured = requestCaptor.getValue();
        assertThat(captured.getTrainerUsername()).isEqualTo("john.doe");
        assertThat(captured.getTrainerFirstName()).isEqualTo("John");
        assertThat(captured.getTrainerLastName()).isEqualTo("Doe");
        assertThat(captured.getIsActive()).isTrue();
        assertThat(captured.getTrainingDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(captured.getTrainingDuration()).isEqualTo(60);
        assertThat(captured.getActionType()).isEqualTo(TrainerWorkloadRequest.ActionType.ADD);
    }

    @Test
    void sendWorkloadUpdate_ShouldSendDeleteAction_WhenActionTypeIsDelete() {
        testRequest.setActionType(TrainerWorkloadRequest.ActionType.DELETE);
        workloadMessageProducer.sendWorkloadUpdate(testRequest);
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(eq(TEST_QUEUE_NAME), requestCaptor.capture());

        assertThat(requestCaptor.getValue().getActionType())
                .isEqualTo(TrainerWorkloadRequest.ActionType.DELETE);
    }

    @Test
    void sendWorkloadUpdate_ShouldNotThrowException_WhenJmsExceptionOccurs() {
        doThrow(new JmsException("Connection failed") {})
                .when(jmsTemplate).convertAndSend(eq(TEST_QUEUE_NAME), any(TrainerWorkloadRequest.class));

        assertThatCode(() -> workloadMessageProducer.sendWorkloadUpdate(testRequest))
                .doesNotThrowAnyException();

        verify(jmsTemplate).convertAndSend(eq(TEST_QUEUE_NAME), eq(testRequest));
    }

    @Test
    void sendWorkloadUpdate_ShouldSendToCorrectQueue_WhenMultipleMessagesAreSent() {
        TrainerWorkloadRequest request1 = TrainerWorkloadRequest.builder()
                .trainerUsername("trainer1")
                .trainerFirstName("First")
                .trainerLastName("Trainer")
                .isActive(true)
                .trainingDate(LocalDate.now())
                .trainingDuration(30)
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();

        TrainerWorkloadRequest request2 = TrainerWorkloadRequest.builder()
                .trainerUsername("trainer2")
                .trainerFirstName("Second")
                .trainerLastName("Trainer")
                .isActive(false)
                .trainingDate(LocalDate.now())
                .trainingDuration(45)
                .actionType(TrainerWorkloadRequest.ActionType.DELETE)
                .build();

        workloadMessageProducer.sendWorkloadUpdate(request1);
        workloadMessageProducer.sendWorkloadUpdate(request2);

        verify(jmsTemplate, times(2)).convertAndSend(eq(TEST_QUEUE_NAME), any(TrainerWorkloadRequest.class));
        verify(jmsTemplate).convertAndSend(TEST_QUEUE_NAME, request1);
        verify(jmsTemplate).convertAndSend(TEST_QUEUE_NAME, request2);
    }
}
