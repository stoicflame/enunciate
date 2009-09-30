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

package org.codehaus.enunciate.config;

import org.codehaus.enunciate.contract.validation.DefaultValidator;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.modules.DeploymentModule;
import org.codehaus.enunciate.modules.BasicAppModule;
import org.codehaus.enunciate.config.war.WebAppConfig;
import org.codehaus.enunciate.config.war.CopyResources;
import org.codehaus.enunciate.config.war.WebAppResource;
import org.codehaus.enunciate.config.war.IncludeExcludeLibs;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.parser.GenericParser;
import org.xml.sax.*;
import sun.misc.Service;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

/**
 * Base configuration object for enunciate.
 *
 * @author Ryan Heaton
 */
public class EnunciateConfiguration implements ErrorHandler {

  private String label = "enunciate";
  private String description = null;
  private String deploymentProtocol = "http";
  private String deploymentHost = "localhost:8080";
  private String deploymentContext = null;
  private String defaultSoapSubcontext = "/soap";
  private String defaultRestSubcontext = "/rest";
  private Validator validator = new DefaultValidator();
  private final SortedSet<DeploymentModule> modules;
  private final Map<String, String> namespaces = new HashMap<String, String>();
  private final Map<String, String> contentTypes = new HashMap<String, String>();
  private final Map<String, String> soapServices2Paths = new HashMap<String, String>();
  private final List<APIImport> apiImports = new ArrayList<APIImport>();
  private final Set<String> disabledRules = new TreeSet<String>();
  private final Set<String> apiIncludePatterns = new TreeSet<String>();
  private final Set<String> apiExcludePatterns = new TreeSet<String>();
  private boolean forceJAXWSSpecCompliance = false;
  private boolean allowEmptyNamespace = true;
  private boolean includeReferencedClasses = true;
  private boolean excludeUnreferencedClasses = true;
  private WebAppConfig webAppConfig;

  /**
   * Create a new enunciate configuration.  The module list will be constructed
   * using Sun's discovery mechanism.
   */
  public EnunciateConfiguration() {
    this.modules = new TreeSet<DeploymentModule>(new DeploymentModuleComparator());

    Iterator discoveredModules = Service.providers(DeploymentModule.class);
    while (discoveredModules.hasNext()) {
      DeploymentModule discoveredModule = (DeploymentModule) discoveredModules.next();
      this.modules.add(discoveredModule);
    }
    this.modules.add(new BasicAppModule());
  }

  /**
   * Construct an enunciate configuration with the specified set of modules.
   *
   * @param modules The modules.
   */
  public EnunciateConfiguration(Collection<DeploymentModule> modules) {
    this.modules = new TreeSet<DeploymentModule>(new DeploymentModuleComparator());
    this.modules.addAll(modules);
  }

  /**
   * The label for this enunciate project.
   *
   * @return The label for this enunciate project.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for this enunciate project.
   *
   * @param label The label for this enunciate project.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * The description for this enunciated API.
   *
   * @return The description for this enunciated API.
   */
  public String getDescription() {
    return description;
  }

  /**
   * The description for this enunciated API.
   *
   * @param description The description for this enunciated API.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The configured validator, if any.
   *
   * @return The configured validator, or null if none.
   */
  public Validator getValidator() {
    return validator;
  }

  /**
   * The validator to use.
   *
   * @param validator The validator to use.
   */
  public void setValidator(Validator validator) {
    this.validator = validator;
  }

  /**
   * The disabled rules.
   *
   * @return The disabled rules.
   */
  public Set<String> getDisabledRules() {
    return disabledRules;
  }

  /**
   * Add a disabled rule.
   *
   * @param ruleId The id of the rule to disable.
   */
  public void addDisabledRule(String ruleId) {
    if (ruleId != null) {
      this.disabledRules.add(ruleId);
    }
  }

  /**
   * The patterns for API includes.
   *
   * @return The patterns for API includes.
   */
  public Set<String> getApiIncludePatterns() {
    return apiIncludePatterns;
  }

  /**
   * The API include pattern.
   *
   * @param pattern The API include pattern.
   */
  public void addApiIncludePattern(String pattern) {
    if (pattern != null) {
      this.apiIncludePatterns.add(pattern);
    }
  }

  /**
   * The patterns for API excludes.
   *
   * @return The patterns for API excludes.
   */
  public Set<String> getApiExcludePatterns() {
    return apiExcludePatterns;
  }

  /**
   * The API exclude pattern.
   *
   * @param pattern The API exclude pattern.
   */
  public void addApiExcludePattern(String pattern) {
    if (pattern != null) {
      this.apiExcludePatterns.add(pattern);
    }
  }

  /**
   * The protocol that will be used when the app is deployed.  Default: "http".
   *
   * @return The protocol that will be used when the app is deployed.  Default: "http".
   */
  public String getDeploymentProtocol() {
    return deploymentProtocol;
  }

  /**
   * The protocol that will be used when the app is deployed.  Default: "http".
   *
   * @param deploymentProtocol The protocol that will be used when the app is deployed.  Default: "http".
   */
  public void setDeploymentProtocol(String deploymentProtocol) {
    this.deploymentProtocol = deploymentProtocol;
  }

  /**
   * The hostname of the host that will host the deployed app.
   *
   * @return The hostname of the host that will host the deployed app.
   */
  public String getDeploymentHost() {
    return deploymentHost;
  }

  /**
   * The hostname of the host that will host the deployed app.
   *
   * @param deploymentHost The hostname of the host that will host the deployed app.
   */
  public void setDeploymentHost(String deploymentHost) {
    this.deploymentHost = deploymentHost;
  }

  /**
   * Whether to force parameter names to be JAX-WS compliant.
   *
   * @return Whether to force parameter names to be JAX-WS compliant.
   */
  public boolean isForceJAXWSSpecCompliance() {
    return forceJAXWSSpecCompliance;
  }

  /**
   * Whether to force parameter names to be JAX-WS compliant.
   *
   * @param forceJAXWSSpecCompliance Whether to force parameter names to be JAX-WS compliant.
   */
  public void setForceJAXWSSpecCompliance(boolean forceJAXWSSpecCompliance) {
    this.forceJAXWSSpecCompliance = forceJAXWSSpecCompliance;
  }

  /**
   * Whether to allow the empty namespace on type/element definitions.
   *
   * @return Whether to allow the empty namespace on type/element definitions.
   */
  public boolean isAllowEmptyNamespace() {
    return allowEmptyNamespace;
  }

  /**
   * Whether to allow the empty namespace on type/element definitions.
   *
   * @param allowEmptyNamespace Whether to allow the empty namespace on type/element definitions.
   */
  public void setAllowEmptyNamespace(boolean allowEmptyNamespace) {
    this.allowEmptyNamespace = allowEmptyNamespace;
  }

  /**
   * Whether to include statically-referenced type definitions (default: true).
   *
   * @return Whether to include statically-referenced type definitions.
   */
  public boolean isIncludeReferencedClasses() {
    return includeReferencedClasses;
  }

  /**
   * Whether to include statically-referenced type definitions (default: true).
   *
   * @param includeReferencedClasses Whether to include statically-referenced type definitions.
   */
  public void setIncludeReferencedClasses(boolean includeReferencedClasses) {
    this.includeReferencedClasses = includeReferencedClasses;
  }

  /**
   * Whether to exclude classes that are not statically referenced (default: true).
   *
   * @return Whether to exclude classes that are not statically referenced.
   */
  public boolean isExcludeUnreferencedClasses() {
    return excludeUnreferencedClasses;
  }

  /**
   * Whether to exclude classes that are not statically referenced.
   *
   * @param excludeUnreferencedClasses Whether to exclude classes that are not statically referenced.
   */
  public void setExcludeUnreferencedClasses(boolean excludeUnreferencedClasses) {
    this.excludeUnreferencedClasses = excludeUnreferencedClasses;
  }

  /**
   * The configuration for the web app.
   *
   * @return The configuration for web app.
   */
  public WebAppConfig getWebAppConfig() {
    return webAppConfig;
  }

  /**
   * The configuration for the web app.
   *
   * @param webAppConfig The configuration for the web app.
   */
  public void setWebAppConfig(WebAppConfig webAppConfig) {
    this.webAppConfig = webAppConfig;
  }

  /**
   * The context at which the deployed app will be mounted.
   *
   * @return The context at which the deployed app will be mounted.
   */
  public String getDeploymentContext() {
    return deploymentContext;
  }

  /**
   * The context at which the deployed app will be mounted.
   *
   * @param deploymentContext The context at which the deployed app will be mounted.
   */
  public void setDeploymentContext(String deploymentContext) {
    if (deploymentContext == null) {
      deploymentContext = "";
    }

    if (!"".equals(deploymentContext)) {
      if (!deploymentContext.startsWith("/")) {
        deploymentContext = "/" + deploymentContext;
      }

      while (deploymentContext.endsWith("/")) {
        deploymentContext = deploymentContext.substring(0, deploymentContext.length() - 1);
      }
    }

    this.deploymentContext = deploymentContext;
  }

  /**
   * Configures a namespace for the specified prefix.
   *
   * @param namespace The namespace.
   * @param prefix    The prefix.
   */
  public void putNamespace(String namespace, String prefix) {
    this.namespaces.put(namespace, prefix);
  }

  /**
   * Configures a content type.
   *
   * @param type The value of the content type.
   * @param id The id of the content type.
   */
  public void putContentType(String type, String id) {
    this.contentTypes.put(type, id);
  }

  /**
   * The default soap context.
   *
   * @return The default soap context.
   */
  public String getDefaultSoapSubcontext() {
    return defaultSoapSubcontext;
  }

  /**
   * The default soap context.
   *
   * @param defaultSoapSubcontext The default soap context.
   */
  public void setDefaultSoapSubcontext(String defaultSoapSubcontext) {
    if (defaultSoapSubcontext == null) {
      throw new IllegalArgumentException("The default SOAP context must not be null.");
    }

    if ("".equals(defaultSoapSubcontext)) {
      throw new IllegalArgumentException("The default SOAP context must not be the emtpy string.");
    }

    if (!defaultSoapSubcontext.startsWith("/")) {
      defaultSoapSubcontext = "/" + defaultSoapSubcontext;
    }

    while (defaultSoapSubcontext.endsWith("/")) {
      defaultSoapSubcontext = defaultSoapSubcontext.substring(0, defaultSoapSubcontext.length() - 1);
    }

    this.defaultSoapSubcontext = defaultSoapSubcontext;
  }

  /**
   * The default rest context.
   *
   * @return The default rest context.
   */
  public String getDefaultRestSubcontext() {
    return defaultRestSubcontext;
  }

  /**
   * The default rest context.
   *
   * @param defaultRestSubcontext The default rest context.
   */
  public void setDefaultRestSubcontext(String defaultRestSubcontext) {
    if (defaultRestSubcontext == null) {
      throw new IllegalArgumentException("The default REST context must not be null.");
    }

    if ("".equals(defaultRestSubcontext)) {
      throw new IllegalArgumentException("The default REST context must not be the emtpy string.");
    }

    if (!defaultRestSubcontext.startsWith("/")) {
      defaultRestSubcontext = "/" + defaultRestSubcontext;
    }

    while (defaultRestSubcontext.endsWith("/")) {
      defaultRestSubcontext = defaultRestSubcontext.substring(0, defaultRestSubcontext.length() - 1);
    }

    this.defaultRestSubcontext = defaultRestSubcontext;
  }

  /**
   * Adds a custom soap endpoint location for an SOAP service.
   *
   * @param serviceName The service name.
   * @param relativePath The relative path to the service.
   */
  public void addSoapEndpointLocation(String serviceName, String relativePath) {
    if (serviceName == null) {
      throw new IllegalArgumentException("A service name must be provided for a custom soap endpoint location.");
    }

    if (relativePath != null) {
      if ("".equals(relativePath)) {
        throw new IllegalArgumentException("A relative path for the custom soap location must be provided for the service name '" + serviceName + "'.");
      }

      if (!relativePath.startsWith("/")) {
        relativePath = "/" + relativePath;
      }

      while (relativePath.endsWith("/")) {
        relativePath = relativePath.substring(0, relativePath.length() - 1);
      }

      this.soapServices2Paths.put(serviceName, relativePath);
    }
  }

  /**
   * Add an API import to the configuration.
   *
   * @param apiImport The API import to add to the configuration.
   */
  public void addAPIImport(APIImport apiImport) {
    this.apiImports.add(apiImport);
  }

  /**
   * Get the list of API imports for this configuration.
   *
   * @return the list of API imports for this configuration.
   */
  public List<APIImport> getAPIImports() {
    return apiImports;
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespacesToPrefixes() {
    return this.namespaces;
  }

  /**
   * The map of content types to ids.
   *
   * @return The map of content types to ids.
   */
  public Map<String, String> getContentTypesToIds() {
    return contentTypes;
  }

  /**
   * Get the map of SOAP service names to custom paths.
   *
   * @return The map of soap service names to custom paths.
   */
  public Map<String, String> getSoapServices2Paths() {
    return soapServices2Paths;
  }

  /**
   * The list of all deployment modules specified in the configuration.
   *
   * @return The list of all deployment modules specified in the configuration.
   */
  public SortedSet<DeploymentModule> getAllModules() {
    return modules;
  }

  /**
   * Add a module to the list of modules.
   *
   * @param module The module to add.
   */
  public void addModule(DeploymentModule module) {
    this.modules.add(module);
  }

  /**
   * The list of enabled modules in the configuration.
   *
   * @return The list of enabled modules in the configuration.
   */
  public List<DeploymentModule> getEnabledModules() {
    ArrayList<DeploymentModule> enabledModules = new ArrayList<DeploymentModule>();

    for (DeploymentModule module : getAllModules()) {
      if (!module.isDisabled()) {
        enabledModules.add(module);
      }
    }

    return enabledModules;
  }

  /**
   * Loads the configuration specified by the given config file.
   *
   * @param file The file.
   */
  public void load(File file) throws IOException, SAXException {
    load(new FileInputStream(file));
  }

  /**
   * Loads the configuration specified by the given stream.
   *
   * @param in The stream.
   */
  public void load(InputStream in) throws IOException, SAXException {
    load(new InputStreamReader(in, "utf-8"));
  }

  /**
   * Loads the configuration specified by the given stream.
   *
   * @param in The stream.
   */
  public void load(Reader in) throws IOException, SAXException {
    Digester digester = createDigester();
    digester.setErrorHandler(this);
    digester.setValidating(false);
    digester.push(this);

    //set any root-level attributes
    digester.addSetProperties("enunciate");

    //allow a validator to be configured.
    digester.addObjectCreate("enunciate/validator", "class", DefaultValidator.class);
    digester.addSetNext("enunciate/validator", "setValidator");

    //set up the include/excludes
    digester.addSetProperties("enunciate/api-classes");
    digester.addCallMethod("enunciate/api-classes/include", "addApiIncludePattern", 1);
    digester.addCallParam("enunciate/api-classes/include", 0, "pattern");
    digester.addCallMethod("enunciate/api-classes/exclude", "addApiExcludePattern", 1);
    digester.addCallParam("enunciate/api-classes/exclude", 0, "pattern");

    //set up the ability to disable certain rules
    digester.addCallMethod("enunciate/disable-rule", "addDisabledRule", 1);
    digester.addCallParam("enunciate/disable-rule", 0, "id");

    //allow for classes and packages to be imported for JAXB.
    digester.addObjectCreate("enunciate/api-import", APIImport.class);
    digester.addSetProperties("enunciate/api-import",
                              new String[] {"classname", "class", "pattern", "seekSource"},
                              new String[] {"pattern", "pattern", "pattern", "seekSource"});
    digester.addSetNext("enunciate/api-import", "addAPIImport");

    //allow for the deployment configuration to be specified.
    digester.addSetProperties("enunciate/deployment",
                              new String[] {"protocol", "host", "context"},
                              new String[] {"deploymentProtocol", "deploymentHost", "deploymentContext"});

    //allow for namespace prefixes to be specified in the config file.
    digester.addCallMethod("enunciate/namespaces/namespace", "putNamespace", 2);
    digester.addCallParam("enunciate/namespaces/namespace", 0, "uri");
    digester.addCallParam("enunciate/namespaces/namespace", 1, "id");

    //allow for the default soap subcontext to be set.
    digester.addSetProperties("enunciate/services/rest");

    //allow for namespace prefixes to be specified in the config file.
    digester.addCallMethod("enunciate/services/rest/content-types/content-type", "putContentType", 2);
    digester.addCallParam("enunciate/services/rest/content-types/content-type", 0, "type");
    digester.addCallParam("enunciate/services/rest/content-types/content-type", 1, "id");

    //allow for the default soap subcontext to be set.
    digester.addSetProperties("enunciate/services/soap", "defaultSubcontext", "defaultSoapSubcontext");

    //allow for custom location of soap endpoints
    digester.addCallMethod("enunciate/services/soap/service", "addSoapEndpointLocation", 2);
    digester.addCallParam("enunciate/services/soap/service", 0, "name");
    digester.addCallParam("enunciate/services/soap/service", 1, "relativePath");

    digester.addObjectCreate("enunciate/webapp", WebAppConfig.class);
    digester.addSetProperties("enunciate/webapp");
    digester.addSetNext("enunciate/webapp", "setWebAppConfig");

    digester.addObjectCreate("enunciate/webapp/resource-env-ref", WebAppResource.class);
    digester.addSetProperties("enunciate/webapp/resource-env-ref");
    digester.addSetNext("enunciate/webapp/resource-env-ref", "addResourceEnvRef");

    digester.addObjectCreate("enunciate/webapp/resource-ref", WebAppResource.class);
    digester.addSetProperties("enunciate/webapp/resource-ref");
    digester.addSetNext("enunciate/webapp/resource-ref", "addResourceRef");

    digester.addObjectCreate("enunciate/webapp/env", WebAppResource.class);
    digester.addSetProperties("enunciate/webapp/env");
    digester.addSetNext("enunciate/webapp/env", "addEnvEntry");

    digester.addObjectCreate("enunciate/webapp/excludeJar", IncludeExcludeLibs.class);
    digester.addSetProperties("enunciate/webapp/excludeJar");
    digester.addSetNext("enunciate/webapp/excludeJar", "addExcludeLibs");

    digester.addObjectCreate("enunciate/webapp/excludeLibs", IncludeExcludeLibs.class);
    digester.addSetProperties("enunciate/webapp/excludeLibs");
    digester.addSetNext("enunciate/webapp/excludeLibs", "addExcludeLibs");

    digester.addObjectCreate("enunciate/webapp/includeLibs", IncludeExcludeLibs.class);
    digester.addSetProperties("enunciate/webapp/includeLibs");
    digester.addSetNext("enunciate/webapp/includeLibs", "addIncludeLibs");

    digester.addCallMethod("enunciate/webapp/manifest/attribute", "addManifestAttribute", 3);
    digester.addCallParam("enunciate/webapp/manifest/attribute", 0, "section");
    digester.addCallParam("enunciate/webapp/manifest/attribute", 1, "name");
    digester.addCallParam("enunciate/webapp/manifest/attribute", 2, "value");

    digester.addObjectCreate("enunciate/webapp/resources", CopyResources.class);
    digester.addSetProperties("enunciate/webapp/resources");
    digester.addSetNext("enunciate/webapp/resources", "addCopyResources");

    digester.addObjectCreate("enunciate/webapp/globalServletFilter", WebAppComponent.class);
    digester.addSetProperties("enunciate/webapp/globalServletFilter");
    digester.addSetNext("enunciate/webapp/globalServletFilter", "addGlobalServletFilter");

    digester.addCallMethod("enunciate/webapp/globalServletFilter/init-param", "addInitParam", 2);
    digester.addCallParam("enunciate/webapp/globalServletFilter/init-param", 0, "name");
    digester.addCallParam("enunciate/webapp/globalServletFiltery/init-param", 1, "value");

    //set up the module configuration.
    for (DeploymentModule module : getAllModules()) {
      String pattern = String.format("enunciate/modules/%s", module.getName());
      digester.addRule(pattern, new PushModuleRule(module));
      digester.addSetProperties(pattern);
      RuleSet configRules = module.getConfigurationRules();
      if (configRules != null) {
        digester.addRuleSet(configRules);
      }
    }

    digester.parse(in);
  }

  /**
   * Create the digester.
   *
   * @return The digester that was created.
   */
  protected Digester createDigester() throws SAXException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setValidating(false);

      Properties properties = new Properties();
      properties.put("SAXParserFactory", factory);
//      properties.put("schemaLocation", EnunciateConfiguration.class.getResource("enunciate.xsd").toString());
//      properties.put("schemaLanguage", "http://www.w3.org/2001/XMLSchema");

      SAXParser parser = GenericParser.newSAXParser(properties);
      return new Digester(parser);
    }
    catch (ParserConfigurationException e) {
      throw new SAXException(e);
    }
  }

  /**
   * Handle a warning.
   *
   * @param warning The warning.
   */
  public void warning(SAXParseException warning) throws SAXException {
    System.err.println(warning.getMessage());
  }

  /**
   * Handle an error.
   *
   * @param error The error.
   */
  public void error(SAXParseException error) throws SAXException {
    throw error;
  }

  /**
   * Handle a fatal.
   *
   * @param fatal The fatal.
   */
  public void fatalError(SAXParseException fatal) throws SAXException {
    throw fatal;
  }

  /**
   * Rule to push a specific deployment module onto the digester stack.
   */
  private static class PushModuleRule extends Rule {

    private final DeploymentModule module;

    public PushModuleRule(DeploymentModule module) {
      this.module = module;
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
      getDigester().push(module);
    }

    @Override
    public void end(String namespace, String name) throws Exception {
      getDigester().pop();
    }

  }

}
