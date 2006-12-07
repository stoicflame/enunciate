package net.sf.enunciate.modules.xml;

import junit.framework.TestCase;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.xml.config.SchemaConfig;
import net.sf.enunciate.modules.xml.config.WsdlConfig;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class TestXMLDeploymentModule extends TestCase {

  /**
   * Tests the "doGenerate" logic.
   */
  public void testDoGenerate() throws Exception {
    SchemaInfo defaultSchemaInfo = new SchemaInfo();
    defaultSchemaInfo.setNamespace("urn:default");
    SchemaInfo customSchemaInfo = new SchemaInfo();
    customSchemaInfo.setNamespace("urn:custom");
    final Map<String, SchemaInfo> ns2schema = new HashMap<String, SchemaInfo>();
    ns2schema.put(defaultSchemaInfo.getNamespace(), defaultSchemaInfo);
    ns2schema.put(customSchemaInfo.getNamespace(), customSchemaInfo);

    WsdlInfo defaultWsdlInfo = new WsdlInfo();
    defaultWsdlInfo.setTargetNamespace("urn:default");
    WsdlInfo customWsdlInfo = new WsdlInfo();
    customWsdlInfo.setTargetNamespace("urn:custom");
    final Map<String, WsdlInfo> ns2wsdl = new HashMap<String, WsdlInfo>();
    ns2wsdl.put(defaultWsdlInfo.getTargetNamespace(), defaultWsdlInfo);
    ns2wsdl.put(customWsdlInfo.getTargetNamespace(), customWsdlInfo);

    final Map<String, String> ns2prefix = new HashMap<String, String>();
    ns2prefix.put("urn:default", "default1");
    ns2prefix.put("urn:custom", "default2");

    final EnunciateFreemarkerModel model = new EnunciateFreemarkerModel() {
      @Override
      public Map<String, String> getNamespacesToPrefixes() {
        return ns2prefix;
      }

      @Override
      public Map<String, SchemaInfo> getNamespacesToSchemas() {
        return ns2schema;
      }

      @Override
      public Map<String, WsdlInfo> getNamespacesToWSDLs() {
        return ns2wsdl;
      }
    };

    final ArrayList<URL> processList = new ArrayList<URL>(Arrays.asList(XMLDeploymentModule.class.getResource("xml.fmt")));
    XMLDeploymentModule module = new XMLDeploymentModule() {
      @Override
      public EnunciateFreemarkerModel getModel() throws IOException {
        return model;
      }


      @Override
      public void processTemplate(URL templateURL, Object model) {
        processList.remove(templateURL);
      }
    };
    SchemaConfig customSchemaConfig = new SchemaConfig();
    customSchemaConfig.setFile("custom.xsd");
    customSchemaConfig.setLocation("urn:custom.xsd");
    customSchemaConfig.setNamespace("urn:custom");
    module.addSchemaConfig(customSchemaConfig);

    WsdlConfig wsdlConfig = new WsdlConfig();
    wsdlConfig.setNamespace("urn:custom");
    wsdlConfig.setFile("custom.wsdl");
    module.addWsdlConfig(wsdlConfig);

    Enunciate enunciate = new Enunciate((String[]) null);
    File generateDir = enunciate.createTempDir();
    enunciate.setGenerateDir(generateDir);
    module.init(enunciate);
    module.doFreemarkerGenerate();

    assertTrue("Not all templates were processed.  Unprocessed templates: " + processList.toString(), processList.isEmpty());
    assertEquals("default1.xsd", defaultSchemaInfo.getProperty("file"));
    assertEquals("default1.xsd", defaultSchemaInfo.getProperty("location"));
    assertEquals("custom.xsd", customSchemaInfo.getProperty("file"));
    assertEquals("urn:custom.xsd", customSchemaInfo.getProperty("location"));
    assertEquals("default1.wsdl", defaultWsdlInfo.getProperty("file"));
    assertEquals("custom.wsdl", customWsdlInfo.getProperty("file"));
    File artifactDir = new File(generateDir, "xml");
    Map<String, File> ns2schemaArtifact = (Map<String, File>) enunciate.getProperty("xml.ns2schema");
    Map<String, File> ns2wsdlArtifact = (Map<String, File>) enunciate.getProperty("xml.ns2wsdl");
    Map<String, File> service2wsdl = (Map<String, File>) enunciate.getProperty("xml.service2wsdl");
    assertEquals(0, service2wsdl.size());
    assertEquals(2, ns2schemaArtifact.size());
    assertEquals(2, ns2wsdlArtifact.size());
    assertEquals(new File(artifactDir, "default1.xsd"), ns2schemaArtifact.get("urn:default"));
    assertEquals(new File(artifactDir, "default1.wsdl"), ns2wsdlArtifact.get("urn:default"));
    assertEquals(new File(artifactDir, "custom.xsd"), ns2schemaArtifact.get("urn:custom"));
    assertEquals(new File(artifactDir, "custom.wsdl"), ns2wsdlArtifact.get("urn:custom"));

  }

}
