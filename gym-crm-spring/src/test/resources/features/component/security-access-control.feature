@Component @Security
Feature: Security and Access Control
  As a system administrator
  I want to enforce role-based access control
  So that users can only access authorized resources

  Background:
    Given the system is ready for testing

  @Positive
  Scenario: Trainee can access their own resources
    Given a trainee "john.doe" is registered and logged in
    When trainee "john.doe" accesses their own profile
    Then the request should be successful with status 200

  @Positive
  Scenario: Trainer can access their own resources
    Given a trainer "jane.trainer" is registered and logged in
    When trainer "jane.trainer" accesses their own profile
    Then the request should be successful with status 200

  @Negative
  Scenario: Unauthenticated access to protected endpoints fails
    When an unauthenticated user accesses "/api/trainees/profile/john.doe"
    Then the request should fail with status 401

  @Positive
  Scenario: Public endpoints are accessible without authentication
    When an unauthenticated user accesses "/api/training-types"
    Then the request should be successful with status 200
