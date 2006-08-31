package net.sf.enunciate.modules.xfire;

import freemarker.template.TemplateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.xml.XMLAPILookup;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Deployment module for XFire.
 *
 * @author Ryan Heaton
 */
public class XFireDeploymentModule extends FreemarkerDeploymentModule {

  /**
   * @return The URL to "xfire-servlet.fmt"
   */
  protected URL getXFireServletTemplateURL() {
    return XFireDeploymentModule.class.getResource("xfire-servlet.fmt");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //generate the xfire-servlet.xml
    processTemplate(getXFireServletTemplateURL(), model);
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

    File classes = new File(webinf, "classes");
    File xmlDir = (File) enunciate.getProperty("xml.dir");
    if (xmlDir != null) {
      //if the xml deployment module has been run, copy all generated xml files to the WEB-INF/classes directory.
      enunciate.copyDir(xmlDir, classes);
    }

    XMLAPILookup lookup = (XMLAPILookup) enunciate.getProperty(XMLAPILookup.class.getName());
    if (lookup != null) {
      //store the lookup, if it exists.
      FileOutputStream out = new FileOutputStream(new File(classes, "xml-api.lookup"));
      lookup.store(out);
      out.close();
    }
    else {
      System.err.println("ERROR: No lookup was generated!  The contoller used to serve up the WSDLs and schemas will not function!");
    }
  }

  /**
   * @return 10
   */
  @Override
  public int getOrder() {
    return 10;
  }

}
