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

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.Download;
import com.webcohesion.enunciate.api.DownloadFile;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.artifacts.Artifact;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedPackageElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandlerFactory;
import com.webcohesion.enunciate.module.*;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.lang.model.element.PackageElement;
import java.io.*;
import java.net.MalformedURLException;
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

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    //documentation depends on any module that provides something to the api registry.
    return Arrays.asList((DependencySpec) new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        return module instanceof ApiRegistryProviderModule;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }
    });
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
  public Collection<ExplicitDownloadConfig> getExplicitDownloads() {
    List<HierarchicalConfiguration> downloads = this.config.configurationsAt("download");
    ArrayList<ExplicitDownloadConfig> downloadConfigs = new ArrayList<ExplicitDownloadConfig>(downloads.size());
    for (HierarchicalConfiguration download : downloads) {
      ExplicitDownloadConfig downloadConfig = new ExplicitDownloadConfig();
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
   * The URL to the Freemarker template for processing the base documentation xml file.
   *
   * @return The URL to the Freemarker template for processing the base documentation xml file.
   */
  protected URL getDocsTemplateURL() throws MalformedURLException {
    File templateFile = getFreemarkerTemplateFile();
    if (templateFile != null && !templateFile.exists()) {
      warn("Unable to use freemarker template at %s: file doesn't exist!", templateFile);
      templateFile = null;
    }

    if (templateFile != null) {
      return templateFile.toURI().toURL();
    }
    else {
      return DocumentationDeploymentModule.class.getResource("docs.fmt");
    }
  }

  /**
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @return The cascading stylesheet to use.
   */
  public String getCss() {
    return this.config.getString("[@css]");
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
    return docsDir != null ? resolveFile(docsDir) : this.defaultDocsDir != null ? this.defaultDocsDir : new File(this.enunciate.getBuildDir(), getName());
  }

  public String getDocsSubdir() {
    return this.config.getString("[@docsSubdir]");
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
    return this.config.getString("[@indexPageName]", "index.html");
  }

  /**
   * Whether to disable the REST mountpoint documentation.
   *
   * @return Whether to disable the REST mountpoint documentation.
   */
  public boolean isDisableRestMountpoint() {
    return this.config.getBoolean("[@disableRestMountpoint]", false);
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
    try {
      File docsDir = getDocsDir();
      String subDir = getDocsSubdir();
      if (subDir != null) {
        docsDir = new File(docsDir, subDir);
      }

      if (!isUpToDateWithSources(docsDir)) {
        docsDir.mkdirs();// make sure the docs dir exists.

        JavaDocTagHandlerFactory.setTagHandler(new DocumentationJavaDocTagHandler());

        Map<String, Object> model = new HashMap<String, Object>();

        String splashPackage = getSplashPackage();
        if (splashPackage != null) {
          PackageElement packageDeclaration = context.getProcessingEnvironment().getElementUtils().getPackageElement(splashPackage);
          if (packageDeclaration != null) {
            debug("Including documentation for package %s as the splash documentation.", splashPackage);
            model.put("apiDoc", ((DecoratedPackageElement) packageDeclaration).getJavaDoc().toString());
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

        //extract out the documentation base
        String cssPath = buildBase(docsDir);
        if (cssPath != null) {
          model.put("cssFile", cssPath);
        }

        model.put("file", new FileDirective(docsDir));

        List<? extends ResourceGroup> resourceGroups = this.apiRegistry.getResourceGroups();
        //todo: filter by facet
        model.put("resourceGroups", resourceGroups);

        List<? extends ServiceGroup> serviceGroups = this.apiRegistry.getServiceGroups();
        //todo: filter by facet
        //iterate through wsdls and make sure the wsdl is copied to the docs dir
        for (ServiceGroup serviceGroup : serviceGroups) {
          File wsdl = serviceGroup.getWsdlFile();
          if (wsdl != null) {
            this.enunciate.copyFile(wsdl, new File(docsDir, wsdl.getName()));
          }
        }
        model.put("serviceGroups", serviceGroups);

        List<Syntax> data = this.apiRegistry.getSyntaxes();
        //todo: filter by facet
        //iterate through schemas and make sure the schema is copied to the docs dir
        for (Syntax syntax : data) {
          for (Namespace namespace : syntax.getNamespaces()) {
            File schema = namespace.getSchemaFile();
            if (schema != null) {
              this.enunciate.copyFile(schema, new File(docsDir, schema.getName()));
            }
          }
        }
        model.put("data", data);

        List<Download> downloads = copyArtifacts(docsDir);
        model.put("downloads", downloads);

        if (data.isEmpty() && serviceGroups.isEmpty() && resourceGroups.isEmpty() && downloads.isEmpty()) {
          throw new EnunciateException("There are no data types, services, or resources to document.");
        }

        model.put("indexPageName", getIndexPageName());

        model.put("apiRelativePath", getRelativePathToRootDir());

        model.put("disableMountpoint", isDisableRestMountpoint());

        model.put("additionalCssFiles", getAdditionalCss());

        if (this.swaggerOutputDir != null) {
          model.put("swaggerDir", "swagger");
        }

        if (this.wadlFile != null) {
          this.enunciate.copyFile(this.wadlFile, new File(docsDir, this.wadlFile.getName()));
          model.put("wadl", this.wadlFile);
        }

        processTemplate(getDocsTemplateURL(), model);

        this.enunciate.addArtifact(new FileArtifact(getName(), "docs", docsDir));
      }
      else {
        info("Skipping documentation source generation as everything appears up-to-date...");
      }
    }
    catch (IOException e) {
      throw new EnunciateException(e);
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
    }
  }

  /**
   * Processes the specified template with the given model.
   *
   * @param templateURL The template URL.
   * @param model       The root model.
   */
  public void processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
    debug("Processing template %s.", templateURL);
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);

    configuration.setTemplateLoader(new URLTemplateLoader() {
      protected URL getURL(String name) {
        try {
          return new URL(name);
        }
        catch (MalformedURLException e) {
          return null;
        }
      }
    });

    configuration.setTemplateExceptionHandler(new TemplateExceptionHandler() {
      public void handleTemplateException(TemplateException templateException, Environment environment, Writer writer) throws TemplateException {
        throw templateException;
      }
    });

    configuration.setLocalizedLookup(false);
    configuration.setDefaultEncoding("UTF-8");
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    debug("Freemarker processing output:\n%s", unhandledOutput);
  }

  protected String buildBase(File outputDir) throws IOException {
    File baseFile = getBase();
    if (baseFile == null) {
      InputStream discoveredBase = DocumentationDeploymentModule.class.getResourceAsStream("/META-INF/enunciate/docs-base.zip");
      if (discoveredBase == null) {
        debug("Default base to be used for documentation base.");
        this.enunciate.unzip(loadDefaultBase(), outputDir);

        String configuredCss = getCss();
        URL discoveredCss = DocumentationDeploymentModule.class.getResource("/META-INF/enunciate/css/style.css");
        if (discoveredCss != null) {
          this.enunciate.copyResource(discoveredCss, new File(new File(outputDir, "css"), "style.css"));
        }
        else if (configuredCss != null) {
          this.enunciate.copyFile(resolveFile(configuredCss), new File(new File(outputDir, "css"), "style.css"));
        }

        return "css/style.css";
      }
      else {
        debug("Discovered documentation base at /META-INF/enunciate/docs-base.zip");
        this.enunciate.unzip(discoveredBase, outputDir);

        return null;
      }
    }
    else if (baseFile.isDirectory()) {
      debug("Directory %s to be used as the documentation base.", baseFile);
      this.enunciate.copyDir(baseFile, outputDir);
      return null;
    }
    else {
      debug("Zip file %s to be extracted as the documentation base.", baseFile);
      this.enunciate.unzip(new FileInputStream(baseFile), outputDir);
      return null;
    }
  }

  protected List<Download> copyArtifacts(File outputDir) throws IOException {

    HashSet<String> explicitArtifacts = new HashSet<String>();
    TreeSet<Artifact> artifacts = new TreeSet<Artifact>();
    for (ExplicitDownloadConfig download : getExplicitDownloads()) {
      if (download.getArtifact() != null) {
        explicitArtifacts.add(download.getArtifact());
      }
      else if (download.getFile() != null) {
        File downloadFile = resolveFile(download.getFile());

        debug("File %s to be added as an extra download.", downloadFile.getAbsolutePath());
        ExplicitArtifact artifact = new ExplicitArtifact(getName(), downloadFile.getName(), downloadFile);

        if (download.getName() != null) {
          artifact.setName(download.getName());
        }

        if (download.getDescription() != null) {
          artifact.setDescription(download.getDescription());
        }

        if (download.getShowLink().equals("false")) {
          debug("Exporting %s to directory %s.", artifact.getId(), outputDir);
          artifact.exportTo(outputDir, this.enunciate);
        }
        else {
          artifacts.add(artifact);
        }
      }
    }

    for (Artifact artifact : this.enunciate.getArtifacts()) {
      if (artifact.isPublic() || explicitArtifacts.contains(artifact.getId())) {
        artifacts.add(artifact);
        debug("Artifact %s to be added as an extra download.", artifact.getId());
        explicitArtifacts.remove(artifact.getId());
      }
    }

    if (explicitArtifacts.size() > 0) {
      for (String artifactId : explicitArtifacts) {
        warn("WARNING: Unknown artifact '%s'.  Will not be available for download.", artifactId);
      }
    }

    ArrayList<Download> downloads = new ArrayList<Download>();

    for (Artifact artifact : artifacts) {
      debug("Exporting %s to directory %s.", artifact.getId(), outputDir);
      artifact.exportTo(outputDir, this.enunciate);
      Download download = new Download();
      download.setSlug("artifact_" + artifact.getId());
      download.setName(artifact.getName());
      download.setDescription(artifact.getDescription());
      download.setCreated(artifact.getCreated());

      Collection<? extends Artifact> childArtifacts = (artifact instanceof ClientLibraryArtifact) ? ((ClientLibraryArtifact) artifact).getArtifacts() : (artifact instanceof ExplicitArtifact) ? Arrays.asList(((ExplicitArtifact) artifact).getFile()) : Arrays.asList(artifact);
      ArrayList<DownloadFile> downloadFiles = new ArrayList<DownloadFile>();
      for (Artifact childArtifact : childArtifacts) {
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setDescription(childArtifact.getDescription());
        downloadFile.setName(childArtifact.getName());
        downloadFile.setSize(getDisplaySize(childArtifact.getSize()));
        downloadFiles.add(downloadFile);
      }
      download.setFiles(downloadFiles);

      downloads.add(download);
    }

    return downloads;
  }

  public String getDisplaySize(long sizeInBytes) {
    String units = "bytes";
    float unitSize = 1;

    if ((sizeInBytes / 1024) > 0) {
      units = "K";
      unitSize = 1024;
    }

    if ((sizeInBytes / 1048576) > 0) {
      units = "M";
      unitSize = 1048576;
    }

    if ((sizeInBytes / 1073741824) > 0) {
      units = "G";
      unitSize = 1073741824;
    }

    return String.format("%.2f%s", ((float) sizeInBytes) / unitSize, units);
  }

  /**
   * Get the relative path to the root directory from the docs directory.
   *
   * @return the relative path to the root directory.
   */
  protected String getRelativePathToRootDir() {
    String relativePath = ".";
    String docsSubdir = getDocsSubdir();
    if (docsSubdir != null) {
      StringBuilder builder = new StringBuilder();
      StringTokenizer pathTokens = new StringTokenizer(docsSubdir.replace(File.separatorChar, '/'), "/");
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