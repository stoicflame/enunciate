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

package com.webcohesion.enunciate.modules.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcohesion.enunciate.EnunciateConfiguration;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.module.ApiProviderModule;
import com.webcohesion.enunciate.module.ApiRegistryAwareModule;
import com.webcohesion.enunciate.module.BasicGeneratingModule;
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * <h1>Swagger Module</h1>
 * @author Ryan Heaton
 */
public class SwaggerDeploymentModule extends BasicGeneratingModule implements ApiProviderModule, ApiRegistryAwareModule {

  private ApiRegistry apiRegistry;

  /**
   * @return "swagger"
   */
  @Override
  public String getName() {
    return "swagger";
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  /**
   * The URL to "swagger.fmt".
   *
   * @return The URL to "swagger.fmt".
   */
  protected URL getTemplateURL() throws MalformedURLException {
    String template = getFreemarkerProcessingTemplate();
    if (template != null) {
      return this.enunciate.getConfiguration().resolveFile(template).toURI().toURL();
    }
    else {
      return SwaggerDeploymentModule.class.getResource("swagger.fmt");
    }
  }

  @Override
  public void call(EnunciateContext context) {
    List<ResourceApi> resourceApis = this.apiRegistry.getResourceApis();
    if (resourceApis == null || resourceApis.isEmpty()) {
      info("No resource APIs registered: Swagger UI will not be generated.");
      return;
    }

    this.apiRegistry.setSwaggerUI(new SwaggerInterfaceDescription(resourceApis));

  }

  private class SwaggerInterfaceDescription implements InterfaceDescriptionFile {

    private final List<ResourceApi> resourceApis;

    public SwaggerInterfaceDescription(List<ResourceApi> resourceApis) {
      this.resourceApis = resourceApis;
    }

    @Override
    public String getHref() {
      return getDocsSubdir() + "/index.html";
    }

    @Override
    public void writeTo(File srcDir) throws IOException {
      srcDir.mkdirs();
      String subdir = getDocsSubdir();
      if (subdir != null) {
        srcDir = new File(srcDir, subdir);
        srcDir.mkdirs();
      }

      Map<String, Object> model = new HashMap<String, Object>();
      model.put("apis", this.resourceApis);
      model.put("syntaxes", apiRegistry.getSyntaxes());
      model.put("file", new FileDirective(srcDir));
      model.put("projectVersion", enunciate.getConfiguration().getVersion());
      model.put("projectTitle", enunciate.getConfiguration().getTitle());
      model.put("projectDescription", enunciate.getConfiguration().readDescription(context));
      model.put("termsOfService", enunciate.getConfiguration().getTerms());
      List<EnunciateConfiguration.Contact> contacts = enunciate.getConfiguration().getContacts();
      model.put("contact", contacts == null || contacts.isEmpty() ? null : contacts.get(0));
      model.put("license", enunciate.getConfiguration().getApiLicense());
      model.put("datatypeNameFor", new DatatypeNameForMethod());
      model.put("responsesOf", new ResponsesOfMethod());
      model.put("host", getHost());
      model.put("basePath", getBasePath());
      buildBase(srcDir);
      try {
        processTemplate(getTemplateURL(), model);
      }
      catch (TemplateException e) {
        throw new EnunciateException(e);
      }

      Set<File> jsonFilesToValidate = new HashSet<File>();
      gatherJsonFiles(jsonFilesToValidate, srcDir);
      ObjectMapper mapper = new ObjectMapper();
      for (File file : jsonFilesToValidate) {
        FileReader reader = new FileReader(file);
        try {
          mapper.readTree(reader);
        }
        catch (JsonProcessingException e) {
          warn("Error processing %s.", file.getAbsolutePath());
          throw e;
        }
        finally {
          reader.close();
        }
      }

      SwaggerDeploymentModule.this.enunciate.addArtifact(new FileArtifact(getName(), "swagger", srcDir));
    }
  }

  protected String getHost() {
    String host = this.config.getString("[@host]", null);

    if (host == null) {
      String root = enunciate.getConfiguration().getApplicationRoot();
      if (root != null) {
        try {
          URI uri = URI.create(root);
          host = uri.getHost();
        }
        catch (IllegalArgumentException e) {
          host = null;
        }
      }
    }

    return host;
  }

  protected String getBasePath() {
    String basePath = this.config.getString("[@basePath]", null);

    if (basePath == null) {
      String root = enunciate.getConfiguration().getApplicationRoot();
      if (root != null) {
        try {
          URI uri = URI.create(root);
          basePath = uri.getPath();
        }
        catch (IllegalArgumentException e) {
          basePath = null;
        }
      }
    }

    return basePath;
  }

  /**
   * Processes the specified template with the given model.
   *
   * @param templateURL The template URL.
   * @param model       The root model.
   */
  public String processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
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
    configuration.setObjectWrapper(new SwaggerUIObjectWrapper());
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    unhandledOutput.close();
    return unhandledOutput.toString();
  }

  /**
   * Builds the base output directory.
   */
  protected void buildBase(File buildDir) throws IOException {
    String base = getBase();
    if (base == null) {
      InputStream discoveredBase = SwaggerDeploymentModule.class.getResourceAsStream("/META-INF/enunciate/swagger-base.zip");
      if (discoveredBase == null) {
        debug("Default base to be used for swagger base.");
        enunciate.unzip(loadDefaultBase(), buildDir);

        String css = getCss();
        if (css != null) {
          enunciate.copyFile(enunciate.getConfiguration().resolveFile(css), new File(new File(buildDir, "css"), "screen.css"));
        }
      }
      else {
        debug("Discovered documentation base at /META-INF/enunciate/docs-base.zip");
        enunciate.unzip(discoveredBase, buildDir);
      }
    }
    else {
      File baseFile = enunciate.getConfiguration().resolveFile(base);
      if (baseFile.isDirectory()) {
        debug("Directory %s to be used as the documentation base.", baseFile);
        enunciate.copyDir(baseFile, buildDir);
      }
      else {
        debug("Zip file %s to be extracted as the documentation base.", baseFile);
        enunciate.unzip(new FileInputStream(baseFile), buildDir);
      }
    }
  }

  private void gatherJsonFiles(Set<File> bucket, File buildDir) {
    File[] files = buildDir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().endsWith(".json")) {
          bucket.add(file);
        }
        else if (file.isDirectory()) {
          gatherJsonFiles(bucket, file);
        }
      }
    }
  }

  /**
   * Loads the default base for the swagger ui.
   *
   * @return The default base for the swagger ui.
   */
  protected InputStream loadDefaultBase() {
    return SwaggerDeploymentModule.class.getResourceAsStream("/swagger-ui.zip");
  }

  /**
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @return The cascading stylesheet to use.
   */
  public String getCss() {
    return this.config.getString("[@css]", null);
  }

  public String getFreemarkerProcessingTemplate() {
    return this.config.getString("[@freemarkerProcessingTemplate]", null);
  }

  /**
   * The swagger "base".  The swagger base is the initial contents of the directory
   * where the swagger ui will be output.  Can be a zip file or a directory.
   *
   * @return The documentation "base".
   */
  public String getBase() {
    return this.config.getString("[@base]", null);
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

  public String getDocsSubdir() {
    return this.config.getString("[@docsSubdir]", "ui");
  }

}
