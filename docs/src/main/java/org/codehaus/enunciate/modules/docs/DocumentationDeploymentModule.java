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

import com.webcohesion.enunciate.Enunciate;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.Download;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedPackageElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandlerFactory;
import com.webcohesion.enunciate.module.ApiRegistryAwareModule;
import com.webcohesion.enunciate.module.BasicGeneratingModule;
import freemarker.ext.dom.NodeModel;
import freemarker.template.TemplateException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.lang.model.element.PackageElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.*;


public class DocumentationDeploymentModule extends BasicGeneratingModule implements ApiRegistryAwareModule {

  private String defaultTitle;
  private Set<String> facetIncludes = new TreeSet<String>();
  private Set<String> facetExcludes = new TreeSet<String>(Arrays.asList("org.codehaus.enunciate.doc.ExcludeFromDocumentation"));
  private File defaultDocsDir;
  private ApiRegistry apiRegistry;
  private File swaggerOutputDir;
  private File wadlFile;

  /**
   * @return "docs"
   */
  @Override
  public String getName() {
    return "docs";
  }

  /**
   * The package that contains the splash page documentation for the API.
   *
   * @return The package that contains the splash page documentation for the API.
   */
  public String getSplashPackage() {
    return this.config.getString("[@splashPackage]");
  }

  /**
   * The copyright (posted on the website).
   *
   * @return The copyright (posted on the website).
   */
  public String getCopyright() {
    return this.config.getString("[@copyright]");
  }

  /**
   * The title of the documentation.
   *
   * @return The title of the documentation.
   */
  public String getTitle() {
    return this.config.getString("[@title]", this.defaultTitle);
  }

  /**
   * The default title of the documentation.
   *
   * @param title The default title of the documentation.
   */
  public void setDefaultTitle(String title) {
    this.defaultTitle = title;
  }

  /**
   * The configured list of downloads to add to the documentation.
   *
   * @return The configured list of downloads to add to the documentation.
   */
  public Collection<DownloadConfig> getDownloads() {
    List<HierarchicalConfiguration> downloads = this.config.configurationsAt("download");
    ArrayList<DownloadConfig> downloadConfigs = new ArrayList<DownloadConfig>(downloads.size());
    for (HierarchicalConfiguration download : downloads) {
      DownloadConfig downloadConfig = new DownloadConfig();
      downloadConfig.setArtifact(download.getString("artifact"));
      downloadConfig.setDescription(download.getString("description"));
      downloadConfig.setFile(download.getString("file"));
      downloadConfig.setName(download.getString("name"));
      downloadConfig.setShowLink(download.getString("showLink"));
      downloadConfigs.add(downloadConfig);
    }
    return downloadConfigs;
  }

  /**
   * The additional css files.
   *
   * @return The additional css files.
   */
  public List<String> getAdditionalCss() {
    LinkedList<String> additionalCss = new LinkedList<String>();
    List<HierarchicalConfiguration> additionalCsses = this.config.configurationsAt("additional-css");
    for (HierarchicalConfiguration additional : additionalCsses) {
      String file = additional.getString("[@file]");
      if (file != null) {
        additionalCss.add(file);
      }
    }
    return additionalCss;
  }

  /**
   * The url to the freemarker XML processing template that will be used to transforms the docs.xml to the site documentation. For more
   * information, see http://freemarker.sourceforge.net/docs/xgui.html
   *
   * @return The url to the freemarker XML processing template.
   */
  public File getFreemarkerTemplateFile() {
    String templatePath = this.config.getString("[@freemarkerTemplate]");
    return templatePath == null ? null : resolveFile(templatePath);
  }

  /**
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @return The cascading stylesheet to use.
   */
  public String getCss() {
    return this.config.getString("[@css]", "css/style.css");
  }

  /**
   * The documentation "base".  The documentation base is the initial contents of the directory
   * where the documentation will be output.  Can be a zip file or a directory.
   *
   * @return The documentation "base".
   */
  public File getBase() {
    String base = this.config.getString("[@base]");
    return base == null ? null : resolveFile(base);
  }

  /**
   * The subdirectory in the web application where the documentation will be put.
   *
   * @return The subdirectory in the web application where the documentation will be put.
   */
  public File getDocsDir() {
    String docsDir = this.config.getString("[@docsDir]");
    return docsDir == null ? this.defaultDocsDir : resolveFile(docsDir);
  }

  public void setDefaultDocsDir(File docsDir) {
    this.defaultDocsDir = docsDir;
  }

  /**
   * The name of the index page.
   *
   * @return The name of the index page.
   */
  public String getIndexPageName() {
    return this.config.getString("[@indexPageName]");
  }

  /**
   * Whether to disable the REST mountpoint documentation.
   *
   * @return Whether to disable the REST mountpoint documentation.
   */
  public boolean isDisableRestMountpoint() {
    return this.config.getBoolean("[@disableRestMountpoint]");
  }

  /**
   * The default namespace for the purposes of generating documentation.
   *
   * @return The default namespace for the purposes of generating documentation.
   */
  public String getDefaultNamespace() {
    return this.config.getString("[@defaultNamespace]");
  }

  /**
   * How to group the REST resources together.
   *
   * @return How to group the REST resources together.
   */
  public String getGroupingFacet() {
    //todo: move this into the jax-rs module
    return this.config.getString("[@groupingFacet]", "org.codehaus.enunciate.contract.jaxrs.Resource");
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  public void setSwaggerOutputDir(File swaggerOutputDir) {
    this.swaggerOutputDir = swaggerOutputDir;
  }

  public void setWadlFile(File wadlFile) {
    this.wadlFile = wadlFile;
  }

  @Override
  public void call(EnunciateContext context) {
    File docsDir = getDocsDir();
    if (!isUpToDateWithSources(docsDir)) {
      JavaDocTagHandlerFactory.setTagHandler(new DocumentationJavaDocTagHandler());

      Map<String, Object> model = new HashMap<String, Object>();

      String splashPackage = getSplashPackage();
      if (splashPackage != null) {
        PackageElement packageDeclaration = context.getProcessingEnvironment().getElementUtils().getPackageElement(splashPackage);
        if (packageDeclaration != null) {
          debug("Including documentation for package %s as the splash documentation.", splashPackage);
          model.put("apiDoc", ((DecoratedPackageElement)packageDeclaration).getJavaDoc().toString());
        }
        else {
          warn("Splash package %s not found.  No splash documentation included.", splashPackage);
        }
      }

      String copyright = getCopyright();
      if (copyright != null) {
        model.put("copyright", copyright);
      }

      String title = getTitle();
      model.put("title", title == null ? "Web API" : title);

      model.put("cssFile", getCss());

      model.put("file", new FileDirective(docsDir));

      List<? extends ResourceGroup> resourceGroups = this.apiRegistry.getResourceGroups();
      //todo: filter by facet
      model.put("resourceGroups", resourceGroups);

      List<? extends ServiceGroup> serviceGroups = this.apiRegistry.getServiceGroups();
      //todo: filter by facet
      model.put("serviceGroups", serviceGroups);

      List<Syntax> data = this.apiRegistry.getSyntaxes();
      //todo: filter by facet
      model.put("data", data);

      List<? extends Download> downloads = new ArrayList<Download>();
      //todo: add downloads
      model.put("downloads", downloads);

      model.put("indexPageName", getIndexPageName());

      model.put("apiRelativePath", getRelativePathToRootDir());

      model.put("disableMountpoint", isDisableRestMountpoint());

      model.put("additionalCssFiles", getAdditionalCss());

      if (this.swaggerOutputDir != null) {
        model.put("swaggerDir", "swagger");
      }

      if (this.wadlFile != null) {
        model.put("wadl", this.wadlFile);
      }

      //todo: iterate through wsdls and make sure the wsdl path is set for each service group

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
      doXmlTransform();
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
    ArrayList<WebAppComponent> filters = new ArrayList<WebAppComponent>();
    if (isApplyWsdlFilter() && !getModelInternal().getNamespacesToWSDLs().isEmpty()) {
      WebAppComponent wsdlFilter = new WebAppComponent();
      wsdlFilter.setName("wsdl-filter");
      wsdlFilter.setClassname("org.codehaus.enunciate.webapp.IDLFilter");
      HashMap<String, String> initParams = new HashMap<String, String>();
      initParams.put("assumed-base-address", getModel().getBaseDeploymentAddress());
      initParams.put("match-prefix", ":address location=\"");
      wsdlFilter.setInitParams(initParams);
      TreeSet<String> wsdls = new TreeSet<String>();

      for (WsdlInfo wsdlInfo : getModelInternal().getNamespacesToWSDLs().values()) {
        String wsdlLocation = (String) wsdlInfo.getProperty("redirectLocation");
        if (wsdlLocation != null) {
          wsdls.add(wsdlLocation);
        }
      }
      wsdlFilter.setUrlMappings(wsdls);
      filters.add(wsdlFilter);
    }

    if (isApplyWadlFilter() && getModelInternal().getWadlFile() != null) {
      WebAppComponent wadlFilter = new WebAppComponent();
      wadlFilter.setName("wadl-filter");
      wadlFilter.setClassname("org.codehaus.enunciate.webapp.IDLFilter");
      HashMap<String, String> initParams = new HashMap<String, String>();
      initParams.put("assumed-base-address", getModel().getBaseDeploymentAddress());
      initParams.put("match-prefix", ":resources base=\"");
      wadlFilter.setInitParams(initParams);
      TreeSet<String> wadls = new TreeSet<String>();
      String docsDir = getDocsDir() == null ? "" : getDocsDir();
      if (!docsDir.startsWith("/")) {
        docsDir = "/" + docsDir;
      }
      while (docsDir.endsWith("/")) {
        docsDir = docsDir.substring(0, docsDir.length() - 1);
      }

      wadls.add(docsDir + "/" + getModelInternal().getWadlFile().getName());
      wadlFilter.setUrlMappings(wadls);
      filters.add(wadlFilter);
    }

    webAppFragment.setFilters(filters);
    getEnunciate().addWebAppFragment(webAppFragment);

  }

  /**
   * Builds the base output directory.
   */
  protected void buildBase() throws IOException {
    Enunciate enunciate = getEnunciate();
    File buildDir = getDocsBuildDir();
    buildDir.mkdirs();
    if (this.base == null) {
      InputStream discoveredBase = DocumentationDeploymentModule.class.getResourceAsStream("/META-INF/enunciate/docs-base.zip");
      if (discoveredBase == null) {
        debug("Default base to be used for documentation base.");
        enunciate.extractBase(loadDefaultBase(), buildDir);

        URL discoveredCss = DocumentationDeploymentModule.class.getResource("/META-INF/enunciate/css/style.css");
        if (discoveredCss != null) {
          enunciate.copyResource(discoveredCss, new File(new File(buildDir, "css"), "style.css"));
        }
        else if (this.css != null) {
          enunciate.copyFile(enunciate.resolvePath(this.css), new File(new File(buildDir, "css"), "style.css"));
        }
      }
      else {
        debug("Discovered documentation base at /META-INF/enunciate/docs-base.zip");
        enunciate.extractBase(discoveredBase, buildDir);
      }
    }
    else {
      File baseFile = enunciate.resolvePath(this.base);
      if (baseFile.isDirectory()) {
        debug("Directory %s to be used as the documentation base.", baseFile);
        enunciate.copyDir(baseFile, buildDir);
      }
      else {
        debug("Zip file %s to be extracted as the documentation base.", baseFile);
        enunciate.extractBase(new FileInputStream(baseFile), buildDir);
      }
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

    File wadlFile = getModelInternal().getWadlFile();
    if (wadlFile != null) {
      enunciate.copyFile(wadlFile, new File(getDocsBuildDir(), wadlFile.getName()));
    }

    HashSet<String> explicitArtifacts = new HashSet<String>();
    TreeSet<Artifact> downloads = new TreeSet<Artifact>();
    for (DownloadConfig download : this.downloads) {
      if (download.getArtifact() != null) {
        explicitArtifacts.add(download.getArtifact());
      }
      else if (download.getFile() != null) {
        File downloadFile = enunciate.resolvePath(download.getFile());

        debug("File %s to be added as an extra download.", downloadFile.getAbsolutePath());
        DownloadBundle downloadArtifact = new DownloadBundle(getName(), downloadFile.getName(), downloadFile);

        if (download.getName() != null) {
          downloadArtifact.setName(download.getName());
        }

        if (download.getDescription() != null) {
          downloadArtifact.setDescription(download.getDescription());
        }

        if(download.getShowLink().equals("false")){
          debug("Exporting %s to directory %s.", downloadArtifact.getId(), buildDir);
          downloadArtifact.exportTo(buildDir, enunciate);
        } else {
          downloads.add(downloadArtifact);
        }

      }
    }

    for (Artifact artifact : enunciate.getArtifacts()) {
      if (((artifact instanceof NamedArtifact) && (includeDefaultDownloads)) || (explicitArtifacts.contains(artifact.getId()))) {
        if (artifact.isPublic()) {
          downloads.add(artifact);
        }

        debug("Artifact %s to be added as an extra download.", artifact.getId());
        explicitArtifacts.remove(artifact.getId());
      }
    }

    if (explicitArtifacts.size() > 0) {
      for (String artifactId : explicitArtifacts) {
        warn("WARNING: Unknown artifact '%s'.  Will not be available for download.", artifactId);
      }
    }

    for (Artifact download : downloads) {
      debug("Exporting %s to directory %s.", download.getId(), buildDir);
      download.exportTo(buildDir, enunciate);      
    }

    Set<String> additionalCssFiles = new HashSet<String>();
    for (String additionalCss : getAdditionalCss()) {
      File additionalCssFile = enunciate.resolvePath(additionalCss);
      debug("File %s to be added as an additional css file.", additionalCss);
      enunciate.copyFile(additionalCssFile, new File(buildDir, additionalCssFile.getName()));
      additionalCssFiles.add(additionalCssFile.getName());
    }

    EnunciateFreemarkerModel model = getModel();
    model.put("downloads", downloads);
    model.put("additionalCssFiles", additionalCssFiles);
  }

  /**
   * Do the XSLT tranformation to generate the documentation.
   */
  protected void doXmlTransform() throws IOException, EnunciateException {
    debug("Executing XML transformation.");
    URL xsltURL = this.xsltURL;
    if (xsltURL == null) {
      if (this.xslt != null) {
        xsltURL = getEnunciate().resolvePath(this.xslt).toURI().toURL();
      }
      else {
        xsltURL = DocumentationDeploymentModule.class.getResource("/META-INF/enunciate/docs.xslt");
      }
    }

    URL freemarkerXMLProcessingTemplateURL = this.freemarkerXMLProcessingTemplateURL;
    if (freemarkerXMLProcessingTemplateURL == null) {
      if (this.freemarkerXMLProcessingTemplate != null) {
        freemarkerXMLProcessingTemplateURL = getEnunciate().resolvePath(this.freemarkerXMLProcessingTemplate).toURI().toURL();
      }
      else {
        freemarkerXMLProcessingTemplateURL = DocumentationDeploymentModule.class.getResource("/META-INF/enunciate/docs.fmt");
      }
    }

    if (xsltURL == null && freemarkerXMLProcessingTemplateURL == null) {
      freemarkerXMLProcessingTemplateURL = DocumentationDeploymentModule.class.getResource("docs.fmt");
    }

    if (xsltURL != null) {
      doXSLT(xsltURL);
    }
    else {
      doFreemarkerXMLProcessing(freemarkerXMLProcessingTemplateURL);
    }
  }

  protected void doFreemarkerXMLProcessing(URL freemarkerXMLProcessingTemplateURL) throws IOException, EnunciateException {
    debug("Using freemarker XML processing template %s", freemarkerXMLProcessingTemplateURL);
    EnunciateFreemarkerModel model = getModel();
    File docsXml = new File(getGenerateDir(), "docs.xml");
    model.put("docsxml", loadNodeModel(docsXml));
    File downloadsXml = new File(getGenerateDir(), "downloads.xml");
    if (downloadsXml.exists()) {
      model.put("downloadsxml", loadNodeModel(downloadsXml));
    }
    File buildDir = getDocsBuildDir();
    buildDir.mkdirs();
    model.setFileOutputDirectory(buildDir);
    model.put("apiRelativePath", getRelativePathToRootDir());
    model.put("disableRestMountpoint", isDisableRestMountpoint());
    model.put("additionalCss", getAdditionalCss());
    try {
      processTemplate(freemarkerXMLProcessingTemplateURL, model);
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
    }
  }

  private NodeModel loadNodeModel(File xml) throws EnunciateException {
    Document doc;
    try {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(false);
      builderFactory.setValidating(false);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      builder.setEntityResolver(new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
          //we don't want to validate or parse external dtds...
          return new InputSource(new StringReader(""));
        }
      });
      doc = builder.parse(new FileInputStream(xml));
    }
    catch (Exception e) {
      throw new EnunciateException("Error parsing " + xml, e);
    }

    NodeModel.simplify(doc);
    return NodeModel.wrap(doc.getDocumentElement());
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
   * The set of facets to include.
   *
   * @return The set of facets to include.
   */
  public Set<String> getFacetIncludes() {
    return facetIncludes;
  }

  /**
   * Add a facet include.
   *
   * @param name The name.
   */
  public void addFacetInclude(String name) {
    if (name != null) {
      this.facetIncludes.add(name);
    }
  }

  /**
   * The set of facets to exclude.
   *
   * @return The set of facets to exclude.
   */
  public Set<String> getFacetExcludes() {
    return facetExcludes;
  }

  /**
   * Add a facet exclude.
   *
   * @param name The name.
   */
  public void addFacetExclude(String name) {
    if (name != null) {
      this.facetExcludes.add(name);
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

}