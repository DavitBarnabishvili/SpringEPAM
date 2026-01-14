@Component
Feature: User Authentication
  As a user of the Gym CRM system
  I want to authenticate and manage my credentials
  So that I can securely access the system

  Background:
    Given the system is ready for testing

  @Positive
  Scenario: Successful trainee login with valid credentials
    Given a trainee is registered with firstName "John" and lastName "Doe"
    When the trainee logs in with correct credentials
    Then the login should be successful
    And a JWT token should be returned

  @Positive
  Scenario: Successful trainer login with valid credentials
    Given a trainer is registered with firstName "Jane" and lastName "Smith" and specialization "Cardio"
    When the trainer logs in with correct credentials
    Then the login should be successful
    And a JWT token should be returned

  @Negative
  Scenario: Failed login with invalid password
    Given a trainee is registered with firstName "John" and lastName "Doe"
    When the trainee attempts to login with username "john.doe" and incorrect password "wrongPassword"
    Then the login should fail with status 401
    And the error message should contain "Invalid username or password"

  @Negative
  Scenario: Failed login with non-existent username
    When a user attempts to login with username "nonexistent.user" and password "password123"
    Then the login should fail with status 401

  @Positive
  Scenario: Change password successfully
    Given a trainee is registered and logged in
    When the trainee changes password to "NewPass123"
    Then the password change should be successful
    And the trainee should be able to login with the new password

  @Positive
  Scenario: Successful logout invalidates token
    Given a trainee is registered and logged in
    When the trainee logs out
    Then the logout should be successful
    When attempting to access protected endpoint with the same token
    Then the request should fail with status 401

  @Negative @Security
  Scenario: Brute force protection locks account after 3 failed attempts
    Given a trainee is registered with firstName "John" and lastName "Doe"
    When the trainee fails to login 3 times with wrong password
    Then subsequent login attempt should fail with status 423
    And the error message should contain "locked"
