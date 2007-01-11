package net.sf.enunciate.modules.docs;

import freemarker.template.TemplateException;
import freemarker.template.ObjectWrapper;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.jelly.apt.freemarker.APTJellyObjectWrapper;

import java.io.IOException;
import java.io.File;
import java.net.URL;

/**
 * The documentation deployment module is responsible for generating the documentation
 * for the API.
 *
 * @author Ryan Heaton
 */
public class DocumentationDeploymentModule extends FreemarkerDeploymentModule {

  /**
   * @return "docs"
   */
  @Override
  public String getName() {
    return "docs";
  }

  /**
   * @return 1
   */
  @Override
  public int getOrder() {
    return 1;
  }

  /**
   * The URL to the Freemarker template for processing the base documentation xml file.
   *
   * @return The URL to the Freemarker template for processing the base documentation xml file.
   */
  protected URL getTemplateURL() {
    return DocumentationDeploymentModule.class.getResource("docs.xml.fmt");
  }

  /**
   * The generate logic builds the XML documentation structure for the enunciated API.
   */
  public void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException {
    processTemplate(getTemplateURL(), getModel());
    File docsXml = new File(getGenerateDir(), "docs.xml");
  }

  /**
   * The generate dir for the documentation.
   *
   * @return The generate dir for the documentation.
   */
  protected File getGenerateDir() {
    return new File(getEnunciate().getGenerateDir(), "docs");
  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return new APTJellyObjectWrapper();
  }
}
