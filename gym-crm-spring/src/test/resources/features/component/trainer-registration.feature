@Component
Feature: Trainer Registration and Profile Management
  As a trainer
  I want to register and manage my profile
  So that I can provide training services

  Background:
    Given the system is ready for testing

  @Positive
  Scenario: Successful trainer registration with specialization
    When a trainer registers with:
      | firstName      | Jane     |
      | lastName       | Smith    |
      | specialization | Cardio   |
    Then the registration should be successful
    And credentials should be generated
    And the username should match pattern "jane.smith"

  @Positive
  Scenario: Retrieve trainer profile
    Given a trainer is registered and logged in
    When the trainer retrieves their profile
    Then the request should be successful with status 200
    And the profile should contain trainer information

  @Positive
  Scenario: Update trainer profile successfully
    Given a trainer is registered and logged in
    When the trainer updates profile with:
      | firstName | Jane Updated |
      | lastName  | Smith Updated |
      | isActive  | true          |
    Then the profile update should be successful

  @Positive
  Scenario: Activate and deactivate trainer profile
    Given a trainer is registered and logged in
    When the trainer deactivates their profile
    Then the request should be successful with status 200
    When the trainer activates their profile
    Then the request should be successful with status 200

  @Negative
  Scenario: Registration fails with invalid specialization
    When a trainer registers with invalid specialization "InvalidType"
    Then the registration should fail with status 400

  @Negative
  Scenario: Registration fails with empty firstName
    When a trainer registers with empty firstName and lastName "Test"
    Then the registration should fail with status 400
    And the error message should contain "First name"
