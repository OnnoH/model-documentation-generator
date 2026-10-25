Feature: BPMN model references are hyperlinks
  A BPMN boundary event references the activity it is attached to. That reference
  should link to the activity's section in the generated HTML and Markdown,
  instead of being dropped from the documentation.

  The scenarios below are driven by the JUnit integration tests in
  nl.onnoh.mdg.bpmn.BpmnModelReferenceTest; every test display name carries the
  matching @AC-n tag.

  @AC-7
  Scenario: A boundary event links to the activity it is attached to, in HTML
    Given the all_elements.bpmn fixture, where boundaryEvent "Event_0qob5un" has attachedToRef "Activity_0nz7ni3"
    When the HTML documentation is generated
    Then the boundary events section contains an <a href="#Activity_0nz7ni3"> hyperlink to the attached activity
    And the document contains an id="Activity_0nz7ni3" anchor that the link resolves to

  @AC-8
  Scenario: A boundary event links to the activity it is attached to, in Markdown
    Given the all_elements.bpmn fixture, where boundaryEvent "Event_0qob5un" has attachedToRef "Activity_0nz7ni3"
    When the Markdown documentation is generated
    Then the boundary events section contains a "[Activity_0nz7ni3](#Activity_0nz7ni3)" Markdown link to the attached activity
    And the document contains an <a id="Activity_0nz7ni3"></a> anchor that the link resolves to
