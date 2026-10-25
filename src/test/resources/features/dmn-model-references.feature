Feature: Model references are hyperlinks
  A DMN requirement that references another element by id should link to that
  element's section in the generated HTML, instead of printing the raw href as
  plain text.

  The scenarios below are driven by the JUnit integration tests in
  nl.onnoh.mdg.dmn.DmnModelReferenceTest; every test display name carries the
  matching @AC-n tag.

  @AC-1
  Scenario: An information requirement links to its required decision
    Given the all_elements.dmn fixture, where informationRequirement "InformationRequirement_0o77mqd" has requiredDecision href "#Decision_1x42iw6"
    When the HTML documentation is generated
    Then the information requirements section contains an <a href="#Decision_1x42iw6"> hyperlink, not the plain href text
    And the document contains an id="Decision_1x42iw6" anchor that the link resolves to

  @AC-2
  Scenario: A knowledge requirement links to its required business knowledge model
    Given the all_elements.dmn fixture, where knowledgeRequirement "KnowledgeRequirement_1j8h01a" has requiredKnowledge href "#BusinessKnowledgeModel_0u1cd8y"
    When the HTML documentation is generated
    Then the knowledge requirements section contains an <a href="#BusinessKnowledgeModel_0u1cd8y"> hyperlink, not the plain href text
    And the document contains an id="BusinessKnowledgeModel_0u1cd8y" anchor that the link resolves to

  @AC-3
  Scenario: An authority requirement links to its required knowledge source
    Given the all_elements.dmn fixture, where authorityRequirement "AuthorityRequirement_0qxlcwe" has requiredAuthority href "#KnowledgeSource_158l8e0"
    When the HTML documentation is generated
    Then the authority requirements section contains an <a href="#KnowledgeSource_158l8e0"> hyperlink, not the plain href text
    And the document contains an id="KnowledgeSource_158l8e0" anchor that the link resolves to
