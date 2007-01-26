package net.sf.enunciate.modules.docs;

import com.sun.mirror.declaration.PackageDeclaration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.main.Artifact;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedPackageDeclaration;
import net.sf.jelly.apt.freemarker.APTJellyObjectWrapper;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The documentation deployment module is responsible for generating the documentation
 * for the API.
 *
 * @author Ryan Heaton
 */
public class DocumentationDeploymentModule extends FreemarkerDeploymentModule {

  private String splashPackage;
  private String copyright;
  private String title;
  private File licenseFile;
  private URL xsltURL;
  private File base;

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
   * The title of the documentation.
   *
   * @return The title of the documentation.
   */
  public String getTitle() {
    return title;
  }

  /**
   * The title of the documentation.
   *
   * @param title The title of the documentation.
   */
  public void setTitle(String title) {
    this.title = title;
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
   * The stylesheet to use to generate the documentation.
   *
   * @param xsltURL The stylesheet to use to generate the documentation.
   */
  public void setXslt(File xsltURL) throws MalformedURLException {
    this.xsltURL = xsltURL.toURL();
  }

  /**
   * The xslt to use.
   *
   * @return The xslt to use.
   */
  public URL getXsltURL() {
    return xsltURL;
  }

  /**
   * Set the url to the xslt to use to generate the documentation.
   *
   * @param xslt The xslt.
   */
  public void setXsltURL(URL xslt) {
    this.xsltURL = xslt;
  }

    /**
   * The documentation "base".  The documentation base is the initial contents of the directory
   * where the documentation will be output.  Can be a zip file or a directory.
   *
   * @return The documentation "base".
   */
  public File getBase() {
    return base;
  }

  /**
   * The documentation "base".
   *
   * @param base The documentation "base".
   */
  public void setBase(File base) {
    this.base = base;
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

    if (this.title != null) {
      model.setVariable("title", this.title);
    }

    processTemplate(getDocsTemplateURL(), model);
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    generateClientLibXML();
    buildBase();
    doXSLT();
  }

  /**
   * Generates the client library xml indicating the available libraries.
   */
  protected void generateClientLibXML() throws IOException, EnunciateException {
    try {
      //first try to generate the client libraries file before processing the xslt.
      Class libraryArtifactClass = getClass().getClassLoader().loadClass("net.sf.enunciate.modules.xfire_client.ClientLibraryArtifact");
      List<Artifact> clientLibraries = new ArrayList<Artifact>();
      for (Artifact artifact : enunciate.getArtifacts()) {
        if (libraryArtifactClass.isAssignableFrom(artifact.getClass())) {
          clientLibraries.add(artifact);
        }
      }

      EnunciateFreemarkerModel model = getModel();
      model.put("libraries", clientLibraries);
      processTemplate(getClientLibrariesTemplateURL(), model);
    }
    catch (ClassNotFoundException e) {
      //no client-side libraries can be found, fall through...
    }
    catch (TemplateException e) {
      //there's something wrong with the template.
      throw new EnunciateException(e);
    }

  }

  /**
   * Builds the base output directory.
   */
  protected void buildBase() throws IOException {
    if (this.base == null) {
      extractBase(loadDefaultBase(), getBuildDir());
    }
    else if (this.base.isDirectory()) {
      getEnunciate().copyDir(this.base, getBuildDir());
    }
    else {
      extractBase(new FileInputStream(this.base), getBuildDir());
    }
  }

  /**
   * Do the XSLT tranformation to generate the documentation.
   */
  protected void doXSLT() throws IOException, EnunciateException {
    URL xsltURL = this.xsltURL;
    if (xsltURL == null) {
      xsltURL = DocumentationDeploymentModule.class.getResource("docs.xslt");
    }

    StreamSource source = new StreamSource(xsltURL.openStream());
    
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer(source);
      transformer.setURIResolver(new URIResolver() {
        public Source resolve(String href, String base) throws TransformerException {
          return new StreamSource(new File(getGenerateDir(), href));
        }
      });
      transformer.setParameter("client-xml-exists", new File(getGenerateDir(), "client-libraries.xml").exists());

      File docsXml = new File(getGenerateDir(), "docs.xml");
      File buildDir = getBuildDir();
      buildDir.mkdirs();
      transformer.setParameter("output-dir", buildDir.getAbsolutePath() + File.separator);
      File indexPage = new File(buildDir, "index.html");
      transformer.transform(new StreamSource(docsXml), new StreamResult(indexPage));
    }
    catch (TransformerException e) {
      throw new EnunciateException("Error during transformation of the documentation (stylesheet " + xsltURL +
        ", document " + new File(getGenerateDir(), "docs.xml") + ")", e);
    }
  }

  /**
   * Extracts the (zipped up) base to the specified directory.
   *
   * @param baseIn The stream to the base.
   * @param toDir The directory to extract to.
   */
  protected void extractBase(InputStream baseIn, File toDir) throws IOException {
    ZipInputStream in = new ZipInputStream(baseIn);
    ZipEntry entry = in.getNextEntry();
    while (entry != null) {
      File file = new File(toDir, entry.getName());
      if (entry.isDirectory()) {
        file.mkdirs();
      }
      else {
        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer = new byte[1024 * 2]; //2 kb buffer should suffice.
        int len;
        while ((len = in.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }
        out.close();
      }

      in.closeEntry();
      entry = in.getNextEntry();
    }
  }

  /**
   * Loads the default base for the documentation.
   *
   * @return The default base for the documentation.
   */
  protected InputStream loadDefaultBase() {
    return DocumentationDeploymentModule.class.getResourceAsStream("/docs.base.zip");
  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return new APTJellyObjectWrapper();
  }
  
}
