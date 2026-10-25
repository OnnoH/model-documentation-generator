package nl.onnoh.mdg;

import lombok.extern.slf4j.Slf4j;
import nl.onnoh.mdg.bpmn.BpmnCommand;
import nl.onnoh.mdg.dmn.DmnCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static nl.onnoh.mdg.template.TemplateService.DEFAULT_STYLE_FILE;

@Slf4j
@Command(name = "generate", description = "Generate documentation for given BPMN or DMN file."
        , mixinStandardHelpOptions = true
        , version = "1.0"
        , subcommands = {
          BpmnCommand.class
        , DmnCommand.class
        , HelpCommand.class
    })
public class GenerateDocumentation implements Callable<Integer> {

//    @CommandLine.Parameters(index = "0", description = "The BPMN/DMN file to document.")
    public String modelFile;
//    @Option(names = { "-h", "--help", "-?", "-help"}, usageHelp = true, description = "Display this help and exit")
//    boolean help;

    @Option(names = {"-of", "--output-format"}, defaultValue = "html", scope = ScopeType.INHERIT, description = "The document format (default: ${DEFAULT-VALUE}).")
    public String outputFormat;

    @Option(names = {"-if", "--image-format"}, defaultValue = "svg", scope = ScopeType.INHERIT, description = "The image format included in the documentation (default: ${DEFAULT-VALUE}).")
    public String imageFormat;


    @Option(names = {"-s", "--suppress-empty-sections"}, defaultValue = "false", scope = ScopeType.INHERIT, description = "Suppress empty sections (default: ${DEFAULT-VALUE}).")
    public boolean suppressEmptySections;

    @Option(names = {"-o", "--open-sections"}, defaultValue = "false", scope = ScopeType.INHERIT, description = "Open all sections (default: ${DEFAULT-VALUE}).")
    public boolean openSections;

    @Option(names = {"-td", "--template-dir"}, scope = ScopeType.INHERIT, description = "Directory with your own FreeMarker templates, laid out like the bundled ones "
            + "(e.g. <dir>/bpmn/templates/html/bpmn-documentation.ftl). Templates missing from the directory are taken from the bundled ones.")
    public String templateDir;

    @Option(names = {"-sf", "--style-file"}, scope = ScopeType.INHERIT, description = "Stylesheet the generated HTML links to (default: " + DEFAULT_STYLE_FILE + ").")
    public String styleFile;

    @Override
    public Integer call() {
        if (!checkFile(modelFile)) {
            return 1;
        }
        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new GenerateDocumentation()).execute(args);
        System.exit(exitCode);
    }

    public static boolean checkFile(String modelFile) {
        if (modelFile == null || modelFile.isEmpty()) {
            log.error("No file specified.");
            return false;
        }
        Path filePath = Paths.get(modelFile);
        if (!Files.exists(filePath)) {
            log.error("File not found: {}", modelFile);
            return false;
        }
        if (!Files.isRegularFile(filePath)) {
            log.error("Not a regular file: {}", modelFile);
            return false;
        }
        if (!Files.isReadable(filePath)) {
            log.error("Error reading file: {}", modelFile);
            return false;
        }
        return true;
    }

}
