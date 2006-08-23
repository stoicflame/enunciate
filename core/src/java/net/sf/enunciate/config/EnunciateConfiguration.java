package net.sf.enunciate.config;

import net.sf.enunciate.contract.validation.DefaultValidator;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.modules.DeploymentModule;
import net.sf.enunciate.modules.xfire.XFireDeploymentModule;
import net.sf.enunciate.modules.xml.XMLDeploymentModule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Base configuration object for enunciate.
 *
 * @author Ryan Heaton
 */
public class EnunciateConfiguration {

  private Validator validator = new DefaultValidator();
  private final List<DeploymentModule> modules;
  private final XMLDeploymentModule xmlModule;
  private final XFireDeploymentModule xfireModule;

  public EnunciateConfiguration() {
    this.modules = new ArrayList<DeploymentModule>();
    this.xmlModule = new XMLDeploymentModule();
    this.xfireModule = new XFireDeploymentModule();
    this.modules.add(xmlModule);
    this.modules.add(xfireModule);
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
   * The xml deployment module.
   *
   * @return The xml deployment module.
   */
  public XMLDeploymentModule getXMLModule() {
    return xmlModule;
  }

  /**
   * The xfire deployment module.
   *
   * @return The xfire deployment module.
   */
  public XFireDeploymentModule getXFireModule() {
    return xfireModule;
  }

  /**
   * The list of all deployment modules specified in the configuration.
   *
   * @return The list of all deployment modules specified in the configuration.
   */
  public List<DeploymentModule> getAllModules() {
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
    digester.addRule("enunciate/modules/xml", new PushModuleRule(config.getXMLModule()));
    digester.addSetProperties("enunciate/modules/xml");

    //set up the xfire module.
    digester.addRule("enunciate/modules/xfire", new PushModuleRule(config.getXFireModule()));
    digester.addSetProperties("enunciate/modules/xfire");

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

    private final DeploymentModule module;

    public PushModuleRule(DeploymentModule module) {
      this.module = module;
    }

    @Override
    public void begin(String string, String string1, Attributes attributes) throws Exception {
      getDigester().push(this.module);
    }

    @Override
    public void end(String string, String string1) throws Exception {
      getDigester().pop();
    }
  }

}
