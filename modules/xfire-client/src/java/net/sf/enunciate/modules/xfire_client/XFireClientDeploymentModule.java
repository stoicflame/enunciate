package net.sf.enunciate.modules.xfire_client;

import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxws.WebFault;
import net.sf.enunciate.contract.jaxws.WebMethod;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xfire_client.config.ClientPackageConversion;
import net.sf.enunciate.modules.xfire_client.config.XFireClientRuleSet;
import net.sf.enunciate.util.ClassDeclarationComparator;
import org.apache.commons.digester.RuleSet;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.TreeSet;

/**
 * Deployment module for XFire.
 *
 * @author Ryan Heaton
 */
public class XFireClientDeploymentModule extends FreemarkerDeploymentModule {

  private final LinkedHashMap<String, String> clientPackageConversions;
  private final XFireClientRuleSet configurationRules;

  public XFireClientDeploymentModule() {
    this.clientPackageConversions = new LinkedHashMap<String, String>();
    this.configurationRules = new XFireClientRuleSet();
  }

  /**
   * @return "xfire-client"
   */
  @Override
  public String getName() {
    return "xfire-client";
  }

  /**
   * @return "http://enunciate.sf.net"
   */
  @Override
  public String getNamespace() {
    return "http://enunciate.sf.net";
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return XFireClientDeploymentModule.class.getResource(template);
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the JDK 1.3 client code.
    LinkedHashMap<String, String> conversions = getClientPackageConversions();
    model.put("packageFor", new ClientPackageForMethod(conversions));
    model.put("classnameFor", new ClientClassnameForMethod(conversions));

    URL eiTemplate = getTemplateURL("client-endpoint-interface.fmt");
    URL faultTemplate = getTemplateURL("client-web-fault.fmt");
    URL enumTypeTemplate = getTemplateURL("client-enum-type.fmt");
    URL simpleTypeTemplate = getTemplateURL("client-simple-type.fmt");
    URL complexTypeTemplate = getTemplateURL("client-complex-type.fmt");
    URL xfireEnumTemplate = getTemplateURL("xfire-enum-type.fmt");
    URL xfireSimpleTemplate = getTemplateURL("xfire-simple-type.fmt");
    URL xfireComplexTemplate = getTemplateURL("xfire-complex-type.fmt");

    //process the endpoint interfaces and gather the list of web faults...
    TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new ClassDeclarationComparator());
    for (WsdlInfo wsdlInfo : model.getNamespacesToWSDLs().values()) {
      for (EndpointInterface ei : wsdlInfo.getEndpointInterfaces()) {
        //first process the templates for the endpoint interfaces.
        model.put("endpointInterface", ei);

        processTemplate(eiTemplate, model);

        for (WebMethod webMethod : ei.getWebMethods()) {
          allFaults.addAll(webMethod.getWebFaults());
        }
      }
    }

    //process the gathered web faults.
    for (WebFault webFault : allFaults) {
      model.put("fault", webFault);
      processTemplate(faultTemplate, model);
    }

    //process each type for client-side stubs.
    for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        model.put("type", typeDefinition);
        URL template = typeDefinition.isEnum() ? enumTypeTemplate : typeDefinition.isSimple() ? simpleTypeTemplate : complexTypeTemplate;
        processTemplate(template, model);

        template = typeDefinition.isEnum() ? xfireEnumTemplate : typeDefinition.isSimple() ? xfireSimpleTemplate : xfireComplexTemplate;
        processTemplate(template, model);
      }
    }

    //todo: generate the JDK 1.5 client code.
  }

  /**
   * An XFire configuration rule set.
   *
   * @return An XFire configuration rule set.
   */
  @Override
  public RuleSet getConfigurationRules() {
    return this.configurationRules;
  }

  /**
   * The client package conversions.
   *
   * @return The client package conversions.
   */
  public LinkedHashMap<String, String> getClientPackageConversions() {
    return clientPackageConversions;
  }

  /**
   * Add a client package conversion.
   *
   * @param conversion The conversion to add.
   */
  public void addClientPackageConversion(ClientPackageConversion conversion) {
    String from = conversion.getFrom();
    String to = conversion.getTo();

    if (from == null) {
      throw new IllegalArgumentException("A 'from' attribute must be specified on a clientPackageConversion element.");
    }

    if (to == null) {
      throw new IllegalArgumentException("A 'to' attribute must be specified on a clientPackageConversion element.");
    }

    this.clientPackageConversions.put(from, to);
  }

}
