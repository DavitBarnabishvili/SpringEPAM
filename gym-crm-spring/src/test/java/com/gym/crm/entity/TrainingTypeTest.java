package com.gym.crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrainingType Model Tests")
class TrainingTypeTest {

    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        trainingType = new TrainingType("Yoga");
    }

    @Test
    @DisplayName("Default constructor should create empty training type")
    void defaultConstructor_ShouldCreateEmpty() {
        TrainingType newType = new TrainingType();
        assertAll(
                () -> assertNull(newType.getId()),
                () -> assertNull(newType.getTrainingTypeName())
        );
    }

    @Test
    @DisplayName("Constructor with name should set name")
    void constructorWithName_ShouldSetName() {
        assertEquals("Yoga", trainingType.getTrainingTypeName());
        assertNull(trainingType.getId());
    }

    @Test
    @DisplayName("Full constructor should set all properties")
    void fullConstructor_ShouldSetAllProperties() {
        TrainingType fullType = new TrainingType(1L, "Pilates");

        assertEquals(1L, fullType.getId());
        assertEquals("Pilates", fullType.getTrainingTypeName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"  Cardio  ", "\tStrength\n"})
    @DisplayName("Training type name should be trimmed when set")
    void setTrainingTypeName_ShouldTrimWhitespace(String name) {
        trainingType.setTrainingTypeName(name);
        assertEquals(name.trim(), trainingType.getTrainingTypeName());
    }

    @Test
    @DisplayName("Training type name should be null when set to null")
    void setTrainingTypeName_WhenNull_ShouldBeNull() {
        trainingType.setTrainingTypeName(null);
        assertNull(trainingType.getTrainingTypeName());
    }

    @Test
    @DisplayName("Equals should work correctly with IDs")
    void equals_WithIds_ShouldWorkCorrectly() {
        TrainingType type1 = new TrainingType(1L, "Cardio");
        TrainingType type2 = new TrainingType(1L, "Strength");

        assertEquals(type1, type2);
    }

    @Test
    @DisplayName("Equals should work with names when both have names")
    void equals_WithNames_ShouldWorkCorrectly() {
        TrainingType type1 = new TrainingType("Cardio");
        TrainingType type2 = new TrainingType("Cardio");

        assertEquals(type1, type2);
    }

    @Test
    @DisplayName("Equals should return false when criteria not met")
    void equals_WhenCriteriaNotMet_ShouldReturnFalse() {
        TrainingType type1 = new TrainingType("Cardio");
        TrainingType type2 = new TrainingType("Strength");

        assertNotEquals(type1, type2);
    }

    @Test
    @DisplayName("HashCode should use training type name when available")
    void hashCode_WithName_ShouldUseName() {
        assertEquals("Yoga".hashCode(), trainingType.hashCode());
    }

    @Test
    @DisplayName("HashCode should use class hash when no name")
    void hashCode_WithoutName_ShouldUseClassHash() {
        trainingType.setTrainingTypeName(null);
        assertEquals(TrainingType.class.hashCode(), trainingType.hashCode());
    }

    @Test
    @DisplayName("ToString should include training type name")
    void toString_ShouldIncludeName() {
        String result = trainingType.toString();
        assertTrue(result.contains("Yoga"));
    }
}
