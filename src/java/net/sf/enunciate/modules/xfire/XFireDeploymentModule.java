package net.sf.enunciate.modules.xfire;

import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xfire.config.ClientPackageConversion;
import net.sf.enunciate.modules.xfire.config.XFireRuleSet;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
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
public class XFireDeploymentModule extends FreemarkerDeploymentModule {

  private final LinkedHashMap<String, String> clientPackageConversions;
  private final XFireRuleSet configurationRules;

  public XFireDeploymentModule() {
    this.clientPackageConversions = new LinkedHashMap<String, String>();
    this.configurationRules = new XFireRuleSet();
  }

  @Override
  protected FreemarkerModel getModel() throws IOException {
    FreemarkerModel model = super.getModel();
    model.put("clientPackageFor", new ClientPackageForMethod(getClientPackageConversions()));
    model.put("clientClassnameFor", new ClientClassnameForMethod(getClientPackageConversions()));
    return model;
  }

  /**
   * @return The URL to "xfire.fmt"
   */
  protected URL getTemplateURL() {
    return XFireDeploymentModule.class.getResource("xfire.fmt");
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
