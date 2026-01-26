@Integration
Feature: Workload Messaging Integration
  As the gym CRM system
  I want to send trainer workload updates to ActiveMQ
  So that the workload microservice can process them asynchronously

  Background:
    Given the system is ready for testing
    And the test message consumer is ready

  @Positive @Async
  Scenario: Training creation sends ADD workload message to ActiveMQ
    Given a trainee "john.doe" is registered and logged in
    And a trainer "jane.trainer" is registered and logged in
    When trainee "john.doe" creates a training session:
      | trainingName     | Morning Cardio  |
      | traineeUsername  | john.doe        |
      | trainerUsername  | jane.trainer    |
      | trainingDate     | 2025-02-15      |
      | trainingDuration | 60              |
    Then the training should be created successfully
    And within 5 seconds a message should be received
    And the message should have actionType "ADD"
    And the message should contain trainerUsername "jane.trainer"
    And the message should contain trainingDuration 60
    And the message should have a valid transactionId

  @Positive @Async
  Scenario: Multiple trainings send multiple messages
    Given a trainee "john.doe" is registered and logged in
    And a trainer "jane.trainer" is registered and logged in
    When trainee "john.doe" creates 3 training sessions with trainer "jane.trainer"
    Then within 10 seconds exactly 3 messages should be received
    And all messages should have actionType "ADD"
