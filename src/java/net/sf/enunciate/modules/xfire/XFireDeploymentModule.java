package net.sf.enunciate.modules.xfire;

import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Deployment module for XFire.
 *
 * @author Ryan Heaton
 */
public class XFireDeploymentModule extends FreemarkerDeploymentModule {

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

}
