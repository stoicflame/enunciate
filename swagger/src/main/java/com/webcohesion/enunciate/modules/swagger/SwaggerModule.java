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
package com.webcohesion.enunciate.modules.swagger;

import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcohesion.enunciate.EnunciateConfiguration;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.javadoc.DefaultJavaDocTagHandler;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxb.JaxbModule;
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1>Swagger Module</h1>
 * @author Ryan Heaton
 */
public class SwaggerModule extends BasicGeneratingModule implements ApiFeatureProviderModule, ApiRegistryAwareModule, ApiRegistryProviderModule {

  private ApiRegistry apiRegistry;
  JaxbModule jaxbModule;

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

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return List.of(new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        if (module instanceof JaxbModule) {
          jaxbModule = (JaxbModule) module;
        }

        return !getName().equals(module.getName()) && module instanceof ApiRegistryProviderModule;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }


      @Override
      public String toString() {
        return "all api registry provider modules";
      }
    });
  }

  /**
   * The URL to "openapi.fmt".
   *
   * @return The URL to "openapi.fmt".
   */
  protected URL getTemplateURL() throws MalformedURLException {
    String template = getFreemarkerProcessingTemplate();
    if (template != null) {
      return this.enunciate.getConfiguration().resolveFile(template).toURI().toURL();
    }
    else {
      return SwaggerModule.class.getResource("openapi.fmt");
    }
  }

  @Override
  public void call(EnunciateContext context) {
    //no-op; work happens with the swagger interface description.
  }

  @Override
  public ApiRegistry getApiRegistry() {
    return new ApiRegistry() {
      @Override
      public List<ServiceApi> getServiceApis(ApiRegistrationContext context) {
        return Collections.emptyList();
      }

      @Override
      public List<ResourceApi> getResourceApis(ApiRegistrationContext context) {
        return Collections.emptyList();
      }

      @Override
      public Set<Syntax> getSyntaxes(ApiRegistrationContext context) {
        return Collections.emptySet();
      }

      @Override
      public InterfaceDescriptionFile getSwaggerUI() {
        Set<String> facetIncludes = new TreeSet<>(enunciate.getConfiguration().getFacetIncludes());
        facetIncludes.addAll(getFacetIncludes());
        Set<String> facetExcludes = new TreeSet<>(enunciate.getConfiguration().getFacetExcludes());
        facetExcludes.addAll(getFacetExcludes());
        FacetFilter facetFilter = new FacetFilter(facetIncludes, facetExcludes);

        ApiRegistrationContext context = new SwaggerRegistrationContext(facetFilter);
        List<ResourceApi> resourceApis = apiRegistry.getResourceApis(context);

        if (resourceApis == null || resourceApis.isEmpty()) {
          info("No resource APIs registered: Swagger UI will not be generated.");
        }

        return new SwaggerInterfaceDescription(resourceApis, context);
      }
    };
  }

  private class SwaggerInterfaceDescription implements InterfaceDescriptionFile {

    private final List<ResourceApi> resourceApis;
    private final ApiRegistrationContext context;

    public SwaggerInterfaceDescription(List<ResourceApi> resourceApis, ApiRegistrationContext context) {
      this.resourceApis = resourceApis;
      this.context = context;
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

      Map<String, Object> model = new HashMap<>();
      model.put("apis", this.resourceApis);
      boolean includeApplicationPath = isIncludeApplicationPath();
      Map<String, SwaggerResource> resourcesByPath = new TreeMap<>();
      for (ResourceApi resourceApi : this.resourceApis) {
        for (ResourceGroup resourceGroup : resourceApi.getResourceGroups()) {
          for (PathSummary pathSummary : resourceGroup.getPaths()) {
            String path = pathSummary.getPath();
            if (includeApplicationPath && !StringUtils.isEmpty(resourceGroup.getRelativeContextPath())) {
              path = "/" + resourceGroup.getRelativeContextPath() + path;
            }
            SwaggerResource swaggerResource = resourcesByPath.get(path);
            if (swaggerResource == null) {
              swaggerResource = new SwaggerResource(resourceGroup);
              resourcesByPath.put(path, swaggerResource);
            }

            for (Resource resource : resourceGroup.getResources()) {
              String resourcePath = resource.getPath();
              if (includeApplicationPath && !StringUtils.isEmpty(resourcePath)) {
                resourcePath = "/" + resourceGroup.getRelativeContextPath() + resourcePath;
              }
              if (path.equals(resourcePath)) {
                resource.getMethods().forEach(swaggerResource::addMethod);
              }
            }
          }
        }
      }
      Map<String, String> ns2prefix = Collections.emptyMap();
      if (jaxbModule != null) {
        ns2prefix = jaxbModule.getJaxbContext().getNamespacePrefixes();
      }

      model.put("resourcesByPath", resourcesByPath);
      model.put("syntaxes", apiRegistry.getSyntaxes(this.context));
      model.put("file", new FileDirective(srcDir, SwaggerModule.this.enunciate.getLogger()));
      model.put("projectVersion", enunciate.getConfiguration().getVersion());
      model.put("projectTitle", enunciate.getConfiguration().getTitle());
      model.put("projectDescription", enunciate.getConfiguration().readDescription(SwaggerModule.this.context, true, DefaultJavaDocTagHandler.INSTANCE));
      model.put("termsOfService", enunciate.getConfiguration().getTerms());
      List<EnunciateConfiguration.Contact> contacts = enunciate.getConfiguration().getContacts();
      model.put("contact", contacts == null || contacts.isEmpty() ? null : contacts.get(0));
      model.put("license", enunciate.getConfiguration().getApiLicense());
      model.put("baseDatatypeNameFor", new BaseDatatypeNameForMethod());
      model.put("referencedDatatypeNameFor", new ReferencedDatatypeNameForMethod());
      model.put("dataFormatNameFor", new DataFormatNameForMethod());
      model.put("constraintsFor", new ConstraintsForMethod());
      model.put("uniqueMediaTypesFor", new UniqueMediaTypesForMethod());
      model.put("jsonExampleFor", new JsonExampleForMethod());
      model.put("operationIdFor", new OperationIdForMethod());
      model.put("responsesOf", new ResponsesOfMethod());
      model.put("validParametersOf", new ValidParametersMethod());
      model.put("definitionIdFor", new DefinitionIdForMethod());
      model.put("prefixes", ns2prefix);
      model.put("servers", getServers());
      model.put("security", getSecurity());
      buildBase(srcDir);
      try {
        processTemplate(getTemplateURL(), model);
      }
      catch (TemplateException e) {
        throw new EnunciateException(e);
      }

      Set<File> jsonFilesToValidate = new HashSet<>();
      gatherJsonFiles(jsonFilesToValidate, srcDir);
      ObjectMapper mapper = new ObjectMapper();
      for (File file : jsonFilesToValidate) {
        try (FileReader reader = new FileReader(file)) {
          mapper.readTree(reader);
        }
        catch (JsonProcessingException e) {
          warn("Error processing %s.", file.getAbsolutePath());
          throw e;
        }
      }

      FileArtifact swaggerArtifact = new FileArtifact(getName(), "swagger", srcDir);
      swaggerArtifact.setPublic(false);
      SwaggerModule.this.enunciate.addArtifact(swaggerArtifact);
    }
  }

  protected String getBasePath() {
    String basePath = null;

    String root = enunciate.getConfiguration().getApplicationRoot();
    if (root != null) {
      try {
        URI uri = URI.create(root);
        basePath = uri.getPath();
      }
      catch (IllegalArgumentException ignored) {
      }
    }
    
    basePath = StringUtils.removeEnd(basePath, "/");

    return basePath;
  }
  
  protected List<SwaggerServer> getServers() {
    List<HierarchicalConfiguration> serverConfigs = this.config.configurationsAt("server");
    List<SwaggerServer> servers = serverConfigs.stream().map(serverConfig -> new SwaggerServer(serverConfig.getString("[@url]"), serverConfig.getString("[@description]", null))).collect(Collectors.toList());
    if (servers.isEmpty()) {
      servers = Collections.singletonList(new SwaggerServer(getBasePath(), null)); 
    }
    return servers;
  }

  protected List<SecurityScheme> getSecurity() {
    return ((List<HierarchicalConfiguration>)this.config.configurationsAt("securityScheme")).stream()
        .map(this::getSecuritySchema)
        .collect(Collectors.toList());
  }

  private SecurityScheme getSecuritySchema(HierarchicalConfiguration securityScheme) {
    if (securityScheme != null) {
      final String id = securityScheme.getString("[@id]");
      if (id != null) {
        return new SecurityScheme(id, securityScheme.getString("[@name]"),
            Optional.ofNullable(securityScheme.getString("[@description]")),
            Optional.ofNullable(securityScheme.getString("[@type]")).orElse("http"), null,
            Optional.ofNullable(securityScheme.getString("[@scheme]")).orElse(id.replace("Auth", "")), null, null,
            null);
      }
    }
    return null;
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
  public String processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
    debug("Processing template %s.", templateURL);
    Configuration configuration = new Configuration(FreemarkerUtil.VERSION);
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

    configuration.setTemplateExceptionHandler((templateException, environment, writer) -> {
      throw templateException;
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
      InputStream discoveredBase = SwaggerModule.class.getResourceAsStream("/META-INF/enunciate/swagger-base.zip");
      if (discoveredBase == null) {
        debug("Default base to be used for swagger base.");
        enunciate.unzip(loadDefaultBase(), buildDir);

        String css = getCss();
        if (css != null) {
          enunciate.copyFile(enunciate.getConfiguration().resolveFile(css), new File(new File(buildDir, "css"), "screen.css"));
        }
      }
      else {
        debug("Discovered documentation base at /META-INF/enunciate/swagger-base.zip");
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


    byte[] initializerJs = SwaggerModule.class.getResourceAsStream("swagger-initializer.js").readAllBytes();
    //filter here if you ever need e.g. variables in the initializer.
    Files.write(new File(buildDir, "swagger-initializer.js").toPath(), initializerJs);
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
    return SwaggerModule.class.getResourceAsStream("/swagger-ui.zip");
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
    Set<String> facetIncludes = new TreeSet<>();
    for (Object include : includes) {
      facetIncludes.add(String.valueOf(include));
    }
    return facetIncludes;
  }

  public Set<String> getFacetExcludes() {
    List<Object> excludes = this.config.getList("facets.exclude[@name]");
    Set<String> facetExcludes = new TreeSet<>();
    for (Object exclude : excludes) {
      facetExcludes.add(String.valueOf(exclude));
    }
    return facetExcludes;
  }

  public String getDocsSubdir() {
    return this.config.getString("[@docsSubdir]", "ui");
  }

}
