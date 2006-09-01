package net.sf.enunciate.modules.xfire_client;

import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xfire_client.config.ClientPackageConversion;
import net.sf.enunciate.modules.xfire_client.config.XFireRuleSet;
import org.apache.commons.digester.RuleSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * Deployment module for XFire.
 *
 * @author Ryan Heaton
 */
public class XFireClientDeploymentModule extends FreemarkerDeploymentModule {

  private final LinkedHashMap<String, String> clientPackageConversions;
  private final XFireRuleSet configurationRules;

  public XFireClientDeploymentModule() {
    this.clientPackageConversions = new LinkedHashMap<String, String>();
    this.configurationRules = new XFireRuleSet();
  }

  /**
   * @return The URL to "xfire-clients.fmt"
   */
  protected URL getClientTemplateURL() {
    return XFireClientDeploymentModule.class.getResource("xfire-clients.fmt");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the JDK 1.3 client code.
    LinkedHashMap<String, String> conversions = getClientPackageConversions();
    model.put("clientPackageFor", new ClientPackageForMethod(conversions));
    model.put("clientClassnameFor", new ClientClassnameForMethod(conversions));

    processTemplate(getClientTemplateURL(), model);

    //todo: generate the JDK 1.5 client code.
  }

  @Override
  protected void doBuild() throws IOException {
    Enunciate enunciate = getEnunciate();
    File webinf = enunciate.getWebInf();
    File xfireConfigDir = new File(new File(enunciate.getPreprocessDir(), "xfire"), "xml");

    //copy the web.xml file to WEB-INF.
    enunciate.copyResource("/net/sf/enunciate/modules/xfire/web.xml", new File(webinf, "web.xml"));

    //copy the xfire config file from the xfire configuration directory to the WEB-INF directory.
    enunciate.copyFile(new File(xfireConfigDir, "xfire-servlet.xml"), new File(webinf, "xfire-servlet.xml"));
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
