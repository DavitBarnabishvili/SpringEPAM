package com.gym.crm.util.impl;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CredentialsGeneratorServiceImpl Tests")
class CredentialsGeneratorServiceImplTest {

    @Mock
    private InMemoryStorage mockStorage;

    private CredentialsGeneratorServiceImpl credentialsGenerator;

    @BeforeEach
    void setUp() {
        credentialsGenerator = new CredentialsGeneratorServiceImpl(mockStorage);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    @DisplayName("generateUsername should throw exception for invalid first name")
    void generateUsername_WithInvalidFirstName_ShouldThrowException(String firstName) {
        assertThrows(IllegalArgumentException.class,
                () -> credentialsGenerator.generateUsername(firstName, "Doe"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    @DisplayName("generateUsername should throw exception for invalid last name")
    void generateUsername_WithInvalidLastName_ShouldThrowException(String lastName) {
        assertThrows(IllegalArgumentException.class,
                () -> credentialsGenerator.generateUsername("John", lastName));
    }

    @Test
    @DisplayName("generateUsername should create base username when unique")
    void generateUsername_WhenUnique_ShouldCreateBaseUsername() {
        when(mockStorage.getAllTrainers()).thenReturn(List.of());
        when(mockStorage.getAllTrainees()).thenReturn(List.of());

        String result = credentialsGenerator.generateUsername("John", "Doe");

        assertEquals("John.Doe", result);
    }

    @Test
    @DisplayName("generateUsername should handle whitespace in names")
    void generateUsername_WithWhitespace_ShouldTrimNames() {
        when(mockStorage.getAllTrainers()).thenReturn(List.of());
        when(mockStorage.getAllTrainees()).thenReturn(List.of());

        String result = credentialsGenerator.generateUsername("  John  ", "  Doe  ");

        assertEquals("John.Doe", result);
    }

    @Test
    @DisplayName("generateUsername should add serial number when duplicate exists")
    void generateUsername_WhenDuplicateExists_ShouldAddSerialNumber() {
        Trainer existingTrainer = new Trainer("John", "Doe");
        existingTrainer.setUsername("John.Doe");

        when(mockStorage.getAllTrainers()).thenReturn(List.of(existingTrainer));
        when(mockStorage.getAllTrainees()).thenReturn(List.of());

        String result = credentialsGenerator.generateUsername("John", "Doe");

        assertEquals("John.Doe1", result);
    }

    @Test
    @DisplayName("generateUsername should handle multiple duplicates")
    void generateUsername_WithMultipleDuplicates_ShouldIncrementSerialNumber() {
        Trainer trainer1 = new Trainer("John", "Doe");
        trainer1.setUsername("John.Doe");

        Trainer trainer2 = new Trainer("John", "Doe");
        trainer2.setUsername("John.Doe1");

        Trainee trainee1 = new Trainee("John", "Doe");
        trainee1.setUsername("John.Doe2");

        when(mockStorage.getAllTrainers()).thenReturn(List.of(trainer1, trainer2));
        when(mockStorage.getAllTrainees()).thenReturn(List.of(trainee1));

        String result = credentialsGenerator.generateUsername("John", "Doe");

        assertEquals("John.Doe3", result);
    }

    @Test
    @DisplayName("generateUsername should check both trainers and trainees")
    void generateUsername_ShouldCheckBothTrainersAndTrainees() {
        Trainee existingTrainee = new Trainee("Jane", "Smith");
        existingTrainee.setUsername("Jane.Smith");

        when(mockStorage.getAllTrainers()).thenReturn(List.of());
        when(mockStorage.getAllTrainees()).thenReturn(List.of(existingTrainee));

        String result = credentialsGenerator.generateUsername("Jane", "Smith");

        assertEquals("Jane.Smith1", result);
    }

    @Test
    @DisplayName("generatePassword should return 10 character password")
    void generatePassword_ShouldReturn10CharacterPassword() {
        String password = credentialsGenerator.generatePassword();
        assertEquals(10, password.length());
    }

    @Test
    @DisplayName("generatePassword should contain only valid characters")
    void generatePassword_ShouldContainOnlyValidCharacters() {
        String password = credentialsGenerator.generatePassword();
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (char c : password.toCharArray()) {
            assertTrue(validChars.indexOf(c) >= 0,
                    "Password contains invalid character: " + c);
        }
    }

    @Test
    @DisplayName("generatePassword should generate different passwords")
    void generatePassword_ShouldGenerateDifferentPasswords() {
        String password1 = credentialsGenerator.generatePassword();
        String password2 = credentialsGenerator.generatePassword();
        String password3 = credentialsGenerator.generatePassword();

        assertNotEquals(password1, password2);
        assertNotEquals(password2, password3);
        assertNotEquals(password1, password3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t\n"})
    @DisplayName("isUsernameUnique should return false for invalid username")
    void isUsernameUnique_WithInvalidUsername_ShouldReturnFalse(String username) {
        assertFalse(credentialsGenerator.isUsernameUnique(username));
    }

    @Test
    @DisplayName("isUsernameUnique should return true when username is unique")
    void isUsernameUnique_WhenUnique_ShouldReturnTrue() {
        when(mockStorage.getAllTrainers()).thenReturn(List.of());
        when(mockStorage.getAllTrainees()).thenReturn(List.of());

        assertTrue(credentialsGenerator.isUsernameUnique("unique.username"));
    }

    @Test
    @DisplayName("isUsernameUnique should return false when trainer has username")
    void isUsernameUnique_WhenTrainerHasUsername_ShouldReturnFalse() {
        Trainer existingTrainer = new Trainer("John", "Doe");
        existingTrainer.setUsername("john.doe");

        when(mockStorage.getAllTrainers()).thenReturn(List.of(existingTrainer));
        when(mockStorage.getAllTrainees()).thenReturn(List.of());

        assertFalse(credentialsGenerator.isUsernameUnique("john.doe"));
    }

    @Test
    @DisplayName("isUsernameUnique should return false when trainee has username")
    void isUsernameUnique_WhenTraineeHasUsername_ShouldReturnFalse() {
        Trainee existingTrainee = new Trainee("Jane", "Smith");
        existingTrainee.setUsername("jane.smith");

        when(mockStorage.getAllTrainers()).thenReturn(List.of());
        when(mockStorage.getAllTrainees()).thenReturn(List.of(existingTrainee));

        assertFalse(credentialsGenerator.isUsernameUnique("jane.smith"));
    }

    @Test
    @DisplayName("isUsernameUnique should handle whitespace")
    void isUsernameUnique_ShouldHandleWhitespace() {
        when(mockStorage.getAllTrainers()).thenReturn(List.of());
        when(mockStorage.getAllTrainees()).thenReturn(List.of());

        assertTrue(credentialsGenerator.isUsernameUnique("  unique.username  "));
    }
}