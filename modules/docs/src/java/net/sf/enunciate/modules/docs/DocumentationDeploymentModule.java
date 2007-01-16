package net.sf.enunciate.modules.docs;

import com.sun.mirror.declaration.PackageDeclaration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.main.Artifact;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedPackageDeclaration;
import net.sf.jelly.apt.freemarker.APTJellyObjectWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * The documentation deployment module is responsible for generating the documentation
 * for the API.
 *
 * @author Ryan Heaton
 */
public class DocumentationDeploymentModule extends FreemarkerDeploymentModule {

  private String splashPackage;
  private String copyright;
  private File licenseFile;

  /**
   * @return "docs"
   */
  @Override
  public String getName() {
    return "docs";
  }

  /**
   * @return 100
   */
  @Override
  public int getOrder() {
    return 100;
  }

  /**
   * The package that contains the splash page documentation for the API.
   *
   * @return The package that contains the splash page documentation for the API.
   */
  public String getSplashPackage() {
    return splashPackage;
  }

  /**
   * The package that contains the splash page documentation for the API.
   *
   * @param splashPackage The package that contains the splash page documentation for the API.
   */
  public void setSplashPackage(String splashPackage) {
    this.splashPackage = splashPackage;
  }

  /**
   * The copyright (posted on the website).
   *
   * @return The copyright (posted on the website).
   */
  public String getCopyright() {
    return copyright;
  }

  /**
   * The copyright (posted on the website).
   *
   * @param copyRight The copyright (posted on the website).
   */
  public void setCopyright(String copyRight) {
    this.copyright = copyRight;
  }

  /**
   * The license file.
   *
   * @return The license file.
   */
  public File getLicenseFile() {
    return licenseFile;
  }

  /**
   * The license file.
   *
   * @param licenseFile The license file.
   */
  public void setLicenseFile(File licenseFile) {
    this.licenseFile = licenseFile;
  }

  /**
   * The URL to the Freemarker template for processing the base documentation xml file.
   *
   * @return The URL to the Freemarker template for processing the base documentation xml file.
   */
  protected URL getDocsTemplateURL() {
    return DocumentationDeploymentModule.class.getResource("docs.xml.fmt");
  }

  /**
   * The URL to the Freemarker template for processing the client libraries xml file.
   *
   * @return The URL to the Freemarker template for processing the client libraries xml file.
   */
  protected URL getClientLibrariesTemplateURL() {
    return DocumentationDeploymentModule.class.getResource("client-libraries.xml.fmt");
  }

  /**
   * The generate logic builds the XML documentation structure for the enunciated API.
   */
  public void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();
    if (this.splashPackage != null) {
      PackageDeclaration packageDeclaration = Context.getCurrentEnvironment().getPackage(this.splashPackage);
      model.setVariable("apiDoc", new DecoratedPackageDeclaration(packageDeclaration).getJavaDoc());
    }

    if (this.copyright != null) {
      model.setVariable("copyright", this.copyright);
    }

    if (this.licenseFile != null) {
      model.setVariable("licenseFile", this.licenseFile.getName());
    }

    processTemplate(getDocsTemplateURL(), model);
    File docsXml = new File(getGenerateDir(), "docs.xml");

    try {
      Class libraryArtifactClass = getClass().getClassLoader().loadClass("net.sf.enunciate.modules.xfire_client.ClientLibraryArtifact");
      List<Artifact> clientLibraries = new ArrayList<Artifact>();
      for (Artifact artifact : enunciate.getArtifacts()) {
        if (libraryArtifactClass.isAssignableFrom(artifact.getClass())) {
          clientLibraries.add(artifact);
        }
      }

      model.setVariable("libraries", clientLibraries);
      processTemplate(getClientLibrariesTemplateURL(), model);
    }
    catch (ClassNotFoundException e) {
      //fall through... no client-side libraries can be found...
    }

  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return new APTJellyObjectWrapper();
  }
}
