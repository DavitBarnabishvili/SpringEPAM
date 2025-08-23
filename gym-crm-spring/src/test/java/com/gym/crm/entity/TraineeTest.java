package com.gym.crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trainee Model Tests")
class TraineeTest {

    private Trainee trainee;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(1990, 5, 15);
        trainee = new Trainee("John", "Doe", testDate, "123 Main St");
    }

    @Test
    @DisplayName("Default constructor should set isActive to true")
    void defaultConstructor_ShouldSetIsActiveToTrue() {
        Trainee newTrainee = new Trainee();
        assertTrue(newTrainee.getIsActive());
    }

    @Test
    @DisplayName("Constructor with names should set properties correctly")
    void constructorWithNames_ShouldSetPropertiesCorrectly() {
        Trainee newTrainee = new Trainee("Jane", "Smith");

        assertEquals("Jane", newTrainee.getFirstName());
        assertEquals("Smith", newTrainee.getLastName());
        assertTrue(newTrainee.getIsActive());
        assertNull(newTrainee.getDateOfBirth());
        assertNull(newTrainee.getAddress());
    }

    @Test
    @DisplayName("Full constructor should set all properties")
    void fullConstructor_ShouldSetAllProperties() {
        assertEquals("John", trainee.getFirstName());
        assertEquals("Doe", trainee.getLastName());
        assertEquals(testDate, trainee.getDateOfBirth());
        assertEquals("123 Main St", trainee.getAddress());
        assertTrue(trainee.getIsActive());
    }

    @Test
    @DisplayName("Internal constructor should set all properties including credentials")
    void internalConstructor_ShouldSetAllProperties() {
        Trainee fullTrainee = new Trainee(1L, "John", "Doe", "john.doe",
                "password123", true, testDate, "123 Main St");

        assertEquals(1L, fullTrainee.getUserId());
        assertEquals("John", fullTrainee.getFirstName());
        assertEquals("Doe", fullTrainee.getLastName());
        assertEquals("john.doe", fullTrainee.getUsername());
        assertEquals("password123", fullTrainee.getPassword());
        assertTrue(fullTrainee.getIsActive());
        assertEquals(testDate, fullTrainee.getDateOfBirth());
        assertEquals("123 Main St", fullTrainee.getAddress());
    }

    @Test
    @DisplayName("Age calculation should be correct")
    void getAge_ShouldCalculateCorrectly() {
        LocalDate birthDate = LocalDate.now().minusYears(25).minusMonths(3);
        trainee.setDateOfBirth(birthDate);

        assertEquals(25, trainee.getAge());
    }

    @Test
    @DisplayName("Age should be null when date of birth is null")
    void getAge_WhenDateOfBirthIsNull_ShouldReturnNull() {
        trainee.setDateOfBirth(null);
        assertNull(trainee.getAge());
    }

    @ParameterizedTest
    @ValueSource(strings = {"  123 Main St  ", "\t456 Oak Ave\n"})
    @DisplayName("Address should be trimmed when set")
    void setAddress_ShouldTrimWhitespace(String address) {
        trainee.setAddress(address);
        assertEquals(address.trim(), trainee.getAddress());
    }

    @Test
    @DisplayName("Address should be null when set to null")
    void setAddress_WhenNull_ShouldBeNull() {
        trainee.setAddress(null);
        assertNull(trainee.getAddress());
    }

    @Test
    @DisplayName("hasDateOfBirth should return true when date exists")
    void hasDateOfBirth_WhenDateExists_ShouldReturnTrue() {
        assertTrue(trainee.hasDateOfBirth());
    }

    @Test
    @DisplayName("hasDateOfBirth should return false when date is null")
    void hasDateOfBirth_WhenDateIsNull_ShouldReturnFalse() {
        trainee.setDateOfBirth(null);
        assertFalse(trainee.hasDateOfBirth());
    }

    @Test
    @DisplayName("hasAddress should return true when address exists")
    void hasAddress_WhenAddressExists_ShouldReturnTrue() {
        assertTrue(trainee.hasAddress());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    @DisplayName("hasAddress should return false for null or empty address")
    void hasAddress_WhenAddressIsNullOrEmpty_ShouldReturnFalse(String address) {
        trainee.setAddress(address);
        assertFalse(trainee.hasAddress());
    }

    @Test
    @DisplayName("Equals should work correctly with userIds")
    void equals_WithUserIds_ShouldWorkCorrectly() {
        Trainee trainee1 = new Trainee();
        Trainee trainee2 = new Trainee();

        trainee1.setUserId(1L);
        trainee2.setUserId(1L);

        assertEquals(trainee1, trainee2);
    }

    @Test
    @DisplayName("Equals should fall back to superclass when no userId")
    void equals_WithoutUserId_ShouldFallBackToSuperclass() {
        Trainee trainee1 = new Trainee("John", "Doe");
        Trainee trainee2 = new Trainee("John", "Doe");

        trainee1.setUsername("john.doe");
        trainee2.setUsername("john.doe");

        assertEquals(trainee1, trainee2);
    }

    @Test
    @DisplayName("HashCode should use userId when available")
    void hashCode_WithUserId_ShouldUseUserId() {
        trainee.setUserId(123L);
        assertEquals(123L, trainee.hashCode());
    }

    @Test
    @DisplayName("HashCode should fall back to superclass when no userId")
    void hashCode_WithoutUserId_ShouldFallBackToSuperclass() {
        trainee.setUsername("john.doe");
        assertEquals("john.doe".hashCode(), trainee.hashCode());
    }

    @Test
    @DisplayName("ToString should include all relevant information")
    void toString_ShouldIncludeAllRelevantInfo() {
        trainee.setUsername("john.doe");
        String result = trainee.toString();

        assertAll(
                () -> assertTrue(result.contains("John")),
                () -> assertTrue(result.contains("Doe")),
                () -> assertTrue(result.contains("john.doe")),
                () -> assertTrue(result.contains("123 Main St")),
                () -> assertTrue(result.contains("true"))
        );
    }
}