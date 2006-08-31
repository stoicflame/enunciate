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

  public EnunciateConfiguration() {
    this.modules = new TreeSet<DeploymentModule>();
    Iterator discoveredModules = Service.providers(DeploymentModule.class);
    while (discoveredModules.hasNext()) {
      DeploymentModule discoveredModule = (DeploymentModule) discoveredModules.next();
      this.modules.add(discoveredModule);
    }
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
   * Reads a configuration from an input stream.
   *
   * @param in The input stream to read from.
   * @return The configuration.
   */
  public static EnunciateConfiguration readFrom(InputStream in) throws IOException, SAXException {
    Digester digester = new EnunciateDigester();

    digester.setValidating(true);
    digester.setSchema(EnunciateConfiguration.class.getResource("enunciate.xsd").toString());

    digester.setNamespaceAware(true);
    digester.setRuleNamespaceURI("http://enunciate.sf.net");

    EnunciateConfiguration config = new EnunciateConfiguration();
    digester.push(config);

    //allow a validator to be configured.
    digester.addObjectCreate("enunciate/validator", "class", DefaultValidator.class);
    digester.addSetNext("enunciate/validator", "validator");

    //todo: add the rules for the namespaces elements.

    //set up the xml module.
    digester.addRule("enunciate/modules/*", new PushModuleRule(config.getAllModules()));
    digester.addSetProperties("enunciate/modules/*");

    //set up all custom modules.
    digester.addObjectCreate("enunciate/modules/custom", "class", BasicDeploymentModule.class);
    digester.addSetProperties("enunciate/modules/custom");
    digester.addSetNext("enunciate/modules/custom", "addModule");

    return (EnunciateConfiguration) digester.parse(in);
  }

  /**
   * Rule to push a specific deployment module onto the digester stack.
   */
  private static class PushModuleRule extends Rule {

    private final Set<DeploymentModule> modules;

    public PushModuleRule(Set<DeploymentModule> modules) {
      this.modules = modules;
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
      DeploymentModule found = null;
      for (DeploymentModule module : modules) {
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
