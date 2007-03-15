/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.xml;

import junit.framework.TestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.xml.config.SchemaConfig;
import org.codehaus.enunciate.modules.xml.config.WsdlConfig;

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
    assertEquals("default1.xsd", defaultSchemaInfo.getProperty("filename"));
    assertEquals("default1.xsd", defaultSchemaInfo.getProperty("location"));
    assertEquals("custom.xsd", customSchemaInfo.getProperty("filename"));
    assertEquals("urn:custom.xsd", customSchemaInfo.getProperty("location"));
    assertEquals("default1.wsdl", defaultWsdlInfo.getProperty("filename"));
    assertEquals("custom.wsdl", customWsdlInfo.getProperty("filename"));
    File artifactDir = new File(generateDir, "xml");
    assertEquals(new File(artifactDir, "default1.xsd"), defaultSchemaInfo.getProperty("file"));
    assertEquals(new File(artifactDir, "default1.wsdl"), defaultWsdlInfo.getProperty("file"));
    assertEquals(new File(artifactDir, "custom.xsd"), customSchemaInfo.getProperty("file"));
    assertEquals(new File(artifactDir, "custom.wsdl"), customWsdlInfo.getProperty("file"));

  }

}
