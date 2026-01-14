@Integration
Feature: End-to-End User Workflows
  As a gym user
  I want to complete full workflows
  So that I can use all system features together

  Background:
    Given the system is ready for testing
    And the test message consumer is ready

  @E2E @Positive
  Scenario: Complete trainee onboarding and training session workflow
    When a trainee registers with firstName "John" and lastName "Doe"
    Then the registration should be successful
    And credentials should be generated
    When the trainee logs in with correct credentials
    Then a JWT token should be returned
    When a trainer registers with firstName "Jane" and lastName "Trainer" and specialization "Cardio"
    And the trainer logs in with correct credentials
    When trainee "john.doe" creates a training session:
      | trainingName     | Morning Cardio  |
      | traineeUsername  | john.doe        |
      | trainerUsername  | jane.trainer    |
      | trainingDate     | 2025-02-15      |
      | trainingDuration | 60              |
    Then the training should be created successfully
    And within 5 seconds a message should be received
    And the message should have actionType "ADD"

  @E2E @Positive
  Scenario: Trainer manages multiple trainees workflow
    Given a trainer "jane.trainer" is registered and logged in
    And a trainee "trainee1.test" is registered and logged in
    And a trainee "trainee2.test" is registered and logged in
    When trainee "trainee1.test" creates a training session:
      | trainingName     | Training 1      |
      | traineeUsername  | trainee1.test   |
      | trainerUsername  | jane.trainer    |
      | trainingDate     | 2025-02-15      |
      | trainingDuration | 60              |
    And trainee "trainee2.test" creates a training session:
      | trainingName     | Training 2      |
      | traineeUsername  | trainee2.test   |
      | trainerUsername  | jane.trainer    |
      | trainingDate     | 2025-02-16      |
      | trainingDuration | 45              |
    Then within 10 seconds exactly 2 messages should be received
    And all messages should have actionType "ADD"
