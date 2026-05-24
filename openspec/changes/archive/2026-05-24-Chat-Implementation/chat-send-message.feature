Feature: Send chat messages for accepted adoption requests
  To enable secure adopter-shelter communication
  As an authenticated participant
  I want to send a message only in a valid accepted request chat

  Background:
    Given an adoption request "ar-100" exists between adopter "u-adopter-1" and shelter owner "u-shelter-1"
    And the adoption request "ar-100" is in state "ACCEPTED"

  Scenario: Adopter sends a message successfully
    Given I am authenticated as user "u-adopter-1"
    When I send a message "Hola, ¿cómo está Luna hoy?" to request "ar-100"
    Then the message is persisted under request "ar-100"
    And the message sender is "u-adopter-1"
    And the message has a server-generated timestamp

  Scenario: Shelter owner sends a message successfully
    Given I am authenticated as user "u-shelter-1"
    When I send a message "Está muy bien, comió perfecto" to request "ar-100"
    Then the message is persisted under request "ar-100"
    And the message sender is "u-shelter-1"
    And the message has a server-generated timestamp

  Scenario: Message content larger than allowed limit is rejected
    Given I am authenticated as user "u-adopter-1"
    And my message content length is 1001 characters
    When I send the message to request "ar-100"
    Then the operation is rejected with validation error
    And no message is persisted
