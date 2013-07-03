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

package org.codehaus.enunciate.modules.ruby;

import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.template.freemarker.ClientPackageForMethod;
import org.codehaus.enunciate.template.freemarker.SimpleNameWithParamsMethod;
import org.codehaus.enunciate.apt.EnunciateClasspathListener;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.main.NamedFileArtifact;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.main.ArtifactType;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.ruby.config.PackageModuleConversion;
import org.codehaus.enunciate.modules.ruby.config.RubyRuleSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;

import com.sun.mirror.type.ClassType;

/**
 * <h1>Ruby Module</h1>
 *
 * <p>The Ruby module generates Ruby data types that can be used in conjunction with the <a href="http://json.rubyforge.org/">Ruby JSON implementation</a>
 * to (de)serialize the REST resources as they are represented as JSON data.</p>
 *
 * <p>The order of the Ruby deployment module is 0, as it doesn't depend on any artifacts exported by any other module.</p>
 *
 * <ul>
 * <li><a href="#config">configuration</a></li>
 * </ul>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The Ruby module is configured with the "ruby" element under the "modules" element of the enunciate configuration file. It supports the following
 * attributes:</p>
 *
 * <ul>
 * <li>The "label" attribute is the label for the Ruby API.  This is the name by which the file will be identified (producing [label].rb).
 * By default the label is the same as the Enunciate project label.</li>
 * <li>The "forceEnable" attribute is used to force-enable the Ruby module. By default, the Ruby module is
 * enabled only when both of these conditions are met:
 *   <ol>
 *     <li>Jackson-XC is on the claspath.</li>
 *     <li>There exists a JAX-RS resource method that consumes or produces JSON.</li>
 *   </ol> 
 * </li>
 * </ul>
 *
 * <h3>The "package-conversions" element</h3>
 *
 * <p>The "package-conversions" subelement of the "ruby" element is used to map packages from
 * the original API packages to Ruby modules.  This element supports an arbitrary number of
 * "convert" child elements that are used to specify the conversions.  These "convert" elements support
 * the following attributes:</p>
 *
 * <ul>
 * <li>The "from" attribute specifies the package that is to be converted.  This package will match
 * all classes in the package as well as any subpackages of the package.  This means that if "org.enunciate"
 * were specified, it would match "org.enunciate", "org.enunciate.api", and "org.enunciate.api.impl".</li>
 * <li>The "to" attribute specifies what the package is to be converted to.  Only the part of the package
 * that matches the "from" attribute will be converted.</li>
 * </ul>
 *
 * <h3>The "facets" element</h3>
 *
 * <p>The "facets" element is applicable to the Ruby module to configure which facets are to be included/excluded from the Ruby artifacts. For
 * more information, see <a href="http://docs.codehaus.org/display/ENUNCIATE/Enunciate+API+Facets">API Facets</a></p>
 *
 * @author Ryan Heaton
 * @docFileName module_ruby.html
 */
public class RubyDeploymentModule extends FreemarkerDeploymentModule implements EnunciateClasspathListener {

  private boolean forceEnable = false;
  private String label = null;
  private final Map<String, String> packageToModuleConversions = new HashMap<String, String>();
  private boolean jacksonXcAvailable = false;
  private Set<String> facetIncludes = new TreeSet<String>();
  private Set<String> facetExcludes = new TreeSet<String>();

  /**
   * @return "ruby"
   */
  @Override
  public String getName() {
    return "ruby";
  }

  public void onClassesFound(Set<String> classes) {
    jacksonXcAvailable |= classes.contains("org.codehaus.jackson.xc.JaxbAnnotationIntrospector");
  }

  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          String pckg = typeDefinition.getPackage().getQualifiedName();
          if (!this.packageToModuleConversions.containsKey(pckg)) {
            this.packageToModuleConversions.put(pckg, packageToModule(pckg));
          }
        }
      }
    }
  }

  protected String packageToModule(String pckg) {
    if (pckg == null) {
      return null;
    }
    else {
      StringBuilder ns = new StringBuilder();
      for (StringTokenizer toks = new StringTokenizer(pckg, "."); toks.hasMoreTokens();) {
        String tok = toks.nextToken();
        ns.append(Character.toString(tok.charAt(0)).toUpperCase());
        if (tok.length() > 1) {
          ns.append(tok.substring(1));
        }
        if (toks.hasMoreTokens()) {
          ns.append("::");
        }
      }
      return ns.toString();
    }
  }

  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    File genDir = getGenerateDir();
    if (!enunciate.isUpToDateWithSources(genDir)) {
      EnunciateFreemarkerModel model = getModel();
      List<TypeDefinition> schemaTypes = new ArrayList<TypeDefinition>();
      ExtensionDepthComparator comparator = new ExtensionDepthComparator();
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          int position = Collections.binarySearch(schemaTypes, typeDefinition, comparator);
          if (position < 0) {
            position = -position - 1;
          }
          schemaTypes.add(position, typeDefinition);
        }
      }
      model.put("schemaTypes", schemaTypes);
      model.put("packages2modules", this.packageToModuleConversions);
      ClientPackageForMethod moduleFor = new ClientPackageForMethod(this.packageToModuleConversions);
      moduleFor.setUseClientNameConversions(true);
      model.put("moduleFor", moduleFor);
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(this.packageToModuleConversions);
      classnameFor.setUseClientNameConversions(true);
      model.put("classnameFor", classnameFor);
      SimpleNameWithParamsMethod simpleNameFor = new SimpleNameWithParamsMethod(classnameFor);
      model.put("simpleNameFor", simpleNameFor);
      model.put("rubyFileName", getSourceFileName());

      debug("Generating the Ruby data classes...");
      URL apiTemplate = getTemplateURL("api.fmt");
      processTemplate(apiTemplate, model);
    }
    else {
      info("Skipping Ruby code generation because everything appears up-to-date.");
    }

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "ruby.client.library", "Ruby Client Library");
    artifactBundle.setPlatform("Ruby");
    NamedFileArtifact sourceScript = new NamedFileArtifact(getName(), "ruby.client", new File(getGenerateDir(), getSourceFileName()));
    sourceScript.setArtifactType(ArtifactType.binaries); //binaries and sources are the same thing in ruby
    sourceScript.setPublic(false);
    String description = readResource("library_description.fmt"); //read in the description from file
    artifactBundle.setDescription(description);
    artifactBundle.addArtifact(sourceScript);
    getEnunciate().addArtifact(artifactBundle);
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource) throws IOException, EnunciateException {
    HashMap<String, Object> model = new HashMap<String, Object>();
    model.put("sample_resource", getModelInternal().findExampleResourceMethod());

    URL res = RubyDeploymentModule.class.getResource(resource);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bytes);
    try {
      processTemplate(res, model, out);
      out.flush();
      bytes.flush();
      return bytes.toString("utf-8");
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
    }
  }

  /**
   * The name of the generated Ruby source file.
   *
   * @return The name of the generated Ruby source file.
   */
  protected String getSourceFileName() {
    String label = getLabel();
    if (label == null) {
      label = getEnunciate().getConfig().getLabel();
    }
    return label + ".rb";
  }

  @Override
  protected ObjectWrapper getObjectWrapper() {
    return new DefaultObjectWrapper() {
      @Override
      public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof JavaDoc) {
          return new FreemarkerJavaDoc((JavaDoc) obj);
        }

        return super.wrap(obj);
      }
    };
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return RubyDeploymentModule.class.getResource(template);
  }

  /**
   * Whether to force-enable the Ruby module.
   *
   * @return Whether to force-enable the Ruby module.
   */
  public boolean isForceEnable() {
    return forceEnable;
  }

  /**
   * Whether to force-enable the Ruby module.
   *
   * @param forceEnable Whether to force-enable the Ruby module.
   */
  public void setForceEnable(boolean forceEnable) {
    this.forceEnable = forceEnable;
  }

  /**
   * The label for the Ruby API.
   *
   * @return The label for the Ruby API.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for the Ruby API.
   *
   * @param label The label for the Ruby API.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * The package-to-module conversions.
   *
   * @return The package-to-module conversions.
   */
  public Map<String, String> getPackageToModuleConversions() {
    return packageToModuleConversions;
  }

  /**
   * Add a client package conversion.
   *
   * @param conversion The conversion to add.
   */
  public void addClientPackageConversion(PackageModuleConversion conversion) {
    String from = conversion.getFrom();
    String to = conversion.getTo();

    if (from == null) {
      throw new IllegalArgumentException("A 'from' attribute must be specified on a package-conversion element.");
    }

    if (to == null) {
      throw new IllegalArgumentException("A 'to' attribute must be specified on a package-conversion element.");
    }

    this.packageToModuleConversions.put(from, to);
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new RubyRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new RubyValidator();
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

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (isForceEnable()) {
      debug("Ruby module is force-enabled via the 'forceEnable' attribute in the config.");
      return false;
    }
    else if (super.isDisabled()) {
      return true;
    }
    else if (!jacksonXcAvailable) {
      debug("Ruby module is disabled because Jackson XC was not found on the Enunciate classpath.");
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToSchemas().isEmpty()) {
      debug("Ruby module is disabled because there are no schema types.");
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getRootResources().isEmpty()) {
      debug("Ruby module is disabled because there are no JAX-RS root resources.");
      return true;
    }
    else if (getModelInternal() != null && !existsAnyJsonResourceMethod(getModelInternal().getRootResources())) {
      debug("Ruby module is disabled because there are no JAX-RS root resource methods that produce or consume json.");
      return true;
    }

    return false;
  }

  /**
   * Whether any root resources exist that produce json.
   *
   * @param rootResources The root resources.
   * @return Whether any root resources exist that produce json.
   */
  protected boolean existsAnyJsonResourceMethod(List<RootResource> rootResources) {
    for (RootResource rootResource : rootResources) {
      for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
        for (String mime : resourceMethod.getProducesMime()) {
          if ("*/*".equals(mime)) {
            return true;
          }
          else if (mime.toLowerCase().contains("json")) {
            return true;
          }
        }
        for (String mime : resourceMethod.getConsumesMime()) {
          if ("*/*".equals(mime)) {
            return true;
          }
          else if (mime.toLowerCase().contains("json")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static final class ExtensionDepthComparator implements Comparator<TypeDefinition> {
    public int compare(TypeDefinition t1, TypeDefinition t2) {
      int depth1 = 0;
      int depth2 = 0;

      ClassType superClass = t1.getSuperclass();
      while (superClass != null && superClass.getDeclaration() != null && !Object.class.getName().equals(superClass.getDeclaration().getQualifiedName())) {
        depth1++;
        superClass = superClass.getDeclaration().getSuperclass();
      }

      superClass = t2.getSuperclass();
      while (superClass != null && superClass.getDeclaration() != null && !Object.class.getName().equals(superClass.getDeclaration().getQualifiedName())) {
        depth2++;
        superClass = superClass.getDeclaration().getSuperclass();
      }

      return depth1 - depth2;
    }
  }
}
