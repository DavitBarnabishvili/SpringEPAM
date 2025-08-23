package com.gym.crm.storage;

import com.gym.crm.entity.Trainee;
import com.gym.crm.entity.Trainer;
import com.gym.crm.entity.Training;
import com.gym.crm.entity.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryStorage Tests")
@Execution(ExecutionMode.CONCURRENT)
class InMemoryStorageTest {

    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
    }

    @Test
    @DisplayName("Initial state should be empty")
    void initialState_ShouldBeEmpty() {
        assertAll(
                () -> assertEquals(0, storage.getAllTrainers().size()),
                () -> assertEquals(0, storage.getAllTrainees().size()),
                () -> assertEquals(0, storage.getAllTrainings().size()),
                () -> assertEquals(0, storage.getAllTrainingTypes().size()),
                () -> assertEquals(0, storage.getTotalEntities())
        );
    }

    @Test
    @DisplayName("generateTrainerId should generate unique sequential IDs")
    void generateTrainerId_ShouldGenerateUniqueSequentialIds() {
        Long id1 = storage.generateTrainerId();
        Long id2 = storage.generateTrainerId();
        Long id3 = storage.generateTrainerId();

        assertEquals(1L, id1);
        assertEquals(2L, id2);
        assertEquals(3L, id3);
    }

    @Test
    @DisplayName("storeTrainer should store trainer correctly")
    void storeTrainer_ShouldStoreCorrectly() {
        TrainingType specialization = new TrainingType("Cardio");
        Trainer trainer = new Trainer("John", "Doe", specialization);
        trainer.setUserId(1L);

        storage.storeTrainer(1L, trainer);

        assertEquals(1, storage.getAllTrainers().size());
        assertEquals(trainer, storage.getTrainer(1L));
    }

    @Test
    @DisplayName("getTrainer should return null for non-existent ID")
    void getTrainer_WithNonExistentId_ShouldReturnNull() {
        assertNull(storage.getTrainer(999L));
    }

    @Test
    @DisplayName("removeTrainer should remove trainer")
    void removeTrainer_ShouldRemoveTrainer() {
        Trainer trainer = new Trainer("John", "Doe");
        storage.storeTrainer(1L, trainer);

        storage.removeTrainer(1L);

        assertNull(storage.getTrainer(1L));
        assertEquals(0, storage.getAllTrainers().size());
    }

    @Test
    @DisplayName("removeTrainer with non-existent ID should not throw exception")
    void removeTrainer_WithNonExistentId_ShouldNotThrow() {
        assertDoesNotThrow(() -> storage.removeTrainer(999L));
    }

    @Test
    @DisplayName("getAllTrainers should return copy of trainers")
    void getAllTrainers_ShouldReturnCopy() {
        Trainer trainer1 = new Trainer("John", "Doe");
        Trainer trainer2 = new Trainer("Jane", "Smith");

        storage.storeTrainer(1L, trainer1);
        storage.storeTrainer(2L, trainer2);

        List<Trainer> trainers = storage.getAllTrainers();
        assertEquals(2, trainers.size());
        assertTrue(trainers.contains(trainer1));
        assertTrue(trainers.contains(trainer2));
    }

    @Test
    @DisplayName("generateTraineeId should generate unique sequential IDs")
    void generateTraineeId_ShouldGenerateUniqueSequentialIds() {
        Long id1 = storage.generateTraineeId();
        Long id2 = storage.generateTraineeId();
        Long id3 = storage.generateTraineeId();

        assertEquals(1L, id1);
        assertEquals(2L, id2);
        assertEquals(3L, id3);
    }

    @Test
    @DisplayName("storeTrainee should store trainee correctly")
    void storeTrainee_ShouldStoreCorrectly() {
        Trainee trainee = new Trainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St");
        trainee.setUserId(1L);

        storage.storeTrainee(1L, trainee);

        assertEquals(1, storage.getAllTrainees().size());
        assertEquals(trainee, storage.getTrainee(1L));
    }

    @Test
    @DisplayName("getTrainee should return null for non-existent ID")
    void getTrainee_WithNonExistentId_ShouldReturnNull() {
        assertNull(storage.getTrainee(999L));
    }

    @Test
    @DisplayName("removeTrainee should remove trainee")
    void removeTrainee_ShouldRemoveTrainee() {
        Trainee trainee = new Trainee("John", "Doe");
        storage.storeTrainee(1L, trainee);

        storage.removeTrainee(1L);

        assertNull(storage.getTrainee(1L));
        assertEquals(0, storage.getAllTrainees().size());
    }

    @Test
    @DisplayName("generateTrainingId should generate unique sequential IDs")
    void generateTrainingId_ShouldGenerateUniqueSequentialIds() {
        Long id1 = storage.generateTrainingId();
        Long id2 = storage.generateTrainingId();
        Long id3 = storage.generateTrainingId();

        assertEquals(1L, id1);
        assertEquals(2L, id2);
        assertEquals(3L, id3);
    }

    @Test
    @DisplayName("storeTraining should store training correctly")
    void storeTraining_ShouldStoreCorrectly() {
        TrainingType type = new TrainingType("Cardio");
        Training training = new Training(1L, 2L, "Morning Run", type, LocalDate.now(), 60);
        training.setId(1L);

        storage.storeTraining(1L, training);

        assertEquals(1, storage.getAllTrainings().size());
        assertEquals(training, storage.getTraining(1L));
    }

    @Test
    @DisplayName("getTraining should return null for non-existent ID")
    void getTraining_WithNonExistentId_ShouldReturnNull() {
        assertNull(storage.getTraining(999L));
    }

    @Test
    @DisplayName("removeTraining should remove training")
    void removeTraining_ShouldRemoveTraining() {
        TrainingType type = new TrainingType("Cardio");
        Training training = new Training(1L, 2L, "Morning Run", type, LocalDate.now(), 60);
        storage.storeTraining(1L, training);

        storage.removeTraining(1L);

        assertNull(storage.getTraining(1L));
        assertEquals(0, storage.getAllTrainings().size());
    }

    @Test
    @DisplayName("generateTrainingTypeId should generate unique sequential IDs")
    void generateTrainingTypeId_ShouldGenerateUniqueSequentialIds() {
        Long id1 = storage.generateTrainingTypeId();
        Long id2 = storage.generateTrainingTypeId();
        Long id3 = storage.generateTrainingTypeId();

        assertEquals(1L, id1);
        assertEquals(2L, id2);
        assertEquals(3L, id3);
    }

    @Test
    @DisplayName("storeTrainingType should store training type correctly")
    void storeTrainingType_ShouldStoreCorrectly() {
        TrainingType type = new TrainingType("Yoga");
        type.setId(1L);

        storage.storeTrainingType(1L, type);

        assertEquals(1, storage.getAllTrainingTypes().size());
        assertEquals(type, storage.getTrainingType(1L));
    }

    @Test
    @DisplayName("getTrainingType should return null for non-existent ID")
    void getTrainingType_WithNonExistentId_ShouldReturnNull() {
        assertNull(storage.getTrainingType(999L));
    }

    @Test
    @DisplayName("clear should reset all storage and ID generators")
    void clear_ShouldResetAllStorageAndIdGenerators() {
        storage.storeTrainer(1L, new Trainer("John", "Doe"));
        storage.storeTrainee(1L, new Trainee("Jane", "Smith"));
        storage.storeTrainingType(1L, new TrainingType("Cardio"));

        storage.clear();

        assertAll(
                () -> assertEquals(0, storage.getAllTrainers().size()),
                () -> assertEquals(0, storage.getAllTrainees().size()),
                () -> assertEquals(0, storage.getAllTrainings().size()),
                () -> assertEquals(0, storage.getAllTrainingTypes().size()),
                () -> assertEquals(0, storage.getTotalEntities()),

                () -> assertEquals(1L, storage.generateTrainerId()),
                () -> assertEquals(1L, storage.generateTraineeId()),
                () -> assertEquals(1L, storage.generateTrainingId()),
                () -> assertEquals(1L, storage.generateTrainingTypeId())
        );
    }

    @Test
    @DisplayName("getTotalEntities should count all entities")
    void getTotalEntities_ShouldCountAllEntities() {
        storage.storeTrainer(1L, new Trainer("John", "Doe"));
        storage.storeTrainee(1L, new Trainee("Jane", "Smith"));
        storage.storeTrainingType(1L, new TrainingType("Cardio"));
        storage.storeTrainingType(2L, new TrainingType("Strength"));

        assertEquals(4, storage.getTotalEntities());
    }

    @Test
    @DisplayName("logStorageStatus should not throw exception")
    void logStorageStatus_ShouldNotThrowException() {
        storage.storeTrainer(1L, new Trainer("John", "Doe"));
        storage.storeTrainee(1L, new Trainee("Jane", "Smith"));

        assertDoesNotThrow(() -> storage.logStorageStatus());
    }

    @Test
    @DisplayName("Concurrent ID generation should be thread-safe")
    void concurrentIdGeneration_ShouldBeThreadSafe() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        storage.generateTrainerId();
                        storage.generateTraineeId();
                        storage.generateTrainingId();
                        storage.generateTrainingTypeId();
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(threadCount * operationsPerThread, successCount.get());

        Long expectedNextId = (long) (threadCount * operationsPerThread + 1);
        assertEquals(expectedNextId, storage.generateTrainerId());
        assertEquals(expectedNextId, storage.generateTraineeId());
        assertEquals(expectedNextId, storage.generateTrainingId());
        assertEquals(expectedNextId, storage.generateTrainingTypeId());

        executor.shutdown();
    }

    @Test
    @DisplayName("Concurrent storage operations should be thread-safe")
    void concurrentStorageOperations_ShouldBeThreadSafe() throws InterruptedException {
        int threadCount = 5;
        int operationsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        Long id = (long) (threadId * operationsPerThread + j + 1);

                        storage.storeTrainer(id, new Trainer("Trainer" + id, "LastName" + id));
                        storage.storeTrainee(id, new Trainee("Trainee" + id, "LastName" + id));
                        storage.storeTrainingType(id, new TrainingType("Type" + id));

                        storage.getTrainer(id);
                        storage.getTrainee(id);
                        storage.getTrainingType(id);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(15, TimeUnit.SECONDS));

        int expectedCount = threadCount * operationsPerThread;
        assertAll(
                () -> assertEquals(expectedCount, storage.getAllTrainers().size()),
                () -> assertEquals(expectedCount, storage.getAllTrainees().size()),
                () -> assertEquals(expectedCount, storage.getAllTrainingTypes().size()),
                () -> assertEquals(expectedCount * 3, storage.getTotalEntities())
        );

        executor.shutdown();
    }

    @Test
    @DisplayName("Storage maps should be separate namespaces")
    void storageMaps_ShouldBeSeparateNamespaces() {
        Long sameId = 1L;

        Trainer trainer = new Trainer("John", "Trainer");
        Trainee trainee = new Trainee("John", "Trainee");
        TrainingType type = new TrainingType("Cardio");
        Training training = new Training(1L, 2L, "Session", type, LocalDate.now(), 60);

        storage.storeTrainer(sameId, trainer);
        storage.storeTrainee(sameId, trainee);
        storage.storeTrainingType(sameId, type);
        storage.storeTraining(sameId, training);

        assertAll(
                () -> assertEquals(trainer, storage.getTrainer(sameId)),
                () -> assertEquals(trainee, storage.getTrainee(sameId)),
                () -> assertEquals(type, storage.getTrainingType(sameId)),
                () -> assertEquals(training, storage.getTraining(sameId)),
                () -> assertEquals(4, storage.getTotalEntities())
        );
    }
}