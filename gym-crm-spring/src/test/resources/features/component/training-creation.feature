@Component
Feature: Training Creation and Management
  As a trainee or trainer
  I want to create and manage training sessions
  So that I can track my workout activities

  Background:
    Given the system is ready for testing

  @Positive
  Scenario: Create training successfully
    Given a trainee "john.doe" is registered and logged in
    And a trainer "jane.trainer" is registered and logged in
    When trainee "john.doe" creates a training with:
      | trainingName     | Morning Cardio  |
      | traineeUsername  | john.doe        |
      | trainerUsername  | jane.trainer    |
      | trainingDate     | 2025-02-15      |
      | trainingDuration | 60              |
    Then the training creation should be successful

  @Positive
  Scenario: Retrieve trainee's training list
    Given a trainee "john.doe" is registered and logged in
    And a trainer "jane.trainer" is registered and logged in
    And trainee "john.doe" has created a training with trainer "jane.trainer"
    When trainee "john.doe" retrieves their trainings
    Then the request should be successful with status 200
    And the training list should not be empty

  @Negative
  Scenario: Cannot create training with invalid duration
    Given a trainee "john.doe" is registered and logged in
    And a trainer "jane.trainer" is registered and logged in
    When trainee creates a training with duration 500 minutes
    Then the training creation should fail with status 400
    And the error message should contain "duration"

  @Negative @Security
  Scenario: Unauthorized user cannot create training
    When an unauthenticated user attempts to create a training
    Then the request should fail with status 401

  @Positive
  Scenario: Get training types list
    When a user retrieves the list of training types
    Then the request should be successful with status 200
    And the training types should include "Cardio"
