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
package com.webcohesion.enunciate.modules.ruby_json_client;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.DefaultRegistrationContext;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.artifacts.ArtifactType;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.JacksonModule;
import com.webcohesion.enunciate.modules.jackson.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.util.JacksonCodeErrors;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.Jackson1Module;
import com.webcohesion.enunciate.modules.jackson1.model.util.Jackson1CodeErrors;
import com.webcohesion.enunciate.modules.jaxrs.JaxrsModule;
import com.webcohesion.enunciate.util.freemarker.ClientPackageForMethod;
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import com.webcohesion.enunciate.util.freemarker.IsFacetExcludedMethod;
import com.webcohesion.enunciate.util.freemarker.SimpleNameWithParamsMethod;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class RubyJSONClientModule extends BasicGeneratingModule implements ApiFeatureProviderModule {

  JacksonModule jacksonModule;
  Jackson1Module jackson1Module;
  JaxrsModule jaxrsModule;

  /**
   * @return "ruby-json-client"
   */
  @Override
  public String getName() {
    return "ruby-json-client";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        if (module instanceof JacksonModule) {
          jacksonModule = (JacksonModule) module;
          return true;
        }
        else if (module instanceof Jackson1Module) {
          jackson1Module = (Jackson1Module) module;
          return true;
        }
        else if (module instanceof JaxrsModule) {
          jaxrsModule = (JaxrsModule) module;
          return true;
        }

        return module instanceof ApiRegistryProviderModule;
      }

      @Override
      public boolean isFulfilled() {
        return true;
      }

      @Override
      public String toString() {
        return "optional jackson, optional jackson1, optional jaxrs";
      }
    });
  }

  @Override
  public void call(EnunciateContext context) {
    if ((this.jacksonModule == null || this.jacksonModule.getJacksonContext() == null || this.jacksonModule.getJacksonContext().getTypeDefinitions().isEmpty()) &&
      (this.jackson1Module == null || this.jackson1Module.getJacksonContext() == null || this.jackson1Module.getJacksonContext().getTypeDefinitions().isEmpty()))
    {
      info("No Jackson JSON data types: Ruby JSON client will not be generated.");
      return;
    }

    detectAccessorNamingErrors();

    if (usesUnmappableElements()) {
      warn("Web service API makes use of elements that cannot be handled by the Ruby JSON client. Ruby JSON client will not be generated.");
      return;
    }

    Map<String, String> packageToModuleConversions = getPackageToModuleConversions();
    List<DecoratedTypeElement> schemaTypes = new ArrayList<DecoratedTypeElement>();
    ExtensionDepthComparator comparator = new ExtensionDepthComparator();
    EnunciateJacksonContext jacksonContext = null;
    EnunciateJackson1Context jackson1Context = null;

    if (this.jacksonModule != null) {
      jacksonContext = this.jacksonModule.getJacksonContext();
      for (TypeDefinition typeDefinition : jacksonContext.getTypeDefinitions()) {
        String pckg = typeDefinition.getPackage().getQualifiedName().toString();
        if (!packageToModuleConversions.containsKey(pckg)) {
          packageToModuleConversions.put(pckg, packageToModule(pckg));
        }

        int position = Collections.binarySearch(schemaTypes, typeDefinition, comparator);
        if (position < 0) {
          position = -position - 1;
        }
        schemaTypes.add(position, typeDefinition);
      }
    }

    if (this.jackson1Module != null) {
      jackson1Context = this.jackson1Module.getJacksonContext();
      for (com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition typeDefinition : jackson1Context.getTypeDefinitions()) {
        String pckg = typeDefinition.getPackage().getQualifiedName().toString();
        if (!packageToModuleConversions.containsKey(pckg)) {
          packageToModuleConversions.put(pckg, packageToModule(pckg));
        }
        schemaTypes.add(typeDefinition);
      }
    }

    File srcDir = getSourceDir();
    Map<String, Object> model = new HashMap<String, Object>();

    model.put("schemaTypes", schemaTypes);

    model.put("schemaTypes", schemaTypes);
    model.put("packages2modules", packageToModuleConversions);
    model.put("moduleFor", new ClientPackageForMethod(packageToModuleConversions, this.context));
    ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(packageToModuleConversions, jacksonContext, jackson1Context);
    model.put("classnameFor", classnameFor);
    SimpleNameWithParamsMethod simpleNameFor = new SimpleNameWithParamsMethod(classnameFor);
    model.put("simpleNameFor", simpleNameFor);
    model.put("rubyFileName", getSourceFileName());
    model.put("file", new FileDirective(srcDir, this.enunciate.getLogger()));
    model.put("generatedCodeLicense", this.enunciate.getConfiguration().readGeneratedCodeLicenseFile());

    Set<String> facetIncludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetIncludes());
    facetIncludes.addAll(getFacetIncludes());
    Set<String> facetExcludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetExcludes());
    facetExcludes.addAll(getFacetExcludes());
    FacetFilter facetFilter = new FacetFilter(facetIncludes, facetExcludes);

    model.put("isFacetExcluded", new IsFacetExcludedMethod(facetFilter));

    if (!isUpToDateWithSources(srcDir)) {
      debug("Generating the Ruby JSON data classes...");
      URL apiTemplate = getTemplateURL("api.fmt");
      try {
        processTemplate(apiTemplate, model);
      }
      catch (IOException e) {
        throw new EnunciateException(e);
      }
      catch (TemplateException e) {
        throw new EnunciateException(e);
      }
    }
    else {
      info("Skipping Ruby code generation because everything appears up-to-date.");
    }

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "ruby.json.client.library", "Ruby JSON Client Library");
    artifactBundle.setPlatform("Ruby");
    FileArtifact sourceScript = new FileArtifact(getName(), "ruby.json.client", new File(srcDir, getSourceFileName()));
    sourceScript.setArtifactType(ArtifactType.binaries); //binaries and sources are the same thing in ruby
    sourceScript.setPublic(false);
    String description = readResource("library_description.fmt", model); //read in the description from file
    artifactBundle.setDescription(description);
    artifactBundle.addArtifact(sourceScript);
    this.enunciate.addArtifact(artifactBundle);
  }

  protected boolean usesUnmappableElements() {
    boolean usesUnmappableElements = false;

    if (this.jacksonModule != null && this.jacksonModule.getJacksonContext() != null) {
      for (TypeDefinition complexType : this.jacksonModule.getJacksonContext().getTypeDefinitions()) {
        if (!Character.isUpperCase(complexType.getClientSimpleName().charAt(0))) {
          warn("%s: Ruby requires your class name to be upper-case. Please rename the class or apply the @org.codehaus.enunciate.ClientName annotation to the class.", positionOf(complexType));
          usesUnmappableElements = true;
        }
      }
    }

    if (this.jackson1Module != null && this.jackson1Module.getJacksonContext() != null) {
      for (com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition complexType : this.jackson1Module.getJacksonContext().getTypeDefinitions()) {
        if (!Character.isUpperCase(complexType.getClientSimpleName().charAt(0))) {
          warn("%s: Ruby requires your class name to be upper-case. Please rename the class or apply the @org.codehaus.enunciate.ClientName annotation to the class.", positionOf(complexType));
          usesUnmappableElements = true;
        }
      }
    }

    return usesUnmappableElements;
  }

  protected void detectAccessorNamingErrors() {
    if (this.jacksonModule != null) {
      List<String> namingConflicts = JacksonCodeErrors.findConflictingAccessorNamingErrors(this.jacksonModule.getJacksonContext());
      if (namingConflicts != null && !namingConflicts.isEmpty()) {
        error("Jackson naming conflicts have been found:");
        for (String namingConflict : namingConflicts) {
          error(namingConflict);
        }
        error("These naming conflicts are often between the field and it's associated property, in which case you need to use one or both of the following strategies to avoid the conflicts:");
        error("1. Explicitly exclude one or the other.");
        error("2. Put the annotations on the property instead of the field.");
        throw new EnunciateException("Jackson naming conflicts detected.");
      }
    }

    if (this.jackson1Module != null) {
      List<String> namingConflicts = Jackson1CodeErrors.findConflictingAccessorNamingErrors(this.jackson1Module.getJacksonContext());
      if (namingConflicts != null && !namingConflicts.isEmpty()) {
        error("Jackson naming conflicts have been found:");
        for (String namingConflict : namingConflicts) {
          error(namingConflict);
        }
        error("These naming conflicts are often between the field and it's associated property, in which case you need to use one or both of the following strategies to avoid the conflicts:");
        error("1. Explicitly exclude one or the other.");
        error("2. Put the annotations on the property instead of the field.");
        throw new EnunciateException("Jackson naming conflicts detected.");
      }
    }
  }

  protected File getSourceDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "src");
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
    configuration.setObjectWrapper(new RubyJSONClientObjectWrapper());
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    unhandledOutput.close();
    return unhandledOutput.toString();
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

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource, Map<String, Object> model) {
    model.put("sample_resource", findExampleResourceMethod());

    URL res = RubyJSONClientModule.class.getResource(resource);
    try {
      return processTemplate(res, model);
    }
    catch (TemplateException e) {
      throw new EnunciateException(e);
    }
    catch (IOException e) {
      throw new EnunciateException(e);
    }
  }

  /**
   * Finds an example resource method, according to the following preference order:
   *
   * <ol>
   * <li>The first method annotated with {@link com.webcohesion.enunciate.metadata.DocumentationExample}.
   * <li>The first method with BOTH an output payload with a known XML element and an input payload with a known XML element.
   * <li>The first method with an output payload with a known XML element.
   * </ol>
   *
   * @return An example resource method, or if no good examples were found.
   */
  public Method findExampleResourceMethod() {
    Method example = null;
    List<ResourceGroup> resourceGroups = this.jaxrsModule.getJaxrsContext().getResourceGroups(new DefaultRegistrationContext(context));
    for (ResourceGroup resourceGroup : resourceGroups) {
      List<Resource> resources = resourceGroup.getResources();
      for (Resource resource : resources) {
        for (Method method : resource.getMethods()) {
          if (hasXmlResponseEntity(method)) {
            if (hasXmlRequestEntity(method)) {
              //we'll prefer one with both an output AND an input.
              return method;
            }
            else {
              //we'll prefer the first one we find with an output.
              example = example == null ? method : example;
            }
          }
        }
      }
    }

    return example;
  }

  private boolean hasXmlResponseEntity(Method method) {
    if (method.getResponseEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getResponseEntity().getMediaTypes()) {
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasXmlRequestEntity(Method method) {
    if (method.getRequestEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getRequestEntity().getMediaTypes()) {
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * The name of the generated Ruby source file.
   *
   * @return The name of the generated Ruby source file.
   */
  protected String getSourceFileName() {
    return getSlug() + ".rb";
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return RubyJSONClientModule.class.getResource(template);
  }

  /**
   * The label for the PHP API.
   *
   * @return The label for the PHP API.
   */
  public String getSlug() {
    return this.config.getString("[@slug]", this.enunciate.getConfiguration().getSlug());
  }

  /**
   * The package-to-module conversions.
   *
   * @return The package-to-module conversions.
   */
  public Map<String, String> getPackageToModuleConversions() {
    List<HierarchicalConfiguration> conversionElements = this.config.configurationsAt("package-conversions.convert");
    HashMap<String, String> conversions = new HashMap<String, String>();
    for (HierarchicalConfiguration conversionElement : conversionElements) {
      conversions.put(conversionElement.getString("[@from]"), conversionElement.getString("[@to]"));
    }
    return conversions;
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

  private static final class ExtensionDepthComparator implements Comparator<DecoratedTypeElement> {
    public int compare(DecoratedTypeElement t1, DecoratedTypeElement t2) {
      int depth1 = 0;
      int depth2 = 0;

      DecoratedTypeMirror superType = (DecoratedTypeMirror) t1.getSuperclass();
      while (superType != null && superType.isDeclared() && !Object.class.getName().equals(((TypeElement) ((DeclaredType) superType).asElement()).getQualifiedName().toString())) {
        depth1++;
        superType = (DecoratedTypeMirror) ((TypeElement) ((DeclaredType) superType).asElement()).getSuperclass();
      }

      superType = (DecoratedTypeMirror) t2.getSuperclass();
      while (superType != null && superType.isDeclared() && !Object.class.getName().equals(((TypeElement) ((DeclaredType) superType).asElement()).getQualifiedName().toString())) {
        depth2++;
        superType = (DecoratedTypeMirror) ((TypeElement) ((DeclaredType) superType).asElement()).getSuperclass();
      }

      return depth1 - depth2;
    }
  }
}
