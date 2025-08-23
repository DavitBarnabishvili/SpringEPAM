package com.gym.crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trainer Model Tests")
class TrainerTest {

    private Trainer trainer;
    private TrainingType specialization;

    @BeforeEach
    void setUp() {
        specialization = new TrainingType("Cardio");
        trainer = new Trainer("Jane", "Smith", specialization);
    }

    @Test
    @DisplayName("Default constructor should set isActive to true")
    void defaultConstructor_ShouldSetIsActiveToTrue() {
        Trainer newTrainer = new Trainer();
        assertTrue(newTrainer.getIsActive());
    }

    @Test
    @DisplayName("Constructor with names should set properties correctly")
    void constructorWithNames_ShouldSetPropertiesCorrectly() {
        Trainer newTrainer = new Trainer("John", "Doe");

        assertEquals("John", newTrainer.getFirstName());
        assertEquals("Doe", newTrainer.getLastName());
        assertTrue(newTrainer.getIsActive());
        assertNull(newTrainer.getSpecialization());
    }

    @Test
    @DisplayName("Constructor with specialization should set all properties")
    void constructorWithSpecialization_ShouldSetAllProperties() {
        assertEquals("Jane", trainer.getFirstName());
        assertEquals("Smith", trainer.getLastName());
        assertEquals(specialization, trainer.getSpecialization());
        assertTrue(trainer.getIsActive());
    }

    @Test
    @DisplayName("Internal constructor should set all properties")
    void internalConstructor_ShouldSetAllProperties() {
        Trainer fullTrainer = new Trainer(1L, "Jane", "Smith", "jane.smith",
                "password123", true, specialization);

        assertEquals(1L, fullTrainer.getUserId());
        assertEquals("Jane", fullTrainer.getFirstName());
        assertEquals("Smith", fullTrainer.getLastName());
        assertEquals("jane.smith", fullTrainer.getUsername());
        assertEquals("password123", fullTrainer.getPassword());
        assertTrue(fullTrainer.getIsActive());
        assertEquals(specialization, fullTrainer.getSpecialization());
    }

    @Test
    @DisplayName("getSpecializationName should return training type name")
    void getSpecializationName_WithSpecialization_ShouldReturnName() {
        assertEquals("Cardio", trainer.getSpecializationName());
    }

    @Test
    @DisplayName("getSpecializationName should return default when no specialization")
    void getSpecializationName_WithoutSpecialization_ShouldReturnDefault() {
        trainer.setSpecialization(null);
        assertEquals("No Specialization", trainer.getSpecializationName());
    }

    @Test
    @DisplayName("hasSpecialization should return true when specialization exists")
    void hasSpecialization_WhenExists_ShouldReturnTrue() {
        assertTrue(trainer.hasSpecialization());
    }

    @Test
    @DisplayName("hasSpecialization should return false when specialization is null")
    void hasSpecialization_WhenNull_ShouldReturnFalse() {
        trainer.setSpecialization(null);
        assertFalse(trainer.hasSpecialization());
    }

    @Test
    @DisplayName("Equals should work correctly with userIds")
    void equals_WithUserIds_ShouldWorkCorrectly() {
        Trainer trainer1 = new Trainer();
        Trainer trainer2 = new Trainer();

        trainer1.setUserId(1L);
        trainer2.setUserId(1L);

        assertEquals(trainer1, trainer2);
    }

    @Test
    @DisplayName("HashCode should use userId when available")
    void hashCode_WithUserId_ShouldUseUserId() {
        trainer.setUserId(456L);
        assertEquals(456L, trainer.hashCode());
    }

    @Test
    @DisplayName("ToString should include all relevant information")
    void toString_ShouldIncludeAllRelevantInfo() {
        trainer.setUsername("jane.smith");
        String result = trainer.toString();

        assertAll(
                () -> assertTrue(result.contains("Jane")),
                () -> assertTrue(result.contains("Smith")),
                () -> assertTrue(result.contains("jane.smith")),
                () -> assertTrue(result.contains("Cardio")),
                () -> assertTrue(result.contains("true"))
        );
    }
}
