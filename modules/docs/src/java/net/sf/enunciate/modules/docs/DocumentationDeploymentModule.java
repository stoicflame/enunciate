package net.sf.enunciate.modules.docs;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.main.*;
import net.sf.enunciate.modules.FreemarkerDeploymentModule;
import net.sf.enunciate.modules.docs.config.DownloadConfig;
import net.sf.enunciate.modules.docs.config.DocsRuleSet;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedPackageDeclaration;
import net.sf.jelly.apt.freemarker.APTJellyObjectWrapper;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.digester.RuleSet;

/**
 * The documentation deployment module is responsible for generating the documentation
 * for the API.
 *
 * @author Ryan Heaton
 */
public class DocumentationDeploymentModule extends FreemarkerDeploymentModule {

  private String splashPackage;
  private String copyright;
  private String title = "Web API";
  private boolean includeDefaultDownloads = true;
  private URL xsltURL;
  private File css;
  private File base;
  private final ArrayList<DownloadConfig> downloads = new ArrayList<DownloadConfig>();

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
   * Adds a download to the documentation.
   *
   * @param download The download to add.
   */
  public void addDownload(DownloadConfig download) {
    this.downloads.add(download);
  }

  /**
   * The configured list of downloads to add to the documentation.
   *
   * @return The configured list of downloads to add to the documentation.
   */
  public Collection<DownloadConfig> getDownloads() {
    return downloads;
  }

  /**
   * Whether to include the default downloads (named artifacts) in the downloads section.
   *
   * @return Whether to include the default downloads (named artifacts) in the downloads section.
   */
  public boolean isIncludeDefaultDownloads() {
    return includeDefaultDownloads;
  }

  /**
   * Whether to include the default downloads (named artifacts) in the downloads section.
   *
   * @param includeDefaultDownloads Whether to include the default downloads (named artifacts) in the downloads section.
   */
  public void setIncludeDefaultDownloads(boolean includeDefaultDownloads) {
    this.includeDefaultDownloads = includeDefaultDownloads;
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
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @return The cascading stylesheet to use.
   */
  public File getCss() {
    return css;
  }

  /**
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @param css The cascading stylesheet to use instead of the default.
   */
  public void setCss(File css) {
    this.css = css;
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
   * The URL to the Freemarker template for processing the downloads xml file.
   *
   * @return The URL to the Freemarker template for processing the downloads xml file.
   */
  protected URL getDownloadsTemplateURL() {
    return DocumentationDeploymentModule.class.getResource("downloads.xml.fmt");
  }

  /**
   * The generate logic builds the XML documentation structure for the enunciated API.
   */
  public void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();
    if (this.splashPackage != null) {
      PackageDeclaration packageDeclaration = Context.getCurrentEnvironment().getPackage(this.splashPackage);
      if (packageDeclaration != null) {
        info("Including documentation for package %s as the splash documentation.", this.splashPackage);
        model.setVariable("apiDoc", new DecoratedPackageDeclaration(packageDeclaration).getJavaDoc());
      }
      else {
        warn("Splash package %s not found.  No splash documentation included.", this.splashPackage);
      }
    }

    if (this.copyright != null) {
      debug("Documentation copyright: %s", this.copyright);
      model.setVariable("copyright", this.copyright);
    }

    if (this.title != null) {
      debug("Documentation title: %s", this.title);
      model.setVariable("title", this.title);
    }

    processTemplate(getDocsTemplateURL(), model);
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    buildBase();
    generateDownloadsXML();
    doXSLT();

    //export the generated documentation as an artifact.
    getEnunciate().addArtifact(new FileArtifact(getName(), "docs", getBuildDir()));
  }

  /**
   * Generates the downloads xml indicating the available downloads.
   */
  protected void generateDownloadsXML() throws IOException, EnunciateException {
    EnunciateFreemarkerModel model = getModel();
    model.put("defaultDate", new Date());

    try {
      processTemplate(getDownloadsTemplateURL(), model);
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
    Enunciate enunciate = getEnunciate();
    File buildDir = getBuildDir();
    buildDir.mkdirs();
    if (this.base == null) {
      debug("Default base to be used for documentation base.");
      extractBase(loadDefaultBase(), buildDir);

      if (this.css != null) {
        enunciate.copyFile(this.css, new File(buildDir, "default.css"));
      }
    }
    else if (this.base.isDirectory()) {
      info("Directory %s to be used as the documentation base.", this.base);
      enunciate.copyDir(this.base, buildDir);
    }
    else {
      info("Zip file %s to be extracted as the documentation base.", this.base);
      extractBase(new FileInputStream(this.base), buildDir);
    }

    for (SchemaInfo schemaInfo : getModel().getNamespacesToSchemas().values()) {
      if (schemaInfo.getProperty("file") != null) {
        File from = (File) schemaInfo.getProperty("file");
        String filename = schemaInfo.getProperty("filename") != null ? (String) schemaInfo.getProperty("filename") : from.getName();
        File to = new File(getBuildDir(), filename);
        enunciate.copyFile(from, to);
      }
    }

    for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
      if (wsdlInfo.getProperty("file") != null) {
        File from = (File) wsdlInfo.getProperty("file");
        String filename = wsdlInfo.getProperty("filename") != null ? (String) wsdlInfo.getProperty("filename") : from.getName();
        File to = new File(getBuildDir(), filename);
        enunciate.copyFile(from, to);
      }
    }

    HashSet<String> explicitArtifacts = new HashSet<String>();
    TreeSet<Artifact> downloads = new TreeSet<Artifact>();
    for (DownloadConfig download : this.downloads) {
      if (download.getArtifact() != null) {
        explicitArtifacts.add(download.getArtifact());
      }
      else if (download.getFile() != null) {
        File downloadFile = download.getFile();
        info("File %s to be added as an extra download.", downloadFile);
        DownloadBundle downloadArtifact = new DownloadBundle(getName(), downloadFile.getName(), downloadFile);

        if (download.getName() != null) {
          downloadArtifact.setName(download.getName());
        }

        if (download.getDescription() != null) {
          downloadArtifact.setDescription(download.getDescription());
        }

        downloads.add(downloadArtifact);
      }
    }

    for (Artifact artifact : enunciate.getArtifacts()) {
      if (((artifact instanceof NamedArtifact) && (includeDefaultDownloads)) || (explicitArtifacts.contains(artifact.getId()))) {
        downloads.add(artifact);
        info("Artifact %s to be added as an extra download.", artifact.getId());
        explicitArtifacts.remove(artifact.getId());
      }
    }

    if (explicitArtifacts.size() > 0) {
      for (String artifactId : explicitArtifacts) {
        warn("WARNING: Unknown artifact '%s'.  Will not be available for download.", artifactId);
      }
    }

    for (Artifact download : downloads) {
      info("Exporting %s to directory %s.", download.getId(), buildDir);
      download.exportTo(buildDir, enunciate);
    }

    EnunciateFreemarkerModel model = getModel();
    model.put("downloads", downloads);
  }

  /**
   * Do the XSLT tranformation to generate the documentation.
   */
  protected void doXSLT() throws IOException, EnunciateException {
    info("Executing documentation stylesheet transformation.");
    URL xsltURL = this.xsltURL;
    if (xsltURL == null) {
      xsltURL = DocumentationDeploymentModule.class.getResource("docs.xslt");
    }

    debug("Using stylesheet %s", xsltURL);
    StreamSource source = new StreamSource(xsltURL.openStream());

    try {
      Transformer transformer = new TransformerFactoryImpl().newTransformer(source);
      transformer.setURIResolver(new URIResolver() {
        public Source resolve(String href, String base) throws TransformerException {
          return new StreamSource(new File(getGenerateDir(), href));
        }
      });
      transformer.setParameter("downloads-exists", new File(getGenerateDir(), "downloads.xml").exists());
      debug("Extra downloads exist: %b", transformer.getParameter("downloads-exists"));

      File docsXml = new File(getGenerateDir(), "docs.xml");
      File buildDir = getBuildDir();
      buildDir.mkdirs();
      transformer.setParameter("output-dir", buildDir.getAbsolutePath() + File.separator);
      File indexPage = new File(buildDir, "index.html");
      debug("Transforming %s to %s.", docsXml, indexPage);
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
      debug("Extracting %s to %s.", entry.getName(), file);
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

  @Override
  public RuleSet getConfigurationRules() {
    return new DocsRuleSet();
  }
}
