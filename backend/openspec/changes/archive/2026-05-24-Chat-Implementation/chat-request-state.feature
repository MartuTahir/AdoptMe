Feature: Restrict chat usage to accepted adoption requests
  To enforce business rules for secure communication timing
  As the chat service
  I only allow chat actions when request status is ACCEPTED

  Scenario Outline: Sending messages is blocked for non-accepted states
    Given an adoption request "<requestId>" exists between adopter "u-adopter-1" and shelter owner "u-shelter-1"
    And the adoption request "<requestId>" is in state "<state>"
    And I am authenticated as user "u-adopter-1"
    When I send a message "¿Podemos coordinar visita?" to request "<requestId>"
    Then the operation is rejected with "BAD_REQUEST"
    And the error message indicates chat is allowed only for ACCEPTED requests
    And no message is persisted

    Examples:
      | requestId | state    |
      | ar-301    | PENDING  |
      | ar-302    | REJECTED |

  Scenario Outline: Retrieving chat history is blocked for non-accepted states
    Given an adoption request "<requestId>" exists between adopter "u-adopter-1" and shelter owner "u-shelter-1"
    And the adoption request "<requestId>" is in state "<state>"
    And I am authenticated as user "u-shelter-1"
    When I request chat history for request "<requestId>" with page 0 and size 20
    Then the operation is rejected with "BAD_REQUEST"
    And the error message indicates chat is allowed only for ACCEPTED requests

    Examples:
      | requestId | state    |
      | ar-401    | PENDING  |
      | ar-402    | REJECTED |
