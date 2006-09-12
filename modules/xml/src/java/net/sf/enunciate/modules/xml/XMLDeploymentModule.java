package net.sf.enunciate.modules.xml;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xml.config.SchemaConfig;
import net.sf.enunciate.modules.xml.config.WsdlConfig;
import net.sf.enunciate.modules.xml.config.XMLRuleSet;
import org.apache.commons.digester.RuleSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Deployment module for the XML schemas and WSDL.
 *
 * @author Ryan Heaton
 */
public class XMLDeploymentModule extends FreemarkerDeploymentModule {

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
   * @return "http://enunciate.sf.net"
   */
  @Override
  public String getNamespace() {
    return "http://enunciate.sf.net";
  }

  /**
   * The URL to "xml.fmt".
   *
   * @return The URL to "xml.fmt".
   */
  protected URL getTemplateURL() {
    return XMLDeploymentModule.class.getResource("xml.fmt");
  }

  @Override
  protected Configuration getConfiguration() {
    Configuration configuration = super.getConfiguration();
    configuration.setObjectWrapper(this.xmlWrapper);
    return configuration;
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
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    Map<String, SchemaInfo> ns2schema = model.getNamespacesToSchemas();
    Map<String, String> ns2prefix = model.getNamespacesToPrefixes();
    Map<String, WsdlInfo> ns2wsdl = model.getNamespacesToWSDLs();

    for (SchemaInfo schemaInfo : ns2schema.values()) {
      //make sure each schema has a "file" and a "location" property.
      String file = ns2prefix.get(schemaInfo.getNamespace()) + ".xsd";
      schemaInfo.setProperty("file", file);
      schemaInfo.setProperty("location", file);
    }

    for (WsdlInfo wsdlInfo : ns2wsdl.values()) {
      //make sure each wsdl has a "file" property.
      String file = ns2prefix.get(wsdlInfo.getTargetNamespace()) + ".xsd";
      wsdlInfo.setProperty("file", file);
    }

    for (SchemaConfig customConfig : this.schemaConfigs) {
      SchemaInfo schemaInfo = ns2schema.get(customConfig.getNamespace());

      if (schemaInfo != null) {
        if (customConfig.getElementFormDefault() != null) {
          schemaInfo.setElementFormDefault(customConfig.getElementFormDefault());
        }

        if (customConfig.getAttributeFormDefault() != null) {
          schemaInfo.setAttributeFormDefault(customConfig.getAttributeFormDefault());
        }

        if (customConfig.getFile() != null) {
          schemaInfo.setProperty("file", customConfig.getFile());
        }

        if (customConfig.getLocation() != null) {
          schemaInfo.setProperty("location", customConfig.getLocation());
        }
      }
    }

    for (WsdlConfig customConfig : this.wsdlConfigs) {
      WsdlInfo wsdlInfo = ns2wsdl.get(customConfig.getNamespace());

      if (wsdlInfo != null) {
        if (customConfig.getFile() != null) {
          wsdlInfo.setProperty("file", customConfig.getFile());
        }
      }
    }

    model.put("prefix", new PrefixMethod());
    processTemplate(getTemplateURL(), model);

    HashMap<String, String> ns2artifact = new HashMap<String, String>();
    HashMap<String, String> service2artifact = new HashMap<String, String>();
    for (WsdlInfo wsdl : ns2wsdl.values()) {
      String file = (String) wsdl.getProperty("file");
      ns2artifact.put(wsdl.getTargetNamespace(), file);
      for (EndpointInterface endpointInterface : wsdl.getEndpointInterfaces()) {
        service2artifact.put(endpointInterface.getServiceName(), file);
      }
    }

    for (SchemaInfo schemaInfo : ns2schema.values()) {
      service2artifact.put(schemaInfo.getNamespace(), (String) schemaInfo.getProperty("file"));
    }

    XMLAPILookup lookup = new XMLAPILookup(ns2artifact, service2artifact);

    getEnunciate().setProperty(XMLAPILookup.class.getName(), lookup);
    getEnunciate().setProperty("xml.dir", new File(enunciate.getGenerateDir(), "xml"));
  }

  @Override
  public RuleSet getConfigurationRules() {
    return this.rules;
  }

  /**
   * @return 1
   */
  @Override
  public int getOrder() {
    return 1;
  }
}
