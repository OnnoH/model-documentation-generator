package nl.onnoh.mdg.template;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class TemplateService {
    public static final String TEMPLATES_FOLDER = "templates";
    public static final String TEMPLATE_SUFFIX = ".ftl";
    /** Stylesheet the bundled HTML templates link to when no --style-file is given. */
    public static final String DEFAULT_STYLE_FILE = "../templates/style/markdown.css";

    public static Configuration initTemplateEngine() {
        return initTemplateEngine(null);
    }

    /**
     * Builds the FreeMarker configuration. Templates are always resolvable from the bundled
     * classpath resources; when {@code templateDir} points at a directory, that directory is
     * consulted first, so a caller can override single templates and let the rest fall back to
     * the bundled ones.
     *
     * @param templateDir directory with the caller's own templates, or {@code null} for bundled only
     */
    public static Configuration initTemplateEngine(String templateDir) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateLoader(buildTemplateLoader(templateDir));
//        cfg.setObjectWrapper(new FlowElementWrapper(cfg.getIncompatibleImprovements()));

        return cfg;
    }

    private static TemplateLoader buildTemplateLoader(String templateDir) {
        TemplateLoader bundledLoader = new ClassTemplateLoader(TemplateService.class, "/");
        if (templateDir == null || templateDir.isBlank()) {
            return bundledLoader;
        }

        File directory = new File(templateDir);
        if (!directory.isDirectory()) {
            System.err.println("Template directory not found: " + templateDir + ". Using the bundled templates.");
            return bundledLoader;
        }

        try {
            return new MultiTemplateLoader(new TemplateLoader[]{new FileTemplateLoader(directory), bundledLoader});
        } catch (IOException ex) {
            System.err.println("Unable to read template directory " + templateDir + ". " + ex.getMessage()
                    + " Using the bundled templates.");
            return bundledLoader;
        }
    }

    private static Template loadTemplate(Configuration cfg, File file) {
        Template template = null;
        try {
            template = cfg.getTemplate(file.getPath());
        } catch (IOException ex) {
            System.err.println("Unable to load template " + file.getPath() + ". " + ex.getMessage());
        }
        return template;
    }

    public static void processTemplate(Configuration configuration, String templateFile, String outputFile, Map<String, Object> model) {

        File file = new File(templateFile);

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException ex) {
            System.err.println("Unable to create output file " + outputFile + ". " + ex.getMessage());
        }

        Template template = loadTemplate(configuration, file);
        try {
            template.process(model, writer);
        } catch (TemplateException | IOException ex) {
            System.err.println("Error processing template " + file.getPath() + ". " + ex.getMessage());
        }
    }

}
