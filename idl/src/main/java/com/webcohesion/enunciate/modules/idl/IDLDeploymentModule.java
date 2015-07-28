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

package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.module.ApiRegistryAwareModule;
import com.webcohesion.enunciate.module.BasicGeneratingModule;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class IDLDeploymentModule extends BasicGeneratingModule implements ApiRegistryAwareModule {

  private boolean prettyPrint = true;
  private boolean validateSchemas = true;
  private boolean inlineWSDLSchemas = true;
  private boolean disableWadl = false;
  private String wadlStylesheetUri = null;
  private final XMLAPIObjectWrapper xmlWrapper = new XMLAPIObjectWrapper();
  private Set<String> facetIncludes = new TreeSet<String>();
  private Set<String> facetExcludes = new TreeSet<String>();
  private ApiRegistry apiRegistry;

  /**
   * @return "idl"
   */
  @Override
  public String getName() {
    return "idl";
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  public Map<String, SchemaConfig> getSchemaConfigs() {
    HashMap<String, SchemaConfig> configs = new HashMap<String, SchemaConfig>();

    List<HierarchicalConfiguration> schemas = this.config.configurationsAt("schema");
    for (HierarchicalConfiguration schema : schemas) {
      SchemaConfig schemaConfig = new SchemaConfig();
      schemaConfig.setAppinfo(schema.getString("[@appinfo]", null));
      schemaConfig.setFilename(schema.getString("[@filename]", null));
      schemaConfig.setJaxbBindingVersion(schema.getString("[@jaxbBindingVersion]", null));
      schemaConfig.setLocation(schema.getString("[@location]", null));
      String ns = schema.getString("[@namespace]", null);
      if ("".equals(ns)) {
        //default namspace to be represented as null.
        ns = null;
      }
      schemaConfig.setNamespace(ns);
      String useFile = schema.getString("[@useFile]", null);
      if (useFile != null) {
        File file = resolveFile(useFile);
        if (!file.exists()) {
          throw new EnunciateException(String.format("Invalid schema config: file %s does not exist.", useFile));
        }
        schemaConfig.setUseFile(file);
      }
      configs.put(schemaConfig.getNamespace(), schemaConfig);
    }

    return configs;
  }

  public Map<String, WsdlConfig> getWsdlConfigs() {
    HashMap<String, WsdlConfig> configs = new HashMap<String, WsdlConfig>();
    List<HierarchicalConfiguration> wsdls = this.config.configurationsAt("wsdl");
    for (HierarchicalConfiguration wsdl : wsdls) {
      WsdlConfig wsdlConfig = new WsdlConfig();
      wsdlConfig.setFilename(wsdl.getString("[@filename]", null));
      wsdlConfig.setNamespace(wsdl.getString("[@namespace]", null));
      wsdlConfig.setInlineSchema(wsdl.getBoolean("[@inlineSchema]", true));
      String useFile = wsdl.getString("[@useFile]", null);
      if (useFile != null) {
        File file = resolveFile(useFile);
        if (!file.exists()) {
          throw new EnunciateException(String.format("Invalid wsdl config: file %s does not exist.", useFile));
        }
        wsdlConfig.setUseFile(file);
      }
      configs.put(wsdlConfig.getNamespace(), wsdlConfig);
    }

    return configs;
  }

  @Override
  public void call(EnunciateContext context) {
    if (!isDisabled()) {
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

          if (customConfig.getFilename() != null) {
            schemaInfo.setProperty("filename", customConfig.getFilename());
            schemaInfo.setProperty("location", customConfig.getFilename());
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

          if (customConfig.getFilename() != null) {
            wsdlInfo.setProperty("filename", customConfig.getFilename());
          }

          wsdlInfo.setProperty("inlineSchema", customConfig.isInlineSchema());
        }
      }

      EnunciateConfiguration config = model.getEnunciateConfig();
      for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
        for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
          if (!ei.getMetaData().containsKey("soapPath")) {
            //if we don't have the soap path set by some other jax-ws implementation provider module
            //then we need to set it ourselves.

            String path = "/soap/" + ei.getServiceName();
            if (config != null) {
              path = config.getDefaultSoapSubcontext() + '/' + ei.getServiceName();
              if (config.getSoapServices2Paths().containsKey(ei.getServiceName())) {
                path = config.getSoapServices2Paths().get(ei.getServiceName());
              }
            }

            ei.putMetaData("soapPath", path);
          }
        }
      }

      for (RootResource resource : model.getRootResources()) {
        for (ResourceMethod resourceMethod : resource.getResourceMethods(true)) {
          if (!resourceMethod.getMetaData().containsKey("defaultSubcontext")) {
            //if we don't have the defaultSubcontext set by some other jax-rs implementation provider module
            //then we need to set it ourselves.

            resourceMethod.putMetaData("defaultSubcontext", config == null ? "/rest" : config.getDefaultRestSubcontext());
          }
        }
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
    model.setVariable("uniqueContentTypes", new UniqueContentTypesMethod());
    model.put("wadlStylesheetUri", this.wadlStylesheetUri);
    model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());
    model.put("qnameForType", new QNameForTypeMethod());
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

    if (!isDisableWadl()) {
      File wadl = new File(artifactDir, "application.wadl");
      if (wadl.exists()) {
        FileArtifact wadlArtifact = new FileArtifact(getName(), "application.wadl", wadl);
        wadlArtifact.setDescription("WADL document");
        getEnunciate().addArtifact(wadlArtifact);
        prettyPrint(wadl);
        model.setWadlFile(wadl);
      }
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
      File prettyFile = enunciate.createTempFile("enunciate", file.getName());
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

  /**
   * Whether to disable the WADL.
   *
   * @return Whether to disable the WADL.
   */
  public boolean isDisableWadl() {
    return disableWadl;
  }

  /**
   * Whether to disable the WADL.
   *
   * @param disableWadl Whether to disable the WADL.
   */
  public void setDisableWadl(boolean disableWadl) {
    this.disableWadl = disableWadl;
  }

  /**
   * The set of facets to include.
   *
   * @return The set of facets to include.
   */
  public Set<String> getFacetIncludes() {
    return facetIncludes;
  }

  /**
   * Add a facet include.
   *
   * @param name The name.
   */
  public void addFacetInclude(String name) {
    if (name != null) {
      this.facetIncludes.add(name);
    }
  }

  /**
   * The set of facets to exclude.
   *
   * @return The set of facets to exclude.
   */
  public Set<String> getFacetExcludes() {
    return facetExcludes;
  }

  /**
   * Add a facet exclude.
   *
   * @param name The name.
   */
  public void addFacetExclude(String name) {
    if (name != null) {
      this.facetExcludes.add(name);
    }
  }

  public void setFreemarkerProcessingTemplate(String freemarkerProcessingTemplate) throws MalformedURLException {
    this.freemarkerProcessingTemplate = freemarkerProcessingTemplate;
  }

  public URL getFreemarkerProcessingTemplateURL() {
    return freemarkerProcessingTemplateURL;
  }

  public void setFreemarkerProcessingTemplateURL(URL freemarkerProcessingTemplateURL) {
    this.freemarkerProcessingTemplateURL = freemarkerProcessingTemplateURL;
  }
}
