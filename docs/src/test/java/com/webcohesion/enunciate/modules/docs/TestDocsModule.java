package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.module.EnunciateModule;
import com.webcohesion.enunciate.samples.docs.CountEnum;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestDocsModule extends TestCase {
    public void testFoo() throws Exception {
        Properties testProperties = new Properties();
        testProperties.load(DocsModule.class.getResourceAsStream("/test.properties"));
        String samplePath = testProperties.getProperty("api.sample.dir");
        assertNotNull(samplePath);
        File sampleDir = new File(samplePath);
        assertTrue(sampleDir.exists());

        Enunciate engine = new Enunciate()
            .addSourceDir(sampleDir)
            .loadConfiguration(DocsModule.class.getResourceAsStream("test-docs-module-config.xml"))
            .loadDiscoveredModules();

        String cp = System.getProperty("java.class.path");
        String[] path = cp.split(File.pathSeparator);
        List<File> classpath = new ArrayList<File>(path.length);
        for (String element : path) {
            File entry = new File(element);
            if (entry.exists() && !new File(entry, "test.properties").exists()) {
                classpath.add(entry);
            }
        }

        engine.setClasspath(classpath);

        DocsModule docsModule = null;
        for (EnunciateModule candidate : engine.getModules()) {
            if (candidate instanceof DocsModule) {
                docsModule = (DocsModule) candidate;
                break;
            }
        }
        assertNotNull(docsModule);

        engine.run();

        File enumFile = new File("target/docs/json_CountEnum.html");
        assertTrue(enumFile.exists());

        FileReader fr = new FileReader(enumFile);
        BufferedReader br = new BufferedReader(fr);
        int count = 0;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("<li><a href=\"#")) {
                    count++;
                }
            }
        } finally {
            br.close();
        }
        assertEquals(CountEnum.values().length, count);

        File downloadFile = new File("target/docs/downloads.html");
        assertTrue(downloadFile.exists());

        fr = new FileReader(downloadFile);
        br = new BufferedReader(fr);
        count = 0;
        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("\"downloadfile-description\"><")) {
                    count++;
                }
            }
        } finally {
            br.close();
        }
        assertEquals(0, count);
    }
}
