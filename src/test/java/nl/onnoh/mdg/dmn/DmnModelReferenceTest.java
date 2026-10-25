package nl.onnoh.mdg.dmn;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the scenarios in src/test/resources/features/dmn-model-references.feature:
 * a DMN requirement that references another element by id renders a hyperlink to
 * that element's section instead of the raw href as plain text, in HTML (AC-1 to
 * AC-3) and in Markdown (AC-4 to AC-6). Every test display name carries the
 * matching @AC-n tag.
 */
class DmnModelReferenceTest {

    private static final String DMN_MODEL = "dmn/models/all_elements.dmn";
    /** The decision the information, knowledge and authority requirements under test belong to. */
    private static final String DECISION_WITH_REQUIREMENTS = "Decision_1xh7jx6";

    @Test
    @DisplayName("@AC-1 An information requirement links to its required decision")
    void informationRequirementLinksToItsRequiredDecision(@TempDir Path tempDir) throws IOException {
        String html = generateHtml(tempDir);

        assertLinks(html, "Information Requirements", "InformationRequirement_0o77mqd", "#Decision_1x42iw6");
        assertAnchorExists(html, "Decision_1x42iw6");
    }

    @Test
    @DisplayName("@AC-2 A knowledge requirement links to its required business knowledge model")
    void knowledgeRequirementLinksToItsRequiredBusinessKnowledgeModel(@TempDir Path tempDir) throws IOException {
        String html = generateHtml(tempDir);

        assertLinks(html, "Knowledge Requirements", "KnowledgeRequirement_1j8h01a", "#BusinessKnowledgeModel_0u1cd8y");
        assertAnchorExists(html, "BusinessKnowledgeModel_0u1cd8y");
    }

    @Test
    @DisplayName("@AC-3 An authority requirement links to its required knowledge source")
    void authorityRequirementLinksToItsRequiredKnowledgeSource(@TempDir Path tempDir) throws IOException {
        String html = generateHtml(tempDir);

        assertLinks(html, "Authority Requirements", "AuthorityRequirement_0qxlcwe", "#KnowledgeSource_158l8e0");
        assertAnchorExists(html, "KnowledgeSource_158l8e0");
    }

    @Test
    @DisplayName("@AC-4 An information requirement links to its required decision in Markdown")
    void informationRequirementLinksToItsRequiredDecisionInMarkdown(@TempDir Path tempDir) throws IOException {
        String markdown = generateMarkdown(tempDir);

        assertMarkdownLinks(markdown, "Information Requirements", "Decision", "#Decision_1x42iw6");
        assertAnchorExists(markdown, "Decision_1x42iw6");
    }

    @Test
    @DisplayName("@AC-5 A knowledge requirement links to its required business knowledge model in Markdown")
    void knowledgeRequirementLinksToItsRequiredBusinessKnowledgeModelInMarkdown(@TempDir Path tempDir) throws IOException {
        String markdown = generateMarkdown(tempDir);

        assertMarkdownLinks(markdown, "Knowledge Requirements", "Business Knowledge Model", "#BusinessKnowledgeModel_0u1cd8y");
        assertAnchorExists(markdown, "BusinessKnowledgeModel_0u1cd8y");
    }

    @Test
    @DisplayName("@AC-6 An authority requirement links to its required knowledge source in Markdown")
    void authorityRequirementLinksToItsRequiredKnowledgeSourceInMarkdown(@TempDir Path tempDir) throws IOException {
        String markdown = generateMarkdown(tempDir);

        assertMarkdownLinks(markdown, "Authority Requirements", "Knowledge Source", "#KnowledgeSource_158l8e0");
        assertAnchorExists(markdown, "KnowledgeSource_158l8e0");
    }

    /**
     * Asserts that the paragraph of {@code requirementId}, inside the section headed
     * {@code heading}, links to {@code href} and no longer prints it as plain text.
     */
    private static void assertLinks(String html, String heading, String requirementId, String href) {
        String paragraph = requirementParagraph(sectionOf(html, heading), requirementId);
        assertTrue(paragraph.contains("<a href=\"" + href + "\">"),
                "the " + heading + " paragraph of " + requirementId + " should link to " + href
                        + ", was:\n" + paragraph);
        assertFalse(paragraph.replaceAll("<a href=\"[^\"]*\">", "").contains(href),
                "the href should only appear inside the link, not as plain text, was:\n" + paragraph);
    }

    /**
     * Asserts that the {@code heading} section of the decision that owns the requirements
     * contains the Markdown link {@code [label](href)}.
     */
    private static void assertMarkdownLinks(String markdown, String heading, String label, String href) {
        String section = markdownSectionOf(markdown, heading);
        String link = "[" + label + "](" + href + ")";
        assertTrue(section.contains(link),
                "the " + heading + " section should contain the Markdown link " + link + ", was:\n" + section);
    }

    /** Asserts the link target resolves to an anchor that exists in the output document. */
    private static void assertAnchorExists(String html, String id) {
        assertTrue(html.contains("id=\"" + id + "\""),
                "the document should contain an id=\"" + id + "\" anchor for the link to resolve to");
    }

    /** The markup between a requirements heading and whatever ends that section. */
    private static String sectionOf(String html, String heading) {
        String marker = "<h3>" + heading + "</h3>";
        int start = html.indexOf(marker);
        assertTrue(start >= 0, "expected a \"" + heading + "\" section in the generated HTML");
        start += marker.length();
        int end = Math.min(endOf(html, "<h3", start), endOf(html, "</details>", start));
        return html.substring(start, end);
    }

    /**
     * The Markdown between a requirements heading of the DecideBla decision - the one
     * holding the requirements of AC-4 to AC-6 - and the next heading.
     */
    private static String markdownSectionOf(String markdown, String heading) {
        int decision = markdown.indexOf("<a id=\"" + DECISION_WITH_REQUIREMENTS + "\"></a>");
        assertTrue(decision >= 0, "expected a section for decision " + DECISION_WITH_REQUIREMENTS);
        String marker = "### " + heading;
        int start = markdown.indexOf(marker, decision);
        assertTrue(start >= 0, "expected a \"" + heading + "\" section in the generated Markdown");
        start += marker.length();
        return markdown.substring(start, endOf(markdown, "\n#", start));
    }

    /** The paragraph a single requirement renders itself into, keyed on its own id. */
    private static String requirementParagraph(String section, String requirementId) {
        int start = section.indexOf(requirementId);
        assertTrue(start >= 0, "expected requirement " + requirementId + " in:\n" + section);
        int end = endOf(section, "</p>", start);
        return section.substring(start, end);
    }

    private static int endOf(String html, String marker, int from) {
        int index = html.indexOf(marker, from);
        return index < 0 ? html.length() : index;
    }

    private static String generateHtml(Path tempDir) throws IOException {
        return generate(tempDir, "html", ".html");
    }

    private static String generateMarkdown(Path tempDir) throws IOException {
        return generate(tempDir, "markdown", ".md");
    }

    /** Generates the documentation for the bundled all_elements.dmn fixture. */
    private static String generate(Path tempDir, String outputFormat, String extension) throws IOException {
        Files.createDirectories(tempDir);
        Path model = tempDir.resolve("all_elements.dmn");
        try (InputStream in = DmnModelReferenceTest.class.getResourceAsStream("/" + DMN_MODEL)) {
            if (in == null) {
                throw new IOException("Bundled resource not found: " + DMN_MODEL);
            }
            Files.write(model, in.readAllBytes());
        }

        assertEquals(0, new CommandLine(new GenerateDocumentation())
                .execute("dmn", model.toString(), "-of", outputFormat));

        Path output = tempDir.resolve("all_elements" + extension);
        assertTrue(Files.exists(output), "expected generated file " + output);
        return Files.readString(output, StandardCharsets.UTF_8);
    }
}
