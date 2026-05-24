Feature: Retrieve chat history with deterministic pagination and sorting
  To support reliable conversation navigation
  As an authorized chat participant
  I want to fetch paginated message history ordered by timestamp

  Background:
    Given an adoption request "ar-100" exists between adopter "u-adopter-1" and shelter owner "u-shelter-1"
    And request "ar-100" is in state "ACCEPTED"
    And request "ar-100" has 6 messages with timestamps:
      | message | senderId      | timestamp                |
      | m1      | u-adopter-1   | 2026-05-24T10:00:00Z     |
      | m2      | u-shelter-1   | 2026-05-24T10:05:00Z     |
      | m3      | u-adopter-1   | 2026-05-24T10:10:00Z     |
      | m4      | u-shelter-1   | 2026-05-24T10:15:00Z     |
      | m5      | u-adopter-1   | 2026-05-24T10:20:00Z     |
      | m6      | u-shelter-1   | 2026-05-24T10:25:00Z     |

  Scenario: First page returns newest messages in descending order
    Given I am authenticated as user "u-adopter-1"
    When I request chat history for request "ar-100" with page 0 and size 2
    Then I receive 2 messages
    And messages are sorted by timestamp descending
    And the first message is "m6"
    And the second message is "m5"

  Scenario: Subsequent page returns the next slice without overlap
    Given I am authenticated as user "u-adopter-1"
    When I request chat history for request "ar-100" with page 1 and size 2
    Then I receive 2 messages
    And the messages are "m4" then "m3"
    And no message from page 0 is repeated

  Scenario: Empty page is returned when offset exceeds available messages
    Given I am authenticated as user "u-shelter-1"
    When I request chat history for request "ar-100" with page 10 and size 2
    Then I receive an empty message list

  Scenario: Invalid pagination parameters are rejected
    Given I am authenticated as user "u-adopter-1"
    When I request chat history for request "ar-100" with page -1 and size 0
    Then the operation is rejected with "BAD_REQUEST"
