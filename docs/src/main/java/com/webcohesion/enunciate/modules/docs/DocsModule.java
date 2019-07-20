/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.*;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.artifacts.Artifact;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.ClientLibraryJavaArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;


public class DocsModule extends BasicGeneratingModule implements ApiRegistryAwareModule, DocumentationProviderModule {

  private File defaultDocsDir;
  private String defaultDocsSubdir;
  private ApiRegistry apiRegistry;

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
        return module instanceof ApiFeatureProviderModule;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }

      @Override
      public String toString() {
        return "all api feature provider modules";
      }
    });
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
      downloadConfig.setArtifact(download.getString("[@artifact]"));
      downloadConfig.setDescription(download.getString("[@description]"));
      downloadConfig.setFile(download.getString("[@file]"));
      downloadConfig.setName(download.getString("[@name]"));
      downloadConfig.setShowLink(download.getString("[@showLink]"));
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
      return DocsModule.class.getResource("docs.fmt");
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
    return this.config.getString("[@docsSubdir]", this.defaultDocsSubdir);
  }

  public boolean isDisableResourceLinks() {
    return this.config.getBoolean("[@disableResourceLinks]", false);
  }

  @Override
  public void setDefaultDocsDir(File docsDir) {
    this.defaultDocsDir = docsDir;
  }

  @Override
  public void setDefaultDocsSubdir(String defaultDocsSubdir) {
    this.defaultDocsSubdir = defaultDocsSubdir;
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
   * URI to the favicon for the generated documentation.
   *
   * @return URI to the favicon for the generated documentation.
   */
  public String getFavicon() {
    return this.config.getString("[@faviconUri]", null);
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
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
        Set<String> facetIncludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetIncludes());
        facetIncludes.addAll(getFacetIncludes());
        Set<String> facetExcludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetExcludes());
        facetExcludes.addAll(getFacetExcludes());
        FacetFilter facetFilter = new FacetFilter(facetIncludes, facetExcludes);

        ApiRegistrationContext registrationContext = new DocsRegistrationContext(this.apiRegistry, facetFilter);

        List<ResourceApi> resourceApis = this.apiRegistry.getResourceApis(registrationContext);
        Set<Syntax> syntaxes = this.apiRegistry.getSyntaxes(registrationContext);
        List<ServiceApi> serviceApis = this.apiRegistry.getServiceApis(registrationContext);

        Set<Artifact> documentationArtifacts = findDocumentationArtifacts();

        if (syntaxes.isEmpty() && serviceApis.isEmpty() && resourceApis.isEmpty() && documentationArtifacts.isEmpty()) {
          warn("No documentation generated: there are no data types, services, or resources to document.");
          return;
        }

        docsDir.mkdirs();// make sure the docs dir exists.

        Map<String, Object> model = new HashMap<String, Object>();

        String intro = this.enunciate.getConfiguration().readDescription(context, false, registrationContext.getTagHandler());
        if (intro != null) {
          model.put("apiDoc", intro);
        }

        String copyright = this.enunciate.getConfiguration().getCopyright();
        if (copyright != null) {
          model.put("copyright", copyright);
        }

        String title = this.enunciate.getConfiguration().getTitle();
        model.put("title", title == null ? "Web Service API" : title);

        //extract out the documentation base
        String cssPath = buildBase(docsDir);
        if (cssPath != null) {
          model.put("cssFile", cssPath);
        }

        model.put("file", new FileDirective(docsDir, this.enunciate.getLogger()));

        model.put("apiRelativePath", getRelativePathToRootDir());
        model.put("includeApplicationPath", isIncludeApplicationPath());

        model.put("favicon", getFavicon());

        //iterate through schemas and make sure the schema is copied to the docs dir
        for (Syntax syntax : syntaxes) {
          for (Namespace namespace : syntax.getNamespaces()) {
            if (namespace.getSchemaFile() != null) {
              namespace.getSchemaFile().writeTo(docsDir);
            }
          }
        }
        model.put("data", syntaxes);

        for (ResourceApi resourceApi : resourceApis) {
          if (resourceApi.getWadlFile() != null) {
            resourceApi.getWadlFile().writeTo(docsDir);
          }
        }
        model.put("resourceApis", resourceApis);

        InterfaceDescriptionFile swaggerUI = this.apiRegistry.getSwaggerUI();
        if (swaggerUI != null) {
          swaggerUI.writeTo(docsDir);
          model.put("swaggerUI", swaggerUI);
        }

        //iterate through wsdls and make sure the wsdl is copied to the docs dir
        for (ServiceApi serviceApi : serviceApis) {
          for (ServiceGroup serviceGroup : serviceApi.getServiceGroups()) {
            if (serviceGroup.getWsdlFile() != null) {
              serviceGroup.getWsdlFile().writeTo(docsDir);
            }
          }
        }
        model.put("serviceApis", serviceApis);

        model.put("downloads", copyDocumentationArtifacts(documentationArtifacts, docsDir));

        model.put("indexPageName", getIndexPageName());

        model.put("disableMountpoint", isDisableRestMountpoint());

        model.put("additionalCssFiles", getAdditionalCss());

        model.put("disableResourceLinks", isDisableResourceLinks());

        processTemplate(getDocsTemplateURL(), model);
      }
      else {
        info("Skipping documentation source generation as everything appears up-to-date...");
      }

      this.enunciate.addArtifact(new FileArtifact(getName(), "docs", docsDir));
    }
    catch (IOException e) {
      throw new EnunciateException(e);
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
    }
  }

  private boolean isIncludeApplicationPath() {
    return this.config.getBoolean("[@includeApplicationPath]", false);
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
    configuration.setLocale(new Locale("en", "US"));

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
    configuration.setURLEscapingCharset("UTF-8");
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    debug("Freemarker processing output:\n%s", unhandledOutput);
  }

  protected String buildBase(File outputDir) throws IOException {
    File baseFile = getBase();
    if (baseFile == null) {
      InputStream discoveredBase = DocsModule.class.getResourceAsStream("/META-INF/enunciate/docs-base.zip");
      if (discoveredBase == null) {
        debug("Default base to be used for documentation base.");
        this.enunciate.unzip(loadDefaultBase(), outputDir);

        String configuredCss = getCss();
        URL discoveredCss = DocsModule.class.getResource("/META-INF/enunciate/css/style.css");
        if (discoveredCss != null) {
          this.enunciate.copyResource(discoveredCss, new File(new File(outputDir, "css"), "enunciate.css"));
        }
        else if (configuredCss != null) {
          try {
            if (URI.create(configuredCss).isAbsolute()) {
              return configuredCss;
            }
          }
          catch (IllegalArgumentException e) {
            //fall through...
          }

          this.enunciate.copyFile(resolveFile(configuredCss), new File(new File(outputDir, "css"), "enunciate.css"));
        }

        return "css/enunciate.css";
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

  protected List<Download> copyDocumentationArtifacts(Set<Artifact> artifacts, File outputDir) throws IOException {

    ArrayList<Download> downloads = new ArrayList<Download>();

    for (Artifact artifact : artifacts) {
      debug("Exporting %s to directory %s.", artifact.getId(), outputDir);
      artifact.exportTo(outputDir, this.enunciate);

      if (artifact instanceof SpecifiedArtifact && !((SpecifiedArtifact)artifact).isShowLink()) {
        continue;
      }

      Download download = new Download();
      download.setSlug("artifact_" + artifact.getId().replace('.', '_'));
      download.setName(artifact.getName());
      download.setDescription(artifact.getDescription());
      download.setCreated(artifact.getCreated());

      if (artifact instanceof ClientLibraryJavaArtifact) {
        download.setGroupId(((ClientLibraryJavaArtifact)artifact).getGroupId());
        download.setArtifactId(((ClientLibraryJavaArtifact)artifact).getArtifactId());
        download.setVersion(((ClientLibraryJavaArtifact)artifact).getVersion());
      }

      Collection<? extends Artifact> childArtifacts = (artifact instanceof ClientLibraryArtifact) ? ((ClientLibraryArtifact) artifact).getArtifacts() : (artifact instanceof SpecifiedArtifact) ? Arrays.asList(((SpecifiedArtifact) artifact).getFile()) : Arrays.asList(artifact);
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

  private TreeSet<Artifact> findDocumentationArtifacts() {
    HashSet<String> explicitArtifacts = new HashSet<String>();
    TreeSet<Artifact> artifacts = new TreeSet<Artifact>();
    for (ExplicitDownloadConfig download : getExplicitDownloads()) {
      if (download.getArtifact() != null) {
        explicitArtifacts.add(download.getArtifact());
      }
      else if (download.getFile() != null) {
        File downloadFile = resolveFile(download.getFile());

        debug("File %s to be added as an extra download.", downloadFile.getAbsolutePath());
        SpecifiedArtifact artifact = new SpecifiedArtifact(getName(), downloadFile.getName(), downloadFile);

        if (download.getName() != null) {
          artifact.setName(download.getName());
        }

        if (download.getDescription() != null) {
          artifact.setDescription(download.getDescription());
        }

        artifact.setShowLink(!"false".equals(download.getShowLink()));

        artifacts.add(artifact);
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
    return artifacts;
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
    String relativePath = "./";
    String docsSubdir = getDocsSubdir();
    if (docsSubdir != null) {
      StringBuilder builder = new StringBuilder();
      StringTokenizer pathTokens = new StringTokenizer(docsSubdir.replace(File.separatorChar, '/'), "/");
      if (pathTokens.hasMoreTokens()) {
        while (pathTokens.hasMoreTokens()) {
          builder.append("../");
          pathTokens.nextToken();
        }

        relativePath = builder.toString();
      }
    }

    return this.config.getString("[@apiRelativePath]", relativePath);
  }


  /**
   * Loads the default base for the documentation.
   *
   * @return The default base for the documentation.
   */
  protected InputStream loadDefaultBase() {
    return DocsModule.class.getResourceAsStream("/docs.base.zip");
  }

  public Set<String> getFacetIncludes() {
    List<Object> includes = this.config.getList("facets.include[@name]");
    Set<String> facetIncludes = new TreeSet<String>();
    for (Object include : includes) {
      facetIncludes.add(String.valueOf(include));
    }
    return facetIncludes;
  }

  public Set<String> getFacetExcludes() {
    List<Object> excludes = this.config.getList("facets.exclude[@name]");
    Set<String> facetExcludes = new TreeSet<String>();
    for (Object exclude : excludes) {
      facetExcludes.add(String.valueOf(exclude));
    }
    return facetExcludes;
  }
}
