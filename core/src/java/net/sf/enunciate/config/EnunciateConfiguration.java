package net.sf.enunciate.config;

import net.sf.enunciate.contract.validation.DefaultValidator;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.modules.DeploymentModule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSet;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import sun.misc.Service;

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
  private Validator validator = new DefaultValidator();
  private final SortedSet<DeploymentModule> modules;

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
      if (module instanceof BasicDeploymentModule) {
        if (((BasicDeploymentModule) module).isDisabled()) {
          continue;
        }
      }

      enabledModules.add(module);
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
    Digester digester = new Digester();
    digester.setErrorHandler(this);
    digester.setValidating(true);
    digester.setSchema(EnunciateConfiguration.class.getResource("enunciate.xsd").toString());
    digester.setNamespaceAware(true);
    digester.setRuleNamespaceURI("http://enunciate.sf.net");
    digester.push(this);

    //set any root-level attributes
    digester.addSetProperties("enunciate");

    //allow a validator to be configured.
    digester.addObjectCreate("enunciate/validator", "class", DefaultValidator.class);
    digester.addSetNext("enunciate/validator", "validator");

    //todo: add the rules for the namespaces elements.

    //set up explicit custom module configuration.
    digester.addObjectCreate("enunciate/modules/custom", "class", BasicDeploymentModule.class);
    digester.addSetProperties("enunciate/modules/custom");
    digester.addSetNext("enunciate/modules/custom", "addModule");

    //set up the module configuration.
    for (DeploymentModule module : getAllModules()) {
      digester.setRuleNamespaceURI(module.getNamespace());
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
