package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.samples.docs.CountEnum;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

public class TestDocsModule {
    private List<File> getClasspath() {
        String classpath = System.getProperty("java.class.path");
        return Stream.of(classpath.split(File.pathSeparator))
                .map(File::new)
                .filter(entry -> entry.exists() && !new File(entry, "test.properties").exists())
                .collect(Collectors.toList());
    }

    private File getSampleDir() throws IOException {
        Properties testProperties = new Properties();
        testProperties.load(DocsModule.class.getResourceAsStream("/test.properties"));
        String samplePath = testProperties.getProperty("api.sample.dir");
        assertNotNull(samplePath);
        File sampleDir = new File(samplePath);
        assertTrue(sampleDir.exists());
        return sampleDir;
    }

    private final File docsDir = new File("target/docs");
    private final Enunciate engine = new Enunciate();

    @Before
    public void init() throws IOException {
        if (docsDir.exists()) {
            for (Path path : Files.walk(docsDir.toPath()).sorted(Comparator.reverseOrder()).toArray(Path[]::new)) {
                Files.delete(path);
            }
        }
        engine
              .addSourceDir(getSampleDir())
              .loadConfiguration(DocsModule.class.getResourceAsStream("test-docs-module-config.xml"))
              .setClasspath(getClasspath());
    }

    @Test
    public void defaultConfig() throws IOException {
        engine.loadDiscoveredModules();
        assertTrue("DocsModule not discovered", engine.getModules().stream().anyMatch(DocsModule.class::isInstance));
        engine.run();

        File enumFile = new File(docsDir, "json_CountEnum.html");
        assertThat(enumFile, exists());
        assertEquals(CountEnum.values().length,
                loadFile(enumFile).stream().filter(s -> s.contains("<li><a href=\"#")).count());

        assertThat(new File(docsDir, "json_OtherEnum.html"), not(exists()));

        assertThat(new File(docsDir, "json_TypeWithHintOnProperty.html"), exists());
        assertThat(new File(docsDir, "json_PropertyTypeActual.html"), not(exists()));
        assertThat(new File(docsDir, "json_PropertyTypeHint.html"), exists());

        assertThat(extractDescriptions(loadFile(new File(docsDir, "json_JavaDocLinks.html"))), contains(
                "Status of <a href=\"json_JavaDocLinks.html#prop-a_b_c_d_e\">a_b_c_d_e</a>",
                "<a href=\"json_JavaDocLinks.html#prop-status\">foo bar</a>"));

        assertThat(extractDescriptions(loadFile(new File(docsDir, "json_JavaDocLinkClass.html"))), contains(
                "Foo <a href=\"json_JavaDocLinks.html\">JavaDocLinks</a>",
                "Bar <a href=\"json_JavaDocLinks.html#prop-a_b_c_d_e\">a_b_c_d_e</a>"));

        assertThat(extractDescriptions(loadFile(new File(docsDir, "json_JavaDocLinkEnum.html"))),
                contains("Description of <a href=\"json_CountEnum.html#one\">one</a>"));

        assertThat(new File(docsDir, "json_TypeInfoA.html"), exists());
        assertThat(extractPropertyNames(loadFile(new File(docsDir, "json_TypeInfoA.html"))),
                contains("prop_a", "type"));

        assertThat(new File(docsDir, "json_TypeInfoB.html"), exists());
        assertThat(extractPropertyNames(loadFile(new File(docsDir, "json_TypeInfoB.html"))),
                contains("prop_b", "type"));

        File downloadFile = new File(docsDir, "downloads.html");
        assertThat(downloadFile, exists());
        assertEquals(asList("Why is there a random file?", "This is a description of the test properties."),
                extract(loadFile(downloadFile), Pattern.compile("\"downloadfile-description\">(.*?)<")));
    }

    @Test
    public void collapseTypeHierarchy() throws IOException {
        engine.getConfiguration().getSource().setProperty("modules.jackson[@collapse-type-hierarchy]", true);
        engine.loadDiscoveredModules();
        engine.run();

        assertThat(new File(docsDir, "json_TypeInfoA.html"), exists());
        assertEquals(asList("type", "prop_a"), extractPropertyNames(loadFile(new File(docsDir, "json_TypeInfoA.html"))));

        assertThat(new File(docsDir, "json_TypeInfoB.html"), exists());
        assertEquals(asList("type", "prop_b"), extractPropertyNames(loadFile(new File(docsDir, "json_TypeInfoB.html"))));
    }

    private static List<String> loadFile(File file) throws IOException {
        return readAllLines(file.toPath());
    }

    private static List<String> extract(List<String> content, Pattern pattern) {
        List<String> result = new ArrayList<>();
        for (String line : content) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                result.add(matcher.group(1));
            }
        }
        return result;
    }

    private static List<String> extractDescriptions(List<String> content) {
        return extract(content, Pattern.compile("<span class=\"property-description\">(.+?)</span>"));
    }

    private static List<String> extractPropertyNames(List<String> content) {
        return extract(content, Pattern.compile("class=\"property-name\">(.+?)</span>"));
    }

    private static FileExists exists() {
        return new FileExists();
    }
}
