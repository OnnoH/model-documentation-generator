Feature: Externalised templates and styles
  A caller of the CLI can supply their own FreeMarker templates and stylesheet
  instead of being stuck with the ones packaged in the jar.

  The scenarios below are driven by the JUnit integration tests in
  nl.onnoh.mdg.template.TemplateAndStyleOverrideTest; every test display name
  carries the matching @AC-n tag.

  @AC-1
  Scenario: Default run is unchanged when no override is given
    Given no --template-dir or --style-file option is passed
    When "generate bpmn model.bpmn -of html" runs
    Then the output matches the current bundled template and CSS byte-for-byte

  @AC-2
  Scenario: A custom template directory overrides the bundled template
    Given a directory containing "bpmn/templates/html/bpmn-documentation.ftl" with custom markup
    When "generate bpmn model.bpmn -of html --template-dir <dir>" runs
    Then the generated HTML contains the custom markup instead of the bundled one

  @AC-3
  Scenario: A partial override falls back to bundled templates for the rest
    Given a --template-dir that only contains "bpmn/templates/html/bpmn-documentation.ftl"
    When generation runs and that template <#import>s "bpmn-tasks.ftl"
    Then "bpmn-tasks.ftl" is loaded from the bundled classpath resources without error

  @AC-4
  Scenario: A custom stylesheet overrides the bundled CSS
    Given "--style-file custom.css" is passed
    When "generate bpmn model.bpmn -of html" runs
    Then the generated HTML stylesheet link points at "custom.css" and the bundled markdown.css is not referenced

  @AC-5
  Scenario: Overrides apply to both bpmn and dmn subcommands
    Given the same --template-dir and --style-file options
    When both "generate bpmn ..." and "generate dmn ..." run
    Then both honour the override the same way
