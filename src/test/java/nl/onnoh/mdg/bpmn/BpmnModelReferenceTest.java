package nl.onnoh.mdg.bpmn;

import nl.onnoh.mdg.GenerateDocumentation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the scenarios in src/test/resources/features/bpmn-model-references.feature:
 * a boundary event links to the activity it is attached to, in both output formats,
 * instead of dropping that reference. Every test display name carries the matching
 * @AC-n tag.
 */
class BpmnModelReferenceTest {

    private static final String BPMN_MODEL = "bpmn/models/all_elements.bpmn";
    private static final String BOUNDARY_EVENT = "Event_0qob5un";
    private static final String BOUNDARY_EVENT_NAME = "Non Interrupting Message Boundary";
    private static final String ATTACHED_ACTIVITY = "Activity_0nz7ni3";

    @Test
    @DisplayName("@AC-7 A boundary event links to the activity it is attached to, in HTML")
    void boundaryEventLinksToTheActivityItIsAttachedToInHtml(@TempDir Path tempDir) throws IOException {
        String html = generate(tempDir, "html", ".html");

        String event = htmlEventBlock(html, BOUNDARY_EVENT);
        assertTrue(event.contains("<a href=\"#" + ATTACHED_ACTIVITY + "\">"),
                "boundary event " + BOUNDARY_EVENT + " should link to #" + ATTACHED_ACTIVITY + ", was:\n" + event);
        assertTrue(html.contains("id=\"" + ATTACHED_ACTIVITY + "\""),
                "the document should contain an id=\"" + ATTACHED_ACTIVITY + "\" anchor for the link to resolve to");
    }

    @Test
    @DisplayName("@AC-8 A boundary event links to the activity it is attached to, in Markdown")
    void boundaryEventLinksToTheActivityItIsAttachedToInMarkdown(@TempDir Path tempDir) throws IOException {
        String markdown = generate(tempDir, "markdown", ".md");

        String bullet = markdownEventBullet(markdown, BOUNDARY_EVENT_NAME);
        assertTrue(bullet.contains("](#" + ATTACHED_ACTIVITY + ")"),
                "the bullet of \"" + BOUNDARY_EVENT_NAME + "\" should link to #" + ATTACHED_ACTIVITY
                        + ", was:\n" + bullet);
        assertTrue(markdown.contains("<a id=\"" + ATTACHED_ACTIVITY + "\"></a>"),
                "the document should contain an <a id=\"" + ATTACHED_ACTIVITY + "\"></a> anchor"
                        + " for the link to resolve to");
    }

    /** The table a single event renders itself into, inside the Boundary Events section. */
    private static String htmlEventBlock(String html, String eventId) {
        String section = htmlSectionOf(html, "Boundary Events");
        int start = section.indexOf(eventId);
        assertTrue(start >= 0, "expected event " + eventId + " in the Boundary Events section:\n" + section);
        return section.substring(start, endOf(section, "</table>", start));
    }

    /** The markup between an events heading and whatever ends that section. */
    private static String htmlSectionOf(String html, String heading) {
        String marker = "<h4>" + heading + "</h4>";
        int start = html.indexOf(marker);
        assertTrue(start >= 0, "expected a \"" + heading + "\" section in the generated HTML");
        start += marker.length();
        return html.substring(start, endOf(html, "</details>", start));
    }

    /** The single line the named event renders itself into, inside the Boundary Events section. */
    private static String markdownEventBullet(String markdown, String eventName) {
        String marker = "##### Boundary Events";
        int section = markdown.indexOf(marker);
        assertTrue(section >= 0, "expected a \"Boundary Events\" section in the generated Markdown");
        int start = markdown.indexOf("* " + eventName, section);
        assertTrue(start >= 0, "expected event \"" + eventName + "\" in the Boundary Events section");
        return markdown.substring(start, endOf(markdown, "\n", start));
    }

    private static int endOf(String document, String marker, int from) {
        int index = document.indexOf(marker, from);
        return index < 0 ? document.length() : index;
    }

    /** Generates the documentation for the bundled all_elements.bpmn fixture. */
    private static String generate(Path tempDir, String outputFormat, String extension) throws IOException {
        Files.createDirectories(tempDir);
        Path model = tempDir.resolve("all_elements.bpmn");
        try (InputStream in = BpmnModelReferenceTest.class.getResourceAsStream("/" + BPMN_MODEL)) {
            if (in == null) {
                throw new IOException("Bundled resource not found: " + BPMN_MODEL);
            }
            Files.write(model, in.readAllBytes());
        }

        assertEquals(0, new CommandLine(new GenerateDocumentation())
                .execute("bpmn", model.toString(), "-of", outputFormat));

        Path output = tempDir.resolve("all_elements" + extension);
        assertTrue(Files.exists(output), "expected generated file " + output);
        return Files.readString(output, StandardCharsets.UTF_8);
    }
}
