@Component
Feature: Trainee Registration and Profile Management
  As a trainee
  I want to register and manage my profile
  So that I can use the gym services

  Background:
    Given the system is ready for testing

  @Positive
  Scenario: Successful trainee registration with all fields
    When a trainee registers with:
      | firstName   | John          |
      | lastName    | Doe           |
      | dateOfBirth | 1990-01-15    |
      | address     | 123 Main St   |
    Then the registration should be successful
    And credentials should be generated
    And the username should match pattern "john.doe"
    And a random password should be returned

  @Positive
  Scenario: Successful trainee registration with minimal fields
    When a trainee registers with firstName "Jane" and lastName "Smith"
    Then the registration should be successful
    And the username should be "jane.smith"

  @Positive
  Scenario: Username auto-increment for duplicate names
    Given a trainee is registered with firstName "John" and lastName "Doe"
    When another trainee registers with firstName "John" and lastName "Doe"
    Then the registration should be successful
    And the new username should be "john.doe1"

  @Positive
  Scenario: Retrieve trainee profile
    Given a trainee is registered and logged in
    When the trainee retrieves their profile
    Then the request should be successful with status 200
    And the profile should contain personal information

  @Positive
  Scenario: Update trainee profile successfully
    Given a trainee is registered and logged in
    When the trainee updates profile with:
      | firstName   | John Updated     |
      | lastName    | Doe Updated      |
      | dateOfBirth | 1990-01-15       |
      | address     | 456 New St       |
      | isActive    | true             |
    Then the profile update should be successful
    And the updated profile should reflect new values

  @Negative @Security
  Scenario: Trainee cannot access another trainee's profile
    Given a trainee "john.doe" is registered and logged in
    And a trainee "jane.smith" is registered
    When trainee "john.doe" attempts to retrieve profile of "jane.smith"
    Then the request should fail with status 403

  @Positive
  Scenario: Activate and deactivate trainee profile
    Given a trainee is registered and logged in
    When the trainee deactivates their profile
    Then the request should be successful with status 200
    When the trainee activates their profile
    Then the request should be successful with status 200

  @Negative
  Scenario: Registration fails with invalid data
    When a trainee registers with empty firstName
    Then the registration should fail with status 400
    And the error message should contain "First name"
