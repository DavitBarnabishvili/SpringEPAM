package com.gym.crm.util.impl;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.util.CredentialsGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialsGeneratorServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    private CredentialsGeneratorService credentialsGenerator;

    @BeforeEach
    void setUp() {
        credentialsGenerator = new CredentialsGeneratorServiceImpl(traineeDao, trainerDao);
    }

    @Test
    void generateUsername_ShouldReturnBaseUsername_WhenUnique() {
        when(traineeDao.existsByUsername("John.Doe")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Doe")).thenReturn(false);

        String username = credentialsGenerator.generateUsername("John", "Doe");

        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void generateUsername_ShouldAddSerialNumber_WhenDuplicateExists() {
        when(traineeDao.existsByUsername("John.Doe")).thenReturn(true);
        when(trainerDao.existsByUsername("John.Doe")).thenReturn(false);
        when(traineeDao.existsByUsername("John.Doe1")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Doe1")).thenReturn(false);

        String username = credentialsGenerator.generateUsername("John", "Doe");

        assertThat(username).isEqualTo("John.Doe1");
    }

    @Test
    void generateUsername_ShouldIncrementSerialNumber_WhenMultipleDuplicates() {
        when(traineeDao.existsByUsername("John.Doe")).thenReturn(true);
        when(trainerDao.existsByUsername("John.Doe")).thenReturn(false);
        when(traineeDao.existsByUsername("John.Doe1")).thenReturn(true);
        when(trainerDao.existsByUsername("John.Doe1")).thenReturn(false);
        when(traineeDao.existsByUsername("John.Doe2")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Doe2")).thenReturn(true);
        when(traineeDao.existsByUsername("John.Doe3")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Doe3")).thenReturn(false);

        String username = credentialsGenerator.generateUsername("John", "Doe");

        assertThat(username).isEqualTo("John.Doe3");
    }

    @Test
    void generateUsername_ShouldTrimNames() {
        when(traineeDao.existsByUsername("John.Doe")).thenReturn(false);
        when(trainerDao.existsByUsername("John.Doe")).thenReturn(false);

        String username = credentialsGenerator.generateUsername("  John  ", "  Doe  ");

        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void generateUsername_ShouldThrowException_WhenFirstNameNull() {
        assertThatThrownBy(() -> credentialsGenerator.generateUsername(null, "Doe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name are required for username generation");
    }

    @Test
    void generateUsername_ShouldThrowException_WhenLastNameNull() {
        assertThatThrownBy(() -> credentialsGenerator.generateUsername("John", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name are required for username generation");
    }

    @Test
    void generateUsername_ShouldThrowException_WhenFirstNameEmpty() {
        assertThatThrownBy(() -> credentialsGenerator.generateUsername("", "Doe"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name are required for username generation");
    }

    @Test
    void generateUsername_ShouldThrowException_WhenLastNameEmpty() {
        assertThatThrownBy(() -> credentialsGenerator.generateUsername("John", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name are required for username generation");
    }

    @Test
    void generateUsername_ShouldThrowException_WhenBothNamesEmpty() {
        assertThatThrownBy(() -> credentialsGenerator.generateUsername("  ", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name are required for username generation");
    }

    @Test
    void generatePassword_ShouldReturnPasswordOfCorrectLength() {
        String password = credentialsGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }

    @Test
    void generatePassword_ShouldReturnDifferentPasswords() {
        String password1 = credentialsGenerator.generatePassword();
        String password2 = credentialsGenerator.generatePassword();
        String password3 = credentialsGenerator.generatePassword();

        assertThat(password1).isNotEqualTo(password2);
        assertThat(password2).isNotEqualTo(password3);
        assertThat(password1).isNotEqualTo(password3);
    }

    @Test
    void generatePassword_ShouldContainValidCharacters() {
        String password = credentialsGenerator.generatePassword();
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        assertThat(password).matches("^[" + validChars + "]+$");
    }

    @Test
    void isUsernameUnique_ShouldReturnTrue_WhenUsernameNotExists() {
        when(traineeDao.existsByUsername("unique.username")).thenReturn(false);
        when(trainerDao.existsByUsername("unique.username")).thenReturn(false);

        boolean unique = credentialsGenerator.isUsernameUnique("unique.username");

        assertThat(unique).isTrue();
    }

    @Test
    void isUsernameUnique_ShouldReturnFalse_WhenExistsInTrainees() {
        when(traineeDao.existsByUsername("john.doe")).thenReturn(true);
        when(trainerDao.existsByUsername("john.doe")).thenReturn(false);

        boolean unique = credentialsGenerator.isUsernameUnique("john.doe");

        assertThat(unique).isFalse();
    }

    @Test
    void isUsernameUnique_ShouldReturnFalse_WhenExistsInTrainers() {
        when(traineeDao.existsByUsername("jane.smith")).thenReturn(false);
        when(trainerDao.existsByUsername("jane.smith")).thenReturn(true);

        boolean unique = credentialsGenerator.isUsernameUnique("jane.smith");

        assertThat(unique).isFalse();
    }

    @Test
    void isUsernameUnique_ShouldReturnFalse_WhenExistsInBoth() {
        when(traineeDao.existsByUsername("duplicate")).thenReturn(true);
        when(trainerDao.existsByUsername("duplicate")).thenReturn(true);

        boolean unique = credentialsGenerator.isUsernameUnique("duplicate");

        assertThat(unique).isFalse();
    }

    @Test
    void isUsernameUnique_ShouldReturnFalse_WhenUsernameNull() {
        boolean unique = credentialsGenerator.isUsernameUnique(null);

        assertThat(unique).isFalse();
    }

    @Test
    void isUsernameUnique_ShouldReturnFalse_WhenUsernameEmpty() {
        boolean unique = credentialsGenerator.isUsernameUnique("");

        assertThat(unique).isFalse();
    }

    @Test
    void isUsernameUnique_ShouldTrimUsername() {
        when(traineeDao.existsByUsername("username")).thenReturn(false);
        when(trainerDao.existsByUsername("username")).thenReturn(false);

        boolean unique = credentialsGenerator.isUsernameUnique("  username  ");

        assertThat(unique).isTrue();
    }
}