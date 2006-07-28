package net.sf.enunciate.modules.xml;

import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Deployment module for the XML schemas and WSDL.
 *
 * @author Ryan Heaton
 */
public class XMLDeploymentModule extends FreemarkerDeploymentModule {

  protected URL getTemplateURL() {
    return XMLDeploymentModule.class.getResource("xml.fmt");
  }

  @Override
  protected void doBuild() throws IOException {
    Enunciate enunciate = getEnunciate();
    File classes = new File(enunciate.getWebInf(), "classes");
    File xmlDir = new File(enunciate.getPreprocessDir(), "xml");

    //copy all generated xml files to the WEB-INF/classes directory.
    enunciate.copyDir(xmlDir, classes);
  }
}
