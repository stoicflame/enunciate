package net.sf.enunciate.config;

import net.sf.enunciate.contract.validation.DefaultValidator;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.modules.DeploymentModule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.parser.GenericParser;
import org.xml.sax.*;
import sun.misc.Service;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
  private String deploymentHost = null;
  private String deploymentContext = null;
  private Validator validator = new DefaultValidator();
  private final SortedSet<DeploymentModule> modules;
  private final Map<String, String> namespaces = new HashMap<String, String>();
  private final Set<String> jaxbPackageImports = new HashSet<String>();
  private final Set<String> jaxbClassImports = new HashSet<String>();

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
   * Add a JAXB import to the configuration.  Either class or package must be specified, but not both.
   *
   * @param clazz The FQN of the class to import.
   * @param pckg The FQN of the package to import.
   */
  public void addJAXBImport(String clazz, String pckg) {
    if ((clazz != null) && (pckg != null)) {
      throw new IllegalArgumentException("Either 'class' or 'package' must be specified on a JAXB import, but not both.");
    }
    else if (clazz != null) {
      this.jaxbClassImports.add(clazz);
    }
    else if (pckg != null) {
      throw new UnsupportedOperationException("Sorry, jaxb package imports aren't supported yet.  It's on the todo list.  For now, you'll have to import each class.");
      //this.jaxbPackageImports.add(pckg);
    }
    else {
      throw new IllegalArgumentException("Either 'class' or 'package' must be specified on a JAXB import (but not both).");
    }
  }

  /**
   * The set of JAXB package imports specified.
   *
   * @return The set of JAXB package imports specified.
   */
  public Set<String> getJaxbPackageImports() {
    return jaxbPackageImports;
  }

  /**
   * The list of JAXB class imports specified.
   *
   * @return The list of JAXB class imports specified.
   */
  public Set<String> getJaxbClassImports() {
    return jaxbClassImports;
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
    Digester digester = createDigester();
    digester.setErrorHandler(this);
    digester.setValidating(true);
    digester.setSchema(EnunciateConfiguration.class.getResource("enunciate.xsd").toString());
    digester.push(this);

    //set any root-level attributes
    digester.addSetProperties("enunciate");

    //allow a validator to be configured.
    digester.addObjectCreate("enunciate/validator", "class", DefaultValidator.class);
    digester.addSetNext("enunciate/validator", "setValidator");

    //allow for classes and packages to be imported for JAXB.
    digester.addCallMethod("enunciate/jaxb-import", "addJAXBImport", 2);
    digester.addCallParam("enunciate/jaxb-import", 0, "class");
    digester.addCallParam("enunciate/jaxb-import", 1, "package");

    //allow for the deployment configuration to be specified.
    digester.addSetProperties("enunciate/deployment",
                              new String[] {"protocol", "host", "context"},
                              new String[] {"deploymentProtocol", "deploymentHost", "deploymentContext"});

    //allow for namespace prefixes to be specified in the config file.
    digester.addCallMethod("enunciate/namespaces/namespace", "putNamespace", 2);
    digester.addCallParam("enunciate/namespaces/namespace", 0, "uri");
    digester.addCallParam("enunciate/namespaces/namespace", 1, "id");

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
      factory.setValidating(true);

      Properties properties = new Properties();
      properties.put("SAXParserFactory", factory);
      properties.put("schemaLocation", EnunciateConfiguration.class.getResource("enunciate.xsd").toString());
      properties.put("schemaLanguage", "http://www.w3.org/2001/XMLSchema");
      
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
