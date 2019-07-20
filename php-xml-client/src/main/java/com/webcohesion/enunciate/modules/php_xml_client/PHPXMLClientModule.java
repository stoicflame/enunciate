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
package com.webcohesion.enunciate.modules.php_xml_client;

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
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.JaxbModule;
import com.webcohesion.enunciate.modules.jaxb.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jaxb.model.Element;
import com.webcohesion.enunciate.modules.jaxb.model.ElementRef;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBCodeErrors;
import com.webcohesion.enunciate.modules.jaxb.util.FindRootElementMethod;
import com.webcohesion.enunciate.modules.jaxb.util.PrefixMethod;
import com.webcohesion.enunciate.modules.jaxb.util.ReferencedNamespacesMethod;
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
import javax.xml.bind.annotation.XmlElements;
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
public class PHPXMLClientModule extends BasicGeneratingModule implements ApiFeatureProviderModule {

  JaxbModule jaxbModule;
  JaxrsModule jaxrsModule;

  /**
   * @return "php-xml-client"
   */
  @Override
  public String getName() {
    return "php-xml-client";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new DependencySpec() {
      @Override
      public boolean accept(EnunciateModule module) {
        if (module instanceof JaxbModule) {
          jaxbModule = (JaxbModule) module;
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
        return "optional jaxb, optional jaxrs";
      }
    });
  }

  @Override
  public void call(EnunciateContext context) {
    if (this.jaxbModule == null || this.jaxbModule.getJaxbContext() == null || this.jaxbModule.getJaxbContext().getSchemas().isEmpty()) {
      info("No JAXB XML data types: PHP XML client will not be generated.");
      return;
    }

    if (usesUnmappableElements()) {
      warn("Web service API makes use of elements that cannot be handled by the PHP XML client. PHP XML client will not be generated.");
      return;
    }

    List<String> namingConflicts = JAXBCodeErrors.findConflictingAccessorNamingErrors(this.jaxbModule.getJaxbContext());
    if (namingConflicts != null && !namingConflicts.isEmpty()) {
      error("JAXB naming conflicts have been found:");
      for (String namingConflict : namingConflicts) {
        error(namingConflict);
      }
      error("These naming conflicts are often between the field and it's associated property, in which case you need to use one or two of the following strategies to avoid the conflicts:");
      error("1. Explicitly exclude one or the other.");
      error("2. Put the annotations on the property instead of the field.");
      error("3. Tell JAXB to use a different process for detecting accessors using the @XmlAccessorType annotation.");
      throw new EnunciateException("JAXB naming conflicts detected.");
    }

    Map<String, String> packageToNamespaceConversions = getPackageToNamespaceConversions();
    List<TypeDefinition> schemaTypes = new ArrayList<TypeDefinition>();
    ExtensionDepthComparator comparator = new ExtensionDepthComparator();

    EnunciateJaxbContext jaxbContext = this.jaxbModule.getJaxbContext();

    for (SchemaInfo schemaInfo : jaxbContext.getSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        String pckg = typeDefinition.getPackage().getQualifiedName().toString();
        if (!packageToNamespaceConversions.containsKey(pckg)) {
          packageToNamespaceConversions.put(pckg, packageToNamespace(pckg));
        }

        int position = Collections.binarySearch(schemaTypes, typeDefinition, comparator);
        if (position < 0) {
          position = -position - 1;
        }
        schemaTypes.add(position, typeDefinition);
      }
    }

    File srcDir = getSourceDir();
    Map<String, Object> model = new HashMap<String, Object>();

    model.put("schemaTypes", schemaTypes);
    model.put("namespaceFor", new ClientPackageForMethod(packageToNamespaceConversions, this.context));
    ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(packageToNamespaceConversions, jaxbContext);
    model.put("classnameFor", classnameFor);
    model.put("typeNameFor", new TypeNameForMethod(packageToNamespaceConversions, jaxbContext));
    model.put("simpleNameFor", new SimpleNameWithParamsMethod(classnameFor));
    model.put("phpFileName", getSourceFileName());
    model.put("findRootElement", new FindRootElementMethod(jaxbContext));
    model.put("referencedNamespaces", new ReferencedNamespacesMethod(jaxbContext));
    model.put("prefix", new PrefixMethod(jaxbContext.getNamespacePrefixes()));
    model.put("file", new FileDirective(srcDir, this.enunciate.getLogger()));
    model.put("generatedCodeLicense", this.enunciate.getConfiguration().readGeneratedCodeLicenseFile());

    Set<String> facetIncludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetIncludes());
    facetIncludes.addAll(getFacetIncludes());
    Set<String> facetExcludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetExcludes());
    facetExcludes.addAll(getFacetExcludes());
    FacetFilter facetFilter = new FacetFilter(facetIncludes, facetExcludes);

    model.put("isFacetExcluded", new IsFacetExcludedMethod(facetFilter));

    if (!isUpToDateWithSources(srcDir)) {
      debug("Generating the PHP XML data classes...");
      URL apiTemplate = isSingleFilePerClass() ? getTemplateURL("api-multiple-files.fmt") : getTemplateURL("api.fmt");
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
      info("Skipping PHP XML code generation because everything appears up-to-date.");
    }

    File packageDir = getPackageDir();
    packageDir.mkdirs();

    File bundle = new File(packageDir, getBundleFileName());
    boolean anyFiles = bundle.exists();
    if (!isUpToDateWithSources(packageDir)) {
      try {
        anyFiles = enunciate.zip(bundle, srcDir);
      }
      catch (IOException e) {
        throw new EnunciateException(e);
      }
    }

    if (anyFiles) {
      ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "php.xml.client.library", "PHP XML Client Library");
      artifactBundle.setPlatform("PHP");
      FileArtifact sourceScript = new FileArtifact(getName(), "php.xml.client", bundle);
      sourceScript.setArtifactType(ArtifactType.binaries); //binaries and sources are the same thing in php
      sourceScript.setPublic(false);
      String description = readResource("library_description.fmt", model); //read in the description from file
      artifactBundle.setDescription(description);
      artifactBundle.addArtifact(sourceScript);
      this.enunciate.addArtifact(artifactBundle);
    }
  }

  protected boolean usesUnmappableElements() {
    boolean usesUnmappableElements = false;

    if (this.jaxbModule != null && this.jaxbModule.getJaxbContext() != null && !this.jaxbModule.getJaxbContext().getSchemas().isEmpty()) {
      for (SchemaInfo schemaInfo : this.jaxbModule.getJaxbContext().getSchemas().values()) {
        for (TypeDefinition complexType : schemaInfo.getTypeDefinitions()) {
          if (!Character.isUpperCase(complexType.getClientSimpleName().charAt(0))) {
            warn("%s: PHP requires your class name to be upper-case. Please rename the class or apply the @org.codehaus.enunciate.ClientName annotation to the class.", positionOf(complexType));
            usesUnmappableElements = true;
          }

          for (Element element : complexType.getElements()) {
            if (element instanceof ElementRef && element.isElementRefs()) {
              info("%s: The PHP client library doesn't fully support the @XmlElementRefs annotation. The items in the collection will be read-only and will only be available to PHP clients in the form of a Hash. Consider redesigning using a collection of a single type.", positionOf(element));
            }
            else if (element.getAnnotation(XmlElements.class) != null) {
              info("%s: The PHP client library doesn't fully support the @XmlElements annotation. The items in the collection will be read-only and will only be available to PHP clients in the form of a Hash. Consider redesigning using a collection of a single type.", positionOf(element));
            }
          }
        }
      }
    }

    return usesUnmappableElements;
  }

  protected File getSourceDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "src");
  }

  protected File getPackageDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "build");
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
    configuration.setObjectWrapper(new PHPXMLClientObjectWrapper());
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    unhandledOutput.close();
    return unhandledOutput.toString();
  }

  protected String packageToNamespace(String pckg) {
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
          ns.append("\\");
        }
      }
      return ns.toString();
    }
  }

  /**
   * The name of the bundle file.
   *
   * @return The name of the bundle file.
   */
  protected String getBundleFileName() {
    return getSlug() + "-php.zip";
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource, Map<String, Object> model) {
    model.put("sample_resource", findExampleResourceMethod());

    URL res = PHPXMLClientModule.class.getResource(resource);
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
   * The name of the generated PHP source file.
   *
   * @return The name of the generated PHP source file.
   */
  protected String getSourceFileName() {
    return getSlug() + ".php";
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return PHPXMLClientModule.class.getResource(template);
  }

  /**
   * The label for the PHP API.
   *
   * @return The label for the PHP API.
   */
  public String getSlug() {
    return this.config.getString("[@slug]", this.enunciate.getConfiguration().getSlug() + "-" + getName());
  }

  /**
   * The package-to-namespace conversions.
   *
   * @return The package-to-namespace conversions.
   */
  public Map<String, String> getPackageToNamespaceConversions() {
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

  /**
   * Whether there should be a single file per class. Default: false (all classes are contained in a single file).
   *
   * @return Whether there should be a single file per class.
   */
  public boolean isSingleFilePerClass() {
    return this.config.getBoolean("[@singleFilePerClass]", false);
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
