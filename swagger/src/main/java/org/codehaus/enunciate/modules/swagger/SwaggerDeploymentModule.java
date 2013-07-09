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

package org.codehaus.enunciate.modules.swagger;

import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.Facet;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.main.FileArtifact;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.FacetAware;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.swagger.config.SwaggerRuleSet;
import org.codehaus.enunciate.template.freemarker.UniqueContentTypesMethod;
import org.codehaus.enunciate.util.FacetFilter;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * <h1>Swagger Module</h1>
 *
 * <p>The Swagger deployment module generates a <a href="https://developers.helloreverb.com/swagger/">Swagger</a> UI.</a>.
 *
 * <ul>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <h3>generate</h3>
 *
 * <p>The only significant step in the Swagger deployment module is the "generate" step.  This step generates the
 * Swagger UI.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The configuration for the XML deployment module is specified by the "swagger" child element of the "modules"
 * element of the enunciate configuration file.</p>
 *
 * <h3>attributes</h3>
 *
 * <ul>
 *   <li>The "<b>dir</b>" attribute is used to specify the directory name for the Swagger UI in the deployed application. Default: "ui"</p>
 *   <li>The "<b>css</b>" attribute is used to specify the file to be used as the cascading stylesheet for the HTML.
 * If one isn't supplied, a default will be provided.</p>
 *   <li>The "<b>base</b>" attribute specifies a gzipped file or a directory to use as the swagger ui base.  If none is supplied,
 * a default base will be provided.
 *   <li>The "<b>groupRestResources</b>" attribute specifies the name of a facet by which resources are grouped together. Default: "org.codehaus.enunciate.contract.jaxrs.Resource".</li>
 *   <li>The "<b>applyBaseUriFilter</b>" attribute specifies whether there should be a servlet filter applied that dynamically configures the Swagger base uri according to where the Swagger UI is deployed.</li>
 * </ul>
 *
 * <h3>The "facets" element</h3>
 *
 * <p>The "facets" element is applicable to the Swagger module to configure which facets are to be included/excluded from the Swagger artifacts. For
 * more information, see <a href="http://docs.codehaus.org/display/ENUNCIATE/Enunciate+API+Facets">API Facets</a></p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The Swagger deployment module exports an artifact named "swagger" that contains the swagger UI.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_swagger.html
 */
public class SwaggerDeploymentModule extends FreemarkerDeploymentModule implements FacetAware {

  private String dir = "ui";
  private String base;
  private String css;
  private String groupRestResources = "org.codehaus.enunciate.contract.jaxrs.Resource";
  private boolean applyBaseUriFilter = true;
  private Set<String> facetIncludes = new TreeSet<String>();
  private Set<String> facetExcludes = new TreeSet<String>();

  /**
   * @return "swagger"
   */
  @Override
  public String getName() {
    return "swagger";
  }

  /**
   * The URL to "swagger.fmt".
   *
   * @return The URL to "swagger.fmt".
   */
  protected URL getTemplateURL() {
    return SwaggerDeploymentModule.class.getResource("swagger.fmt");
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    File artifactDir = new File(getGenerateDir(), getDir());
    model.setFileOutputDirectory(artifactDir);
    boolean upToDate = isUpToDate(artifactDir);
    if (!upToDate) {
      Map<String, Map<String, List<ResourceMethod>>> facetsToResourceMethods = new TreeMap<String, Map<String, List<ResourceMethod>>>();
      Map<String, String> facetsToDocs = new TreeMap<String, String>();
      for (RootResource rootResource : getModelInternal().getRootResources()) {
        for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
          if (FacetFilter.accept(resourceMethod)) {
            Set<Facet> facets = resourceMethod.getFacets();
            if (facets != null) {
              for (Facet facet : facets) {
                if (facet.getName().equals(getGroupRestResources())) {
                  Map<String, List<ResourceMethod>> group = facetsToResourceMethods.get(facet.getValue());
                  if (group == null) {
                    group = new TreeMap<String, List<ResourceMethod>>();
                    facetsToResourceMethods.put(facet.getValue(), group);
                  }
                  String subcontext = (String) resourceMethod.getMetaData().get("defaultSubcontext");
                  if (subcontext == null) {
                    subcontext = "/rest";
                  }
                  String fullpath = subcontext + resourceMethod.getFullpath();
                  List<ResourceMethod> methods = group.get(fullpath);
                  if (methods == null) {
                    methods = new ArrayList<ResourceMethod>();
                    group.put(fullpath, methods);
                  }
                  methods.add(resourceMethod);
                  facetsToDocs.put(facet.getValue(), facet.getDocumentation());
                }
              }
            }
          }
        }
      }
      model.setVariable("uniqueContentTypes", new UniqueContentTypesMethod());
      model.put("datatypeNameFor", new DatatypeNameForMethod(model));
      model.put("facetsToResourceMethods", facetsToResourceMethods);
      model.put("facetsToDocs", facetsToDocs);
      model.put("swaggerDir", artifactDir.getName());
      buildBase(artifactDir);
      processTemplate(getTemplateURL(), model);
    }
    else {
      info("Skipping generation of Swagger since everything appears up-to-date...");
    }

    Set<File> jsonFilesToValidate = new HashSet<File>();
    gatherJsonFiles(jsonFilesToValidate, artifactDir);
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

    //add the webapp fragment...
    BaseWebAppFragment webAppFragment = new BaseWebAppFragment(getName());
    webAppFragment.setBaseDir(getGenerateDir());
    ArrayList<WebAppComponent> filters = new ArrayList<WebAppComponent>();
    if (isApplyBaseUriFilter()) {
      WebAppComponent swaggerFilter = new WebAppComponent();
      swaggerFilter.setName("swagger-ui-filter");
      swaggerFilter.setClassname("org.codehaus.enunciate.webapp.IDLFilter");
      HashMap<String, String> initParams = new HashMap<String, String>();
      initParams.put("assumed-base-address", getModel().getBaseDeploymentAddress());
      initParams.put("match-prefix", "\"");
      initParams.put("content-type", "application/json");
      swaggerFilter.setInitParams(initParams);
      TreeSet<String> paths = new TreeSet<String>();

      for (File json : jsonFilesToValidate) {
        paths.add("/" + artifactDir.getName() + "/" + json.getName());
      }

      swaggerFilter.setUrlMappings(paths);
      filters.add(swaggerFilter);
    }

    webAppFragment.setFilters(filters);
    getEnunciate().addWebAppFragment(webAppFragment);

    getEnunciate().addArtifact(new FileArtifact(getName(), "swagger", artifactDir));
  }

  private void gatherJsonFiles(Set<File> bucket, File buildDir) {
    File[] files = buildDir.listFiles();
    for (File file : files) {
      if (file.getName().endsWith(".json")) {
        bucket.add(file);
      }
      else if (file.isDirectory()) {
        gatherJsonFiles(bucket, file);
      }
    }
  }

  /**
   * Builds the base output directory.
   */
  protected void buildBase(File buildDir) throws IOException {
    Enunciate enunciate = getEnunciate();
    buildDir.mkdirs();
    if (this.base == null) {
      InputStream discoveredBase = SwaggerDeploymentModule.class.getResourceAsStream("/META-INF/enunciate/swagger-base.zip");
      if (discoveredBase == null) {
        debug("Default base to be used for swagger base.");
        enunciate.extractBase(loadDefaultBase(), buildDir);

        if (this.css != null) {
          enunciate.copyFile(enunciate.resolvePath(this.css), new File(new File(buildDir, "css"), "screen.css"));
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
   * Whether the artifact directory is up-to-date.
   *
   * @param artifactDir The artifact directory.
   * @return Whether the artifact directory is up-to-date.
   */
  protected boolean isUpToDate(File artifactDir) {
    return enunciate.isUpToDateWithSources(artifactDir);
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new SwaggerRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new SwaggerValidator();
  }

  /**
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @return The cascading stylesheet to use.
   */
  public String getCss() {
    return css;
  }

  /**
   * The cascading stylesheet to use instead of the default.  This is ignored if the 'base' is also set.
   *
   * @param css The cascading stylesheet to use instead of the default.
   */
  public void setCss(String css) {
    this.css = css;
  }

  /**
   * The swagger "base".  The swagger base is the initial contents of the directory
   * where the swagger ui will be output.  Can be a zip file or a directory.
   *
   * @return The documentation "base".
   */
  public String getBase() {
    return base;
  }

  /**
   * The swagger "base".
   *
   * @param base The swagger "base".
   */
  public void setBase(String base) {
    this.base = base;
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
   * How to group the REST resources together.
   *
   * @return How to group the REST resources together.
   */
  public String getGroupRestResources() {
    return groupRestResources;
  }

  /**
   * How to group the REST resources together.
   *
   * @param groupRestResources How to group the REST resources together.
   */
  public void setGroupRestResources(String groupRestResources) {
    this.groupRestResources = groupRestResources;
  }

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public boolean isApplyBaseUriFilter() {
    return applyBaseUriFilter;
  }

  public void setApplyBaseUriFilter(boolean applyBaseUriFilter) {
    this.applyBaseUriFilter = applyBaseUriFilter;
  }
}
