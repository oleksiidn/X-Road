Feature: 0100 - Signer: HardwareToken
  Uses SoftHSM to emulate hardware token.

  Scenario: HSM is operational
    * HSM is operational

#
#  Scenario: Initialization
#    Given tokens list contains token "0"
#    And token "0" status is "NOT_INITIALIZED"
#    When signer is initialized with pin "1234"
#    Then token "0" is not active
#    And token "0" status is "OK"
#
#  Scenario: Activate token
#    Given token "0" is not active
#    When token "0" is logged in with pin "1234"
#    Then token "0" is active
#
#  Scenario: Deactivate token
#    When token "0" is logged out
#    Then token "0" is not active
#
#  Scenario: Update token pin
#    Given token "0" is not active
#    And token "0" is logged in with pin "1234"
#    When token "0" pin is updated from "1234" to "4321"
#    And token "0" is logged in with pin "4321"
#    Then token "0" is active
#
#  Scenario: Set token friendly name
#    When name "New friendly name" is set for token "0"
#    Then token "0" name is "New friendly name"
#
#  Scenario: Key generation
#    When new key "key-1" generated for token "0"
#    And name "First key" is set for generated key
#    When new key "key-2" generated for token "0"
#    And name "Second key" is set for generated key
#    When new key "key-3" generated for token "0"
#    And name "Third key" is set for generated key
#    Then token "0" has exact keys "First key,Second key,Third key"
#    And sign mechanism for token "0" key "Second key" is not null
#
#  Scenario: Delete key
#    Given new key "key-X" generated for token "0"
#    And name "KeyX" is set for generated key
#    Then token info can be retrieved by key id
#    When key "Third key" is deleted from token "0"
#    Then token "0" has exact keys "First key,Second key,KeyX"
#
#  Scenario: Sign
#    Given digest can be signed using key "KeyX" from token "0"
#    And Signing with unknown algorithm fails using key "KeyX" from token "0"
#
#  Scenario: Generate/Regenerate cert request
#    When cert request is generated for token "0" key "Second key" for client "cs:test:member-2"
#    And token and key can be retrieved by cert request
#    Then cert request can be deleted
#    When cert request is generated for token "0" key "Second key" for client "cs:test:member-2"
#    And cert request is regenerated
#
#  Scenario: Certificate can be (re)imported
#    Given tokens list contains token "0"
#    When Wrong Certificate is not imported for client "cs:test:member-1"
#    And self signed cert generated for token "0" key "First key", client "cs:test:member-1"
#    And certificate info can be retrieved by cert hash
#    When certificate can be deleted
#    Then token "0" key "First key" has 0 certificates
#    When Certificate is imported for client "cs:test:member-1"
#    Then token "0" key "First key" has 1 certificates
#
#  Scenario: Member test
#    Given tokens list contains token "0"
#    * Member signing info for client "cs:test:member-1" is retrieved
#
#  Scenario: HSM status is not operational
#    * HSM is not operational
#
#  Scenario: Self signed certificate
#    Given token "0" key "First key" has 0 certificates
#    When self signed cert generated for token "0" key "First key", client "cs:test:member-1"
#    Then token "0" key "First key" has 1 certificates
#    And keyId can be retrieved by cert hash
#    And token and keyId can be retrieved by cert hash
#    And certificate can be signed using key "First key" from token "0"
#
#  Scenario: Member info
#    Then member "cs:test:member-1" has 1 certificate
#
#  Scenario: Cert status
#    Given self signed cert generated for token "0" key "KeyX", client "cs:test:member-2"
#    And certificate info can be retrieved by cert hash
#    Then certificate can be deactivated
#    And certificate can be activated
#    And certificate status can be changed to "deletion in progress"
#    And certificate can be deleted
#
#  Scenario: Miscellaneous checks
#    * check token "0" key "First key" batch signing enabled
#
#  Scenario: Exceptions
#    * Set token name fails with TokenNotFound exception when token does not exist
#    * Deleting not existing certificate from token fails
#    * Retrieving token info by not existing key fails
#    * Deleting not existing certRequest fails
#    * Signing with unknown key fails
#    * Getting key by not existing cert hash fails
#    * Not existing certificate can not be activated
#
#  Scenario: Ocsp responses
#    When ocsp responses are set
#    Then ocsp responses can be retrieved
#    And null ocsp response is returned for unknown certificate

