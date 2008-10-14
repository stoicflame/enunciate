/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.docs;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedPackageDeclaration;
import net.sf.jelly.apt.util.JavaDocTagHandler;
import net.sf.jelly.apt.util.JavaDocTagHandlerFactory;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.main.Artifact;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.main.NamedArtifact;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.docs.config.DocsRuleSet;
import org.codehaus.enunciate.modules.docs.config.DownloadConfig;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * <h1>Documentation Module</h1>
 *
 * <p>The documentation deployment module is responsible for generating the documentation
 * for the API.  This includes both the HTML pages and any other static content put at the
 * root of the web application.</p>
 *
 * <p>The order of the documentation module is 100, essentially putting it after the generation
 * of any static documents (e.g. WSDL, schemas) or static downloads (e.g. client libraries), but
 * before the assembly of the war (see the spring-app module, order 200).</p>
 *
 * <ul>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <p>The only significant steps in the documentation module are the "generate" step and the "build"
 * step.</p>
 *
 * <h3>generate</h3>
 *
 * <p>During the <b>generate</b> step, the documentation deployment module generates an XML file that
 * conforms to <a href="doc-files/docs.xsd">this schema</a>, containing all the documentation for the
 * entire API in a strcutured form.</p>
 *
 * <h3>build</h3>
 *
 * <p>The build step is where all the documentation files are generated as needed and assembled into
 * a single directory.  The first step is to copy the static files (the "base") to the build directory.
 * The documentation base can be specified in the <a href="#config">configuration</a>, or you can
 * use the default base.</p>
 *
 * <p>Then, the documentation deployment module generates another XML file conforming to
 * <a href="doc-files/downloads.xsd">this schema</a> that contains the information
 * that is available for the downloads page in a structured form.  By default, all named artifacts
 * are included as a download (see the architecture guide for details).  Other downloads can also
 * be specified in the <a href="#config">configuration</a>.</p>
 *
 * <p>Finally, an <a href="http://www.w3.org/TR/xslt">XML Stylesheet Transformation</a> is applied to the
 * generated XML file in order to generate the HTML pages.  You can specify your own XSLT, or you can
 * just use <a href="doc-files/docs.xslt">the default</a>.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The configuration for the documentation deployment module is specified by the "docs" element
 * under the "modules" element in the Enunciate configuration file.</p>
 *
 * <h3>attributes</h3>
 *
 * <ul>
 *   <li>The "<b>splashPackage</b>" attribute specifies the package that contains the documentation
 *       to use as the introduction to the API documentation.  By default, no text is used for the
 * introduction.</li>
 *   <li>The "<b>copyright</b>" attribute specifies the text for the copyright holder on the web
 * page. By default, there is no copyright information displayed on the webpage.</li>
 *   <li>The "<b>title</b>" specifies the title of the generated HTML pages.  By default, the title is "Web API".</li>
 *   <li>The "<b>includeDefaultDownloads</b>" is a boolean attribute specifying whether the default downloads should
 * be included.  The default is "true".</li>
 *   <li>The "<b>css</b>" attribute is used to specify the file to be used as the cascading stylesheet for the HTML.  If one isn't supplied, a
 *  default will be provided.</p>
 *   <li>The "<b>xslt</b>" attribute specifies the file that is the XML Stylesheet Transform that will be applied to the
 * documentation XML to generate the HTML docs.  If no XSLT is specified, a default one will be used.</p>
 *   <li>The "<b>xsltURL</b>" attribute specifies the URL to the XML Stylesheet Transform that will be applied to the
 * documentation XML to generate the HTML docs.  If no XSLT is specified, a default one will be used.</p>
 *   <li>The "<b>base</b>" attribute specifies a gzipped file or a directory to use as the documentation base.  If none is supplied, a default
 * base will be provided.
 *   <li>The "javadocTagHandling" attribute is used to specify the handling of JavaDoc tags. It must be either "OFF" or the FQN of an instance of
 *       <tt>net.sf.jelly.apt.util.JavaDocTagHandler</tt></li>
 * </ul>
 *
 * <h3>The "download" element</h3>
 *
 * <p>There can be any number of "download" elements specified.  This element is used to indicate another file or Enunciate artifact
 * that is to be included in the "downloads" page.  The download element supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "name" attribute specifies a name for the download.</li>
 *   <li>The "artifact" attribute specifies the id of an Enunciate artifact that is to be included as a download.</li>
 *   <li>The "file" attribute specifies a file on the filesystem that is to be included as a download. This attribute is
 *       ignored if the "artifact" attribute is set.</li>
 *   <li>The "description" attribute includes a description of the download.  This attribute is ignored if
 *       the "artifact" attribute is set.</li>
 * </ul>
 *
 * <h3>The "war" element</h3>
 *
 * <p>The "war" element under the "docs" element is used to configure the webapp that will host the documenation. It supports
 * the following attributes:</p>
 *
 * <ul>
 * <li>The "docsDir" attribute is the directory in the war to which the documentation will be put.  The default is the root of the war.</li>
 * </ul>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <h3>docs</h3>
 *
 * <p>The documentation deployment module exports only one artifact: the build directory for the documentation.
 * The artifact id is "docs", and it is exported during the "build" step.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_docs.html
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
  private String docsDir = null;
  private String javadocTagHandling;

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
   * JavaDoc tag handing.
   *
   * @return The javadoc tag handling.
   */
  public String getJavadocTagHandling() {
    return javadocTagHandling;
  }

  /**
   * The javadoc tag handling.
   *
   * @param javadocTagHandling The javadoc tag handling.
   */
  public void setJavadocTagHandling(String javadocTagHandling) {
    this.javadocTagHandling = javadocTagHandling;
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
   * The subdirectory in the web application where the documentation will be put.
   *
   * @return The subdirectory in the web application where the documentation will be put.
   */
  public String getDocsDir() {
    return docsDir;
  }

  /**
   * The subdirectory in the web application where the documentation will be put.
   *
   * @param docsDir The subdirectory in the web application where the documentation will be put.
   */
  public void setDocsDir(String docsDir) {
    this.docsDir = docsDir;
  }

  /**
   * The directory into which the documentation is put.
   *
   * @return The directory into which the documentation is put.
   */
  public File getDocsBuildDir() {
    File docsDir = getBuildDir();
    if (getDocsDir() != null) {
      docsDir = new File(docsDir, getDocsDir());
    }

    return docsDir;
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    //some application components might want to reference their documentation, so we'll put a reference to the configured docs dir.
    enunciate.setProperty("docs.webapp.dir", getDocsDir());
    if (getJavadocTagHandling() == null) {
      JavaDocTagHandlerFactory.setTagHandler(new DocumentationJavaDocTagHandler());
    }
    else if (!"OFF".equalsIgnoreCase(getJavadocTagHandling())) {
      try {
        JavaDocTagHandlerFactory.setTagHandler((JavaDocTagHandler) Class.forName(getJavadocTagHandling()).newInstance());
      }
      catch (Throwable e) {
        throw new EnunciateException(e);
      }
    }
  }

  /**
   * The generate logic builds the XML documentation structure for the enunciated API.
   */
  public void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException {
    if (!getEnunciate().isUpToDateWithSources(getGenerateDir())) {
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

      model.setVariable("uniqueContentTypes", new UniqueContentTypesMethod());
      processTemplate(getDocsTemplateURL(), model);
    }
    else {
      info("Skipping documentation source generation as everything appears up-to-date...");
    }
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    if (!getEnunciate().isUpToDateWithSources(getBuildDir())) {
      buildBase();
      generateDownloadsXML();
      doXSLT();
    }
    else {
      info("Skipping build of documentation as everything appears up-to-date...");
    }

    //export the generated documentation as an artifact.
    getEnunciate().addArtifact(new FileArtifact(getName(), "docs", getDocsBuildDir()));

    //add the webapp fragment...
    BaseWebAppFragment webAppFragment = new BaseWebAppFragment(getName());
    webAppFragment.setBaseDir(getBuildDir());
    TreeMap<String, String> mimeMappings = new TreeMap<String, String>();
    mimeMappings.put("wsdl", "text/xml");
    mimeMappings.put("xsd", "text/xml");
    webAppFragment.setMimeMappings(mimeMappings);
    getEnunciate().addWebAppFragment(webAppFragment);
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
    File buildDir = getDocsBuildDir();
    buildDir.mkdirs();
    if (this.base == null) {
      debug("Default base to be used for documentation base.");
      enunciate.extractBase(loadDefaultBase(), buildDir);

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
      enunciate.extractBase(new FileInputStream(this.base), buildDir);
    }

    for (SchemaInfo schemaInfo : getModel().getNamespacesToSchemas().values()) {
      if (schemaInfo.getProperty("file") != null) {
        File from = (File) schemaInfo.getProperty("file");
        String filename = schemaInfo.getProperty("filename") != null ? (String) schemaInfo.getProperty("filename") : from.getName();
        File to = new File(getDocsBuildDir(), filename);
        enunciate.copyFile(from, to);
      }
    }

    for (WsdlInfo wsdlInfo : getModel().getNamespacesToWSDLs().values()) {
      if (wsdlInfo.getProperty("file") != null) {
        File from = (File) wsdlInfo.getProperty("file");
        String filename = wsdlInfo.getProperty("filename") != null ? (String) wsdlInfo.getProperty("filename") : from.getName();
        File to = new File(getDocsBuildDir(), filename);
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
        File downloadFile = enunciate.resolvePath(download.getFile());

        info("File %s to be added as an extra download.", downloadFile.getAbsolutePath());
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
        if (artifact.isPublic()) {
          downloads.add(artifact);
        }

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
      xsltURL = DocumentationDeploymentModule.class.getResource("doc-files/docs.xslt");
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
      File buildDir = getDocsBuildDir();
      buildDir.mkdirs();
      transformer.setParameter("output-dir", buildDir.getAbsolutePath() + File.separator);
      transformer.setParameter("api-relative-path", getRelativePathToRootDir());
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
   * Get the relative path to the root directory from the docs directory.
   *
   * @return the relative path to the root directory.
   */
  protected String getRelativePathToRootDir() {
    String relativePath = ".";
    String docsDir = getDocsDir();
    if (docsDir != null) {
      StringBuilder builder = new StringBuilder();
      StringTokenizer pathTokens = new StringTokenizer(docsDir.replace(File.separatorChar, '/'), "/");
      if (pathTokens.hasMoreTokens()) {
        while (pathTokens.hasMoreTokens()) {
          builder.append("..");
          pathTokens.nextToken();
          if (pathTokens.hasMoreTokens()) {
            builder.append('/');
          }
        }
      }
      else {
        builder.append('.');
      }
      relativePath = builder.toString();
    }
    return relativePath;
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
    return new DocumentationObjectWrapper();
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new DocsRuleSet();
  }
}
