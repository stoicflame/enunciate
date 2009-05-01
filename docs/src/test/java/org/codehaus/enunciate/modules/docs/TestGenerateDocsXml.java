/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.docs;

import junit.framework.TestCase;
import static org.codehaus.enunciate.EnunciateTestUtil.getAllJavaFiles;
import static org.codehaus.enunciate.InAPTTestCase.getInAPTClasspath;
import static org.codehaus.enunciate.InAPTTestCase.getSamplesDir;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.NamedFileArtifact;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestGenerateDocsXml extends TestCase {

  /**
   * Tests the generation of the documentation.
   */
  public void testGenerateDocsXML() throws Exception {
    DocumentationDeploymentModule module = new DocumentationDeploymentModule();
    module.setCopyright("myco");
    module.setTitle("mytitle");
    module.setSplashPackage("org.codehaus.enunciate.samples.docs.pckg1");
    EnunciateConfiguration config = new EnunciateConfiguration(Arrays.asList((DeploymentModule) module));
    Enunciate enunciate = new Enunciate(getAllJavaFiles(getSamplesDir()));
    enunciate.setConfig(config);
    enunciate.setTarget(Enunciate.Target.BUILD);
    enunciate.setClasspath(getInAPTClasspath());
    module.setBase(enunciate.createTempDir());
    ClientLibraryArtifact artifact1 = new ClientLibraryArtifact("module1", "1", "lib1") {

      @Override
      public void exportTo(File file, Enunciate enunciate) throws IOException {
        //no-op
      }
    };
    artifact1.setDescription("my <b>marked up</b> description for artifact 1");

    NamedFileArtifact file = new NamedFileArtifact(null, null, new File("1.1.xml"));
    file.setDescription("my description 1.1");
    artifact1.addArtifact(file);
    file = new NamedFileArtifact(null, null, new File("1.2.xml"));
    file.setDescription("my description 1.2");
    artifact1.addArtifact(file);

    ClientLibraryArtifact artifact2 = new ClientLibraryArtifact("module2", "2", "lib2") {
      @Override
      public void exportTo(File file, Enunciate enunciate) throws IOException {
        //no-op
      }
    };
    artifact2.setDescription("my <b>marked up</b> description for artifact 2");
    file = new NamedFileArtifact(null, null, new File("2.1.xml"));
    file.setDescription("my description 2.1");
    artifact2.addArtifact(file);
    file = new NamedFileArtifact(null, null, new File("2.2.xml"));
    file.setDescription("my description 2.2");
    artifact2.addArtifact(file);

    enunciate.addArtifact(artifact1);
    enunciate.addArtifact(artifact2);
    enunciate.execute();

    File docsXml = new File(enunciate.getGenerateDir(), "docs/docs.xml");
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setValidating(false);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    builder.setEntityResolver(new EntityResolver() {
      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (String.valueOf(systemId).endsWith("xhtml1-transitional.dtd")) {
          return new InputSource(TestGenerateDocsXml.this.getClass().getResourceAsStream("xhtml1-transitional.dtd"));
        }
        else if (String.valueOf(systemId).endsWith("xhtml-special.ent")) {
          return new InputSource(TestGenerateDocsXml.this.getClass().getResourceAsStream("xhtml-special.ent"));
        }
        else if (String.valueOf(systemId).endsWith("xhtml-lat1.ent")) {
          return new InputSource(TestGenerateDocsXml.this.getClass().getResourceAsStream("xhtml-lat1.ent"));
        }
        else if (String.valueOf(systemId).endsWith("xhtml-symbol.ent")) {
          return new InputSource(TestGenerateDocsXml.this.getClass().getResourceAsStream("xhtml-symbol.ent"));
        }
        return null;
      }
    });
    Document document = builder.parse(docsXml);
    XPath xpath = XPathFactory.newInstance().newXPath();

    assertEquals("Here is some package documentation. <child>text</child>", xpath.evaluate("/api-docs/documentation", document).trim());
    assertEquals("myco", xpath.evaluate("/api-docs/@copyright", document).trim());

    String packageDocsXPath = "/api-docs/packages/package[@id='%s']/documentation";
    assertEquals("Here is some package documentation. <child>text</child>", xpath.evaluate(String.format(packageDocsXPath, "org.codehaus.enunciate.samples.docs.pckg1"), document).trim());
    assertEquals("Here is some more package documentation.", xpath.evaluate(String.format(packageDocsXPath, "org.codehaus.enunciate.samples.docs.pckg2"), document).trim());

    String packageTagsXPath = "/api-docs/packages/package[@id='%s']/tag[@name='%s']";
    assertEquals("sometag value", xpath.evaluate(String.format(packageTagsXPath, "org.codehaus.enunciate.samples.docs.pckg2", "sometag"), document).trim());

    String typeDocsXPath = "/api-docs/data/schema[@namespace='%s']/types/type[@id='%s']/documentation";
    assertEquals("Text for EnumOne", xpath.evaluate(String.format(typeDocsXPath, "urn:pckg1", "org.codehaus.enunciate.samples.docs.pckg1.EnumOne"), document).trim());
    
    //todo: more testing of the docs xml...

    File libsXml = new File(enunciate.getGenerateDir(), "docs/downloads.xml");
    document = builder.parse(libsXml);
    
    String libDescriptionXPath = "/downloads/download[@name='%s']/description";
    assertEquals("my <b>marked up</b> description for artifact 1", xpath.evaluate(String.format(libDescriptionXPath, "lib1"), document).trim());
    assertEquals("my <b>marked up</b> description for artifact 2", xpath.evaluate(String.format(libDescriptionXPath, "lib2"), document).trim());

    String fileDescriptionXPath = "/downloads/download[@name='%s']/files/file[@name='%s']";
    assertEquals("my description 1.1", xpath.evaluate(String.format(fileDescriptionXPath, "lib1", "1.1.xml"), document).trim());
    assertEquals("my description 1.2", xpath.evaluate(String.format(fileDescriptionXPath, "lib1", "1.2.xml"), document).trim());
    assertEquals("my description 2.1", xpath.evaluate(String.format(fileDescriptionXPath, "lib2", "2.1.xml"), document).trim());
    assertEquals("my description 2.2", xpath.evaluate(String.format(fileDescriptionXPath, "lib2", "2.2.xml"), document).trim());

    //todo: more testing of the downloads xml...

    File indexHtml = new File(enunciate.getBuildDir(), "docs/index.html");
    document = builder.parse(indexHtml);
    
    assertEquals("mytitle", xpath.evaluate("/html/head/title", document).trim());
    assertEquals("EIOneService", xpath.evaluate("//font[@style='text-decoration:line-through;']/a", document).trim());
    assertEquals("text", xpath.evaluate("//child", document).trim());

    //todo: more testing of the generated HTML...
  }

  /**
   * testing the absolute file stuff.
   */
  public void testAbsoluteFileStuff() throws Exception {
    File file = new File("License.txt");
    assertFalse(file.isAbsolute());
  }

}
