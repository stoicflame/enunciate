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

package org.codehaus.enunciate.modules.xml;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.xml.config.SchemaConfig;
import org.codehaus.enunciate.modules.xml.config.WsdlConfig;
import org.codehaus.enunciate.modules.xml.config.XMLRuleSet;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.template.freemarker.IsDefinedGloballyMethod;
import org.apache.commons.digester.RuleSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * <h1>XML Module</h1>
 *
 * <p>The XML deployment module generates the consolidated WSDLs and schemas for the API.</a>.
 *
 * <ul>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 *
 * <p>The only significant step in the XML deployment module is the "generate" step.  This step generates the
 * WSDLs and schemas for the API.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The configuration for the XML deployment module is specified by the "xml" child element of the "modules"
 * element of the enunciate configuration file.  It supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "<b>prettyPrint</b>" attribute specifies that the generated XML files should be pretty printed.  Default is "true".</li>
 * </ul>
 *
 * <h3>The "schema" element(s)</h3>
 *
 * <p>The "xml" element supports an arbitrary number of "schema" child elements that are used to configure the schema for
 * a specified namespace.  It supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "<b>namespace</b>" attribute specifies the namespace of the schema that is to be configured.</li>
 *   <li>The "<b>useFile</b>" attribute specifies the (already existing) file to use for this schema.
 *   <li>The "<b>file</b>" attribute specifies name of the schema file.  The default is the prefix appended with ".xsd".</li>
 *   <li>The "<b>location</b>" attribute specifies name of the schema location (i.e. how the schema is to be located by other schemas).
 * The default is the name of the file.</li>
 *   <li>The "<b>jaxbBindingVersion</b>" attribute specifies the JAXB binding version for this schema. (Advanced, usually not needed.)</li>
 * </ul>
 *
 * <p>The "schema" element also supports a nested subelement, "appinfo" whose contents will be inlined into the schema "appinfo" annotation.</p>
 *
 * <h3>The "wsdl" element(s)</h3>
 *
 * <p>The "xml" element supports an arbitrary number of "wsdl" child elements that are used to configure the wsdl for
 * a specified namespace.  It supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "<b>namespace</b>" attribute specifies the namespace of the wsdl that is to be configured.</li>
 *   <li>The "<b>useFile</b>" attribute specifies the (already existing) file to use for this schema.
 *   <li>The "<b>file</b>" attribute specifies name of the wsdl file.  The default is the prefix appended with ".wsdl".</li>
 * </ul>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The XML deployment module exports artifacts for each WSDL and schema produced.  The id of the artifact is the name of the prefix for the namespace of hte
 * file appended with ".wsdl" (for wsdls) and ".xsd" (for schemas).</p>
 *
 * @author Ryan Heaton
 * @docFileName module_xml.html
 */
public class XMLDeploymentModule extends FreemarkerDeploymentModule {

  private boolean prettyPrint = true;
  private boolean validateSchemas = true;
  private boolean inlineWSDLSchemas = true;
  private final XMLAPIObjectWrapper xmlWrapper = new XMLAPIObjectWrapper();
  private final XMLRuleSet rules = new XMLRuleSet();
  private final ArrayList<SchemaConfig> schemaConfigs = new ArrayList<SchemaConfig>();
  private final ArrayList<WsdlConfig> wsdlConfigs = new ArrayList<WsdlConfig>();

  /**
   * @return "xml"
   */
  @Override
  public String getName() {
    return "xml";
  }

  /**
   * The URL to "xml.fmt".
   *
   * @return The URL to "xml.fmt".
   */
  protected URL getTemplateURL() {
    return XMLDeploymentModule.class.getResource("xml.fmt");
  }

  /**
   * Add a custom schema configuration.
   *
   * @param config The configuration to add.
   */
  public void addSchemaConfig(SchemaConfig config) {
    this.schemaConfigs.add(config);
  }

  /**
   * Add a custom wsdl configuration.
   *
   * @param config The configuration to add.
   */
  public void addWsdlConfig(WsdlConfig config) {
    this.wsdlConfigs.add(config);
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    for (SchemaConfig schemaConfig : this.schemaConfigs) {
      if (schemaConfig.getUseFile() != null) {
        File useFile = enunciate.resolvePath(schemaConfig.getUseFile());
        if (!useFile.exists()) {
          throw new EnunciateException("File " + useFile + " does not exist.");
        }
      }
    }
    for (WsdlConfig wsdlConfig : this.wsdlConfigs) {
      if (wsdlConfig.getUseFile() != null) {
        File useFile = enunciate.resolvePath(wsdlConfig.getUseFile());
        if (!useFile.exists()) {
          throw new EnunciateException("File " + useFile + " does not exist.");
        }
      }
    }
  }
  
  // Inherited.
  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    Map<String, SchemaInfo> ns2schema = model.getNamespacesToSchemas();
    Map<String, String> ns2prefix = model.getNamespacesToPrefixes();
    Map<String, WsdlInfo> ns2wsdl = model.getNamespacesToWSDLs();

    for (SchemaInfo schemaInfo : ns2schema.values()) {
      //make sure each schema has a "file" and a "location" property.
      String prefix = ns2prefix.get(schemaInfo.getNamespace());
      if (prefix != null) {
        String file = prefix + ".xsd";
        schemaInfo.setProperty("filename", file);
        schemaInfo.setProperty("location", file);
      }
    }

    for (WsdlInfo wsdlInfo : ns2wsdl.values()) {
      //make sure each wsdl has a "file" property.
      String prefix = ns2prefix.get(wsdlInfo.getTargetNamespace());
      if (prefix != null) {
        String file = prefix + ".wsdl";
        wsdlInfo.setProperty("filename", file);
        wsdlInfo.setProperty("inlineSchema", inlineWSDLSchemas);
      }
    }

    for (SchemaConfig customConfig : this.schemaConfigs) {
      SchemaInfo schemaInfo = ns2schema.get(customConfig.getNamespace());

      if (schemaInfo != null) {
        if (customConfig.getUseFile() != null) {
          File useFile = getEnunciate().resolvePath(customConfig.getUseFile());
          if (!useFile.exists()) {
            throw new IllegalStateException("File " + useFile + " does not exist.");
          }
          schemaInfo.setProperty("filename", useFile.getName());
          schemaInfo.setProperty("location", useFile.getName());
          schemaInfo.setProperty("file", useFile);
        }

        if (customConfig.getFile() != null) {
          schemaInfo.setProperty("filename", customConfig.getFile());
          schemaInfo.setProperty("location", customConfig.getFile());
        }

        if (customConfig.getLocation() != null) {
          schemaInfo.setProperty("location", customConfig.getLocation());
        }

        if (customConfig.getJaxbBindingVersion() != null) {
          schemaInfo.setProperty("jaxbBindingVersion", customConfig.getJaxbBindingVersion());
        }

        if (customConfig.getAppinfo() != null) {
          schemaInfo.setProperty("appinfo", customConfig.getAppinfo());
        }
      }
    }

    for (WsdlConfig customConfig : this.wsdlConfigs) {
      WsdlInfo wsdlInfo = ns2wsdl.get(customConfig.getNamespace());

      if (wsdlInfo != null) {
        if (customConfig.getUseFile() != null) {
          File useFile = getEnunciate().resolvePath(customConfig.getUseFile());
          if (!useFile.exists()) {
            throw new IllegalStateException("File " + useFile + " does not exist.");
          }
          wsdlInfo.setProperty("filename", useFile.getName());
          wsdlInfo.setProperty("file", useFile);
        }

        if (customConfig.getFile() != null) {
          wsdlInfo.setProperty("filename", customConfig.getFile());
        }

        wsdlInfo.setProperty("inlineSchema", customConfig.isInlineSchema());
      }
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    Map<String, SchemaInfo> ns2schema = model.getNamespacesToSchemas();
    Map<String, WsdlInfo> ns2wsdl = model.getNamespacesToWSDLs();

    model.put("prefix", new PrefixMethod());
    model.put("isDefinedGlobally", new IsDefinedGloballyMethod());
    File artifactDir = getGenerateDir();
    model.setFileOutputDirectory(artifactDir);
    boolean upToDate = isUpToDate(artifactDir);
    if (!upToDate) {
      processTemplate(getTemplateURL(), model);
    }
    else {
      info("Skipping generation of XML files since everything appears up-to-date...");
    }

    for (WsdlInfo wsdl : ns2wsdl.values()) {
      File wsdlFile = (File) wsdl.getProperty("file");
      if (wsdlFile == null) {
        String file = (String) wsdl.getProperty("filename");
        wsdlFile = new File(artifactDir, file);
        wsdl.setProperty("file", wsdlFile);

        if (!upToDate && prettyPrint) {
          prettyPrint(wsdlFile);
        }
      }

      FileArtifact wsdlArtifact = new FileArtifact(getName(), wsdl.getId() + ".wsdl", wsdlFile);
      wsdlArtifact.setDescription("WSDL file for namespace " + wsdl.getTargetNamespace());
      getEnunciate().addArtifact(wsdlArtifact);
    }

    for (SchemaInfo schemaInfo : ns2schema.values()) {
      File schemaFile = (File) schemaInfo.getProperty("file");
      if (schemaFile == null) {
        String file = (String) schemaInfo.getProperty("filename");
        schemaFile = new File(artifactDir, file);
        schemaInfo.setProperty("file", schemaFile);

        if (!upToDate && prettyPrint) {
          prettyPrint(schemaFile);
        }

        if (!upToDate && validateSchemas) {
          //todo: write some logic to validate the schemas.
        }
      }

      FileArtifact schemaArtifact = new FileArtifact(getName(), schemaInfo.getId() + ".xsd", schemaFile);
      schemaArtifact.setDescription("Schema file for namespace " + schemaInfo.getNamespace());
      getEnunciate().addArtifact(schemaArtifact);
    }
  }

  /**
   * Whether the artifact directory is up-to-date.
   *
   * @param artifactDir The artifact directory.
   * @return Whether the artifact directory is up-to-date.
   */
  protected boolean isUpToDate(File artifactDir) {
    return enunciate.isUpToDateWithSources(artifactDir);
  }

  /**
   * Pretty-prints the specified xml file.
   *
   * @param file The file to pretty-print.
   */
  protected void prettyPrint(File file) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(false);
      SAXParser parser = factory.newSAXParser();
      File prettyFile = File.createTempFile("enunciate", file.getName());
      parser.parse(file, new PrettyPrinter(prettyFile));

      if (file.delete()) {
        enunciate.copyFile(prettyFile, file);
      }
      else {
        warn("Unable to delete %s.  Skipping pretty-print transformation....", file);
      }
    }
    catch (Exception e) {
      //fall through... skip pretty printing.
      warn("Unable to pretty-print %s (%s).", file, e.getMessage());
      if (enunciate.isDebug()) {
        e.printStackTrace(System.err);
      }
    }
  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return xmlWrapper;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return this.rules;
  }

  @Override
  public Validator getValidator() {
    return new XMLValidator();
  }

  /**
   * Whether to pretty-print the xml.
   *
   * @param prettyPrint Whether to pretty-print the xml.
   */
  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  /**
   * Whether to validate the generated schemas in an attempt to catch possible errors that enunciate might have missed.
   *
   * @param validateSchemas Whether to validate the generated schemas in an attempt to catch possible errors that enunciate might have missed.
   */
  public void setValidateSchemas(boolean validateSchemas) {
    this.validateSchemas = validateSchemas;
  }

  /**
   * Whether to inline the WSDL schemas.
   *
   * @param inlineWSDLSchemas Whether to inline the WSDL schemas.
   */
  public void setInlineWSDLSchemas(boolean inlineWSDLSchemas) {
    this.inlineWSDLSchemas = inlineWSDLSchemas;
  }
}
