package com.gym.crm.service.impl;

import com.gym.crm.dto.request.TrainerWorkloadRequest;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.entity.TrainingType;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.messaging.WorkloadMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadNotificationServiceTest {

    @Mock
    private WorkloadMessageProducer messageProducer;

    @Mock
    private TrainerService trainerService;

    private WorkloadNotificationService workloadNotificationService;

    private Training testTraining;
    private Trainer testTrainer;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        workloadNotificationService = new WorkloadNotificationService(messageProducer, trainerService);

        testTrainingType = new TrainingType("Cardio");
        testTrainingType.setId(1L);

        testTrainer = new Trainer("Jane", "Smith", testTrainingType);
        testTrainer.setId(2L);
        testTrainer.setUsername("jane.smith");
        testTrainer.setPassword("password");
        testTrainer.setIsActive(true);

        testTraining = new Training(
                1L,
                testTrainer.getId(),
                "Morning Workout",
                testTrainingType,
                LocalDate.of(2025, 1, 15),
                60
        );
        testTraining.setId(1L);
    }

    @Test
    void notifyTrainingAdded_ShouldSendMessage_WhenTrainerExists() {
        when(trainerService.findTrainerById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));
        workloadNotificationService.notifyTrainingAdded(testTraining);
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(messageProducer).sendWorkloadUpdate(requestCaptor.capture());

        TrainerWorkloadRequest captured = requestCaptor.getValue();
        assertThat(captured.getTrainerUsername()).isEqualTo("jane.smith");
        assertThat(captured.getTrainerFirstName()).isEqualTo("Jane");
        assertThat(captured.getTrainerLastName()).isEqualTo("Smith");
        assertThat(captured.getIsActive()).isTrue();
        assertThat(captured.getTrainingDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(captured.getTrainingDuration()).isEqualTo(60);
        assertThat(captured.getActionType()).isEqualTo(TrainerWorkloadRequest.ActionType.ADD);
    }

    @Test
    void notifyTrainingAdded_ShouldNotSendMessage_WhenTrainerNotFound() {
        when(trainerService.findTrainerById(testTrainer.getId())).thenReturn(Optional.empty());
        workloadNotificationService.notifyTrainingAdded(testTraining);
        verify(messageProducer, never()).sendWorkloadUpdate(any());
    }

    @Test
    void notifyTrainingAdded_ShouldIncludeInactiveStatus_WhenTrainerIsInactive() {
        testTrainer.setIsActive(false);
        when(trainerService.findTrainerById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));
        workloadNotificationService.notifyTrainingAdded(testTraining);
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(messageProducer).sendWorkloadUpdate(requestCaptor.capture());

        assertThat(requestCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    void notifyTrainingDeleted_ShouldSendMessage_WhenTrainerExists() {
        when(trainerService.findTrainerById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));
        workloadNotificationService.notifyTrainingDeleted(testTraining);
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(messageProducer).sendWorkloadUpdate(requestCaptor.capture());

        TrainerWorkloadRequest captured = requestCaptor.getValue();
        assertThat(captured.getTrainerUsername()).isEqualTo("jane.smith");
        assertThat(captured.getTrainerFirstName()).isEqualTo("Jane");
        assertThat(captured.getTrainerLastName()).isEqualTo("Smith");
        assertThat(captured.getIsActive()).isTrue();
        assertThat(captured.getTrainingDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(captured.getTrainingDuration()).isEqualTo(60);
        assertThat(captured.getActionType()).isEqualTo(TrainerWorkloadRequest.ActionType.DELETE);
    }

    @Test
    void notifyTrainingDeleted_ShouldNotSendMessage_WhenTrainerNotFound() {
        when(trainerService.findTrainerById(testTrainer.getId())).thenReturn(Optional.empty());
        workloadNotificationService.notifyTrainingDeleted(testTraining);
        verify(messageProducer, never()).sendWorkloadUpdate(any());
    }

    @Test
    void notifyTrainingDeleted_ShouldIncludeCorrectTrainingDetails() {
        Training trainingWithDetails = new Training(
                10L,
                testTrainer.getId(),
                "Evening Session",
                testTrainingType,
                LocalDate.of(2025, 2, 20),
                90
        );
        when(trainerService.findTrainerById(testTrainer.getId())).thenReturn(Optional.of(testTrainer));

        workloadNotificationService.notifyTrainingDeleted(trainingWithDetails);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(messageProducer).sendWorkloadUpdate(requestCaptor.capture());

        TrainerWorkloadRequest captured = requestCaptor.getValue();
        assertThat(captured.getTrainingDate()).isEqualTo(LocalDate.of(2025, 2, 20));
        assertThat(captured.getTrainingDuration()).isEqualTo(90);
        assertThat(captured.getActionType()).isEqualTo(TrainerWorkloadRequest.ActionType.DELETE);
    }
}
