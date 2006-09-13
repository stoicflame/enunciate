package net.sf.enunciate.config;

import net.sf.enunciate.contract.validation.DefaultValidator;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.modules.DeploymentModule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sun.misc.Service;

import javax.xml.namespace.QName;
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
public class EnunciateConfiguration {

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
    Digester digester = new EnunciateDigester();

    digester.setValidating(true);
    digester.setSchema(EnunciateConfiguration.class.getResource("enunciate.xsd").toString());

    digester.setNamespaceAware(true);
    digester.setRuleNamespaceURI("http://enunciate.sf.net");

    digester.push(this);

    //allow a validator to be configured.
    digester.addObjectCreate("enunciate/validator", "class", DefaultValidator.class);
    digester.addSetNext("enunciate/validator", "validator");

    //todo: add the rules for the namespaces elements.

    //set up the xml module.
    digester.addRule("enunciate/modules/*", new PushModuleRule());
    digester.addSetProperties("enunciate/modules/*");

    //set up all custom modules.
    digester.addObjectCreate("enunciate/modules/custom", "class", BasicDeploymentModule.class);
    digester.addSetProperties("enunciate/modules/custom");
    digester.addSetNext("enunciate/modules/custom", "addModule");
    digester.parse(in);
  }

  /**
   * Rule to push a specific deployment module onto the digester stack.
   */
  private class PushModuleRule extends Rule {

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
      DeploymentModule found = null;
      for (DeploymentModule module : getAllModules()) {
        if ((name.equals(module.getName())) && (namespace.equals(module.getNamespace()))) {
          found = module;
          break;
        }
      }

      if (found == null) {
        throw new IllegalStateException("Configuration found for unknown module: " + new QName(namespace, name));
      }

      getDigester().push(found);
    }

    @Override
    public void end(String string, String string1) throws Exception {
      getDigester().pop();
    }
  }

}
