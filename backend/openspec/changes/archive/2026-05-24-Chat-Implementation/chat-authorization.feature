Feature: Authorize chat participation by request ownership
  To protect sensitive adopter-shelter communication
  As the chat service
  I must only allow the request adopter and the pet shelter owner

  Background:
    Given an adoption request "ar-100" exists for pet "p-10"
    And adopter "u-adopter-1" created request "ar-100"
    And pet "p-10" belongs to shelter owner "u-shelter-1"
    And request "ar-100" is in state "ACCEPTED"

  Scenario: Unrelated authenticated user cannot send a message
    Given I am authenticated as user "u-random-9"
    When I send a message "Quiero sumarme" to request "ar-100"
    Then the operation is rejected with "FORBIDDEN"
    And no message is persisted for request "ar-100"

  Scenario: Unrelated authenticated user cannot retrieve chat history
    Given I am authenticated as user "u-random-9"
    When I request chat history for request "ar-100" with page 0 and size 20
    Then the operation is rejected with "FORBIDDEN"

  Scenario: Participant cannot access another request by guessing ID
    Given an adoption request "ar-200" exists between adopter "u-adopter-2" and shelter owner "u-shelter-2"
    And request "ar-200" is in state "ACCEPTED"
    And I am authenticated as user "u-adopter-1"
    When I send a message "¿Hay novedades?" to request "ar-200"
    Then the operation is rejected with "FORBIDDEN"
    And no message is persisted for request "ar-200" from "u-adopter-1"

  Scenario: Access using non-existent request id returns not found
    Given I am authenticated as user "u-adopter-1"
    When I request chat history for request "ar-999" with page 0 and size 20
    Then the operation is rejected with "NOT_FOUND"
