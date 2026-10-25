package nl.onnoh.mdg.template;

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
 * Covers the scenarios in src/test/resources/features/template-and-style-overrides.feature:
 * the --template-dir and --style-file overrides of the generate command.
 */
class TemplateAndStyleOverrideTest {

    private static final String BUNDLED_STYLE_LINK =
            "<link rel=\"stylesheet\" href=\"../templates/style/markdown.css\" />";
    private static final String BPMN_TEMPLATE = "bpmn/templates/html/bpmn-documentation.ftl";
    private static final String DMN_TEMPLATE = "dmn/templates/html/dmn-documentation.ftl";
    private static final String BPMN_MODEL = "bpmn/models/all_elements.bpmn";
    private static final String DMN_MODEL = "dmn/models/all_elements.dmn";

    @Test
    @DisplayName("@AC-1 Default run is unchanged when no override is given")
    void defaultRunRendersTheBundledTemplateAndCss(@TempDir Path tempDir) throws IOException {
        Path defaultRun = modelIn(tempDir.resolve("default"), BPMN_MODEL, "all_elements.bpmn");

        // A --template-dir holding a verbatim copy of the bundled template must render the same
        // bytes as the no-option run: that is what "matches the bundled template" means here.
        Path copyRun = modelIn(tempDir.resolve("copy"), BPMN_MODEL, "all_elements.bpmn");
        Path templateDir = tempDir.resolve("templates");
        copyBundledTemplate(BPMN_TEMPLATE, templateDir);

        assertEquals(0, run("bpmn", defaultRun.toString(), "-of", "html"));
        assertEquals(0, run("bpmn", copyRun.toString(), "-of", "html", "--template-dir", templateDir.toString()));

        String defaultHtml = readOutput(defaultRun, ".bpmn");
        assertTrue(defaultHtml.contains(BUNDLED_STYLE_LINK),
                "default output should link the bundled stylesheet verbatim, was:\n" + head(defaultHtml));
        assertEquals(withoutRunSpecificValues(readOutput(copyRun, ".bpmn")),
                withoutRunSpecificValues(defaultHtml));
    }

    @Test
    @DisplayName("@AC-2 A custom template directory overrides the bundled template")
    void customTemplateDirectoryOverridesTheBundledTemplate(@TempDir Path tempDir) throws IOException {
        Path model = modelIn(tempDir.resolve("model"), BPMN_MODEL, "all_elements.bpmn");
        Path templateDir = tempDir.resolve("templates");
        writeTemplate(templateDir, BPMN_TEMPLATE, "<h1>Custom BPMN doc for ${bpmn.fileName}</h1>\n");

        assertEquals(0, run("bpmn", model.toString(), "-of", "html", "--template-dir", templateDir.toString()));

        String html = readOutput(model, ".bpmn");
        assertEquals("<h1>Custom BPMN doc for all_elements.bpmn</h1>", html.strip());
        assertFalse(html.contains(BUNDLED_STYLE_LINK), "bundled template must not be rendered");
    }

    @Test
    @DisplayName("@AC-3 A partial override falls back to bundled templates for the rest")
    void partialOverrideFallsBackToTheBundledTemplates(@TempDir Path tempDir) throws IOException {
        Path model = modelIn(tempDir.resolve("model"), BPMN_MODEL, "all_elements.bpmn");
        Path templateDir = tempDir.resolve("templates");
        // bpmn-tasks.ftl is deliberately NOT in the override directory.
        writeTemplate(templateDir, BPMN_TEMPLATE, """
                <#import "bpmn-tasks.ftl" as tasks/>
                <#import "../bpmn-template-util.ftl" as util/>
                <#global skipEmptySections=bpmn.suppressEmptySections />
                <#global openSections="" />
                <#global markdown=false />
                <h1>Partial override</h1>
                <#list bpmn.processes as process>
                    <#if process.elements?has_content>
                        <@tasks.listTasks processElements=process.elements />
                    </#if>
                </#list>
                """);

        assertEquals(0, run("bpmn", model.toString(), "-of", "html", "--template-dir", templateDir.toString()));

        String html = readOutput(model, ".bpmn");
        assertTrue(html.contains("<h1>Partial override</h1>"), "override template should be used");
        // listTasks from the bundled bpmn-tasks.ftl renders the service tasks of all_elements.bpmn.
        assertTrue(html.contains("Service Tasks"),
                "bpmn-tasks.ftl should have been loaded from the bundled resources, was:\n" + head(html));
    }

    @Test
    @DisplayName("@AC-4 A custom stylesheet overrides the bundled CSS")
    void customStyleFileOverridesTheBundledCss(@TempDir Path tempDir) throws IOException {
        Path model = modelIn(tempDir.resolve("model"), BPMN_MODEL, "all_elements.bpmn");

        assertEquals(0, run("bpmn", model.toString(), "-of", "html", "--style-file", "custom.css"));

        String html = readOutput(model, ".bpmn");
        assertTrue(html.contains("<link rel=\"stylesheet\" href=\"custom.css\" />"),
                "stylesheet link should point at custom.css, was:\n" + head(html));
        assertFalse(html.contains("markdown.css"), "the bundled markdown.css must not be referenced");
    }

    @Test
    @DisplayName("@AC-5 Overrides apply to both bpmn and dmn subcommands")
    void overridesApplyToBothSubcommands(@TempDir Path tempDir) throws IOException {
        Path bpmnModel = modelIn(tempDir.resolve("bpmn-model"), BPMN_MODEL, "all_elements.bpmn");
        Path dmnModel = modelIn(tempDir.resolve("dmn-model"), DMN_MODEL, "all_elements.dmn");
        Path templateDir = tempDir.resolve("templates");
        writeTemplate(templateDir, BPMN_TEMPLATE,
                "<link rel=\"stylesheet\" href=\"${bpmn.styleFile}\" />\n<p>custom bpmn</p>\n");
        writeTemplate(templateDir, DMN_TEMPLATE,
                "<link rel=\"stylesheet\" href=\"${dmn.styleFile}\" />\n<p>custom dmn</p>\n");

        assertEquals(0, run("bpmn", bpmnModel.toString(), "-of", "html",
                "--template-dir", templateDir.toString(), "--style-file", "custom.css"));
        assertEquals(0, run("dmn", dmnModel.toString(), "-of", "html",
                "--template-dir", templateDir.toString(), "--style-file", "custom.css"));

        String bpmnHtml = readOutput(bpmnModel, ".bpmn");
        String dmnHtml = readOutput(dmnModel, ".dmn");
        assertEquals("<link rel=\"stylesheet\" href=\"custom.css\" />\n<p>custom bpmn</p>", bpmnHtml.strip());
        assertEquals("<link rel=\"stylesheet\" href=\"custom.css\" />\n<p>custom dmn</p>", dmnHtml.strip());
    }

    private static int run(String... args) {
        return new CommandLine(new GenerateDocumentation()).execute(args);
    }

    /** Copies a bundled model resource into its own directory so each run writes its own output. */
    private static Path modelIn(Path directory, String resource, String fileName) throws IOException {
        Files.createDirectories(directory);
        Path model = directory.resolve(fileName);
        Files.write(model, readResource(resource));
        return model;
    }

    private static void writeTemplate(Path templateDir, String relativePath, String content) throws IOException {
        Path template = templateDir.resolve(relativePath);
        Files.createDirectories(template.getParent());
        Files.writeString(template, content, StandardCharsets.UTF_8);
    }

    private static void copyBundledTemplate(String relativePath, Path templateDir) throws IOException {
        Path template = templateDir.resolve(relativePath);
        Files.createDirectories(template.getParent());
        Files.write(template, readResource(relativePath));
    }

    private static byte[] readResource(String resource) throws IOException {
        try (InputStream in = TemplateAndStyleOverrideTest.class.getResourceAsStream("/" + resource)) {
            if (in == null) {
                throw new IOException("Bundled resource not found: " + resource);
            }
            return in.readAllBytes();
        }
    }

    private static String readOutput(Path model, String modelSuffix) throws IOException {
        Path output = Path.of(model.toString().replace(modelSuffix, ".html"));
        assertTrue(Files.exists(output), "expected generated file " + output);
        return Files.readString(output, StandardCharsets.UTF_8);
    }

    /**
     * Blanks out the two things that legitimately differ between two runs of the same template:
     * the generation timestamp, and the identity hashes of the JAXB objects that some sections
     * still render with the default Object.toString().
     */
    private static String withoutRunSpecificValues(String html) {
        return html
                .replaceAll("Generated on [^<]*", "Generated on <timestamp>")
                .replaceAll("@[0-9a-f]{6,}", "@<identity>");
    }

    private static String head(String html) {
        return html.length() > 600 ? html.substring(0, 600) : html;
    }
}
