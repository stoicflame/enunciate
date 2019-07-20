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
package com.webcohesion.enunciate.modules.c_client;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.DefaultRegistrationContext;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.artifacts.ArtifactType;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxb.JaxbModule;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxb.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jaxb.model.Attribute;
import com.webcohesion.enunciate.modules.jaxb.model.Element;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBCodeErrors;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;
import com.webcohesion.enunciate.modules.jaxb.util.AccessorOverridesAnotherMethod;
import com.webcohesion.enunciate.modules.jaxb.util.FindRootElementMethod;
import com.webcohesion.enunciate.modules.jaxb.util.ReferencedNamespacesMethod;
import com.webcohesion.enunciate.modules.jaxrs.JaxrsModule;
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import com.webcohesion.enunciate.util.freemarker.IsFacetExcludedMethod;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Ryan Heaton
 */
public class CXMLClientModule extends BasicGeneratingModule implements ApiFeatureProviderModule {

  /**
   * The pattern to scrub is any non-word character.
   */
  private static final Pattern SCRUB_PATTERN = Pattern.compile("\\W");

  JaxbModule jaxbModule;
  JaxrsModule jaxrsModule;

  /**
   * @return "c-xml-client"
   */
  @Override
  public String getName() {
    return "c-xml-client";
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

  /**
   * Scrub a C identifier (removing any illegal characters, etc.).
   *
   * @param identifier The identifier.
   * @return The identifier.
   */
  public static String scrubIdentifier(String identifier) {
    return identifier == null ? null : SCRUB_PATTERN.matcher(identifier).replaceAll("_");
  }

  @Override
  public void call(EnunciateContext context) {
    if (this.jaxbModule == null || this.jaxbModule.getJaxbContext() == null || this.jaxbModule.getJaxbContext().getSchemas().isEmpty()) {
      info("No JAXB XML data types: C XML client will not be generated.");
      return;
    }

    if (usesUnmappableElements()) {
      warn("Web service API makes use of elements that cannot be handled by the C XML client. C XML client will not be generated.");
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

    File srcDir = getSourceDir();
    srcDir.mkdirs();

    Map<String, Object> model = new HashMap<String, Object>();

    String slug = getSlug();
    Map<String, String> ns2prefix = this.jaxbModule.getJaxbContext().getNamespacePrefixes();
    NameForTypeDefinitionMethod nameForTypeDefinition = new NameForTypeDefinitionMethod(getTypeDefinitionNamePattern(), slug, ns2prefix);
    model.put("nameForTypeDefinition", nameForTypeDefinition);
    model.put("nameForEnumConstant", new NameForEnumConstantMethod(getEnumConstantNamePattern(), slug, ns2prefix));
    TreeMap<String, String> conversions = new TreeMap<String, String>();
    for (SchemaInfo schemaInfo : this.jaxbModule.getJaxbContext().getSchemas().values()) {
      for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
        if (typeDefinition.isEnum()) {
          conversions.put(typeDefinition.getQualifiedName().toString(), "enum " + nameForTypeDefinition.calculateName(typeDefinition));
        }
        else {
          conversions.put(typeDefinition.getQualifiedName().toString(), "struct " + nameForTypeDefinition.calculateName(typeDefinition));
        }
      }
    }
    ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions, this.jaxbModule.getJaxbContext());
    model.put("classnameFor", classnameFor);
    String sourceFileName = getSourceFileName(slug);
    model.put("cFileName", sourceFileName);
    model.put("separateCommonCode", isSeparateCommonCode());
    model.put("findRootElement", new FindRootElementMethod(this.jaxbModule.getJaxbContext()));
    model.put("referencedNamespaces", new ReferencedNamespacesMethod(this.jaxbModule.getJaxbContext()));
    model.put("prefix", new PrefixMethod(ns2prefix));
    model.put("xmlFunctionIdentifier", new XmlFunctionIdentifierMethod(ns2prefix));
    model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());
    model.put("filename", sourceFileName);
    model.put("file", new FileDirective(srcDir, this.enunciate.getLogger()));
    model.put("schemas", this.jaxbModule.getJaxbContext().getSchemas().values());
    model.put("generatedCodeLicense", this.enunciate.getConfiguration().readGeneratedCodeLicenseFile());

    Set<String> facetIncludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetIncludes());
    facetIncludes.addAll(getFacetIncludes());
    Set<String> facetExcludes = new TreeSet<String>(this.enunciate.getConfiguration().getFacetExcludes());
    facetExcludes.addAll(getFacetExcludes());
    FacetFilter facetFilter = new FacetFilter(facetIncludes, facetExcludes);

    model.put("isFacetExcluded", new IsFacetExcludedMethod(facetFilter));

    if (!isUpToDateWithSources(srcDir)) {
      debug("Generating the C data structures and (de)serialization functions...");
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
      info("Skipping C code generation because everything appears up-to-date.");
    }

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "c.client.library", "C Client Library");
    FileArtifact sourceScript = new FileArtifact(getName(), "c.client", new File(srcDir, sourceFileName));
    sourceScript.setArtifactType(ArtifactType.sources);
    sourceScript.setPublic(false);
    String description = readResource("library_description.fmt", model, nameForTypeDefinition); //read in the description from file
    artifactBundle.setDescription(description);
    artifactBundle.addArtifact(sourceScript);
    if (isSeparateCommonCode()) {
      FileArtifact commonSourceHeader = new FileArtifact(getName(), "c.common.client", new File(srcDir, "enunciate-common.c"));
      commonSourceHeader.setPublic(false);
      commonSourceHeader.setArtifactType(ArtifactType.sources);
      commonSourceHeader.setDescription("Common code needed for all projects.");
      artifactBundle.addArtifact(commonSourceHeader);
    }

    this.enunciate.addArtifact(artifactBundle);
  }

  protected boolean usesUnmappableElements() {
    boolean usesUnmappableElements = false;

    if (this.jaxbModule != null && this.jaxbModule.getJaxbContext() != null && !this.jaxbModule.getJaxbContext().getSchemas().isEmpty()) {
      for (SchemaInfo schemaInfo : this.jaxbModule.getJaxbContext().getSchemas().values()) {
        for (TypeDefinition complexType : schemaInfo.getTypeDefinitions()) {
          for (Attribute attribute : complexType.getAttributes()) {
            if (attribute.isXmlList()) {
              info("%s: The C client code won't serialize xml lists as an array, instead passing the list as a string that will need to be parsed. This may cause confusion to C consumers.", positionOf(attribute));
            }

            if (attribute.isCollectionType() && attribute.isBinaryData()) {
              warn("%s: The C client code doesn't support a collection of items that are binary data. You'll have to define separate accessors for each item or disable the C module.", positionOf(attribute));
              usesUnmappableElements = true;
            }
          }

          if (complexType.getValue() != null) {
            if (complexType.getValue().isXmlList()) {
              info("%s: The C client code won't serialize xml lists as an array, instead passing the list as a string that will need to be parsed. This may cause confusion to C consumers.", positionOf(complexType.getValue()));
            }

            if (complexType.getValue().isCollectionType() && complexType.getValue().isBinaryData()) {
              warn("%s: The C client code doesn't support a collection of items that are binary data.", positionOf(complexType.getValue()));
              usesUnmappableElements = true;
            }
          }

          for (Element element : complexType.getElements()) {
            if (element.isXmlList()) {
              info("%s: The C client code won't serialize xml lists as an array, instead passing the list as a string that will need to be parsed. This may cause confusion to C consumers.", positionOf(element));
            }

            if (element.getAccessorType() instanceof MapType && !element.isAdapted()) {
              warn("%s: The C client doesn't have a built-in way of serializing a Map. Use @XmlJavaTypeAdapter to supply your own adapter for the Map.", positionOf(element));
              usesUnmappableElements = true;
            }

            if (element.isCollectionType()) {
              if (element.getChoices().size() > 1) {
                info("%s: The C client code doesn't fully support multiple choices for a collection. It has to separate each choice into its own array. " +
                       "This makes the C API a bit awkward to use and makes it impossible to preserve the order of the collection. If order is relevant, consider breaking out " +
                       "the choices into their own collection or otherwise refactoring the API.", positionOf(element));
              }

              if (element.isBinaryData()) {
                warn("%s: The C client code doesn't support a collection of items that are binary data.", positionOf(element));
                usesUnmappableElements = true;
              }

              for (Element choice : element.getChoices()) {
                if (choice.isNillable()) {
                  info("%s: The C client code doesn't support nillable items in a collection (the nil items will be skipped). This may cause confusion to C consumers.", positionOf(choice));
                }
              }
            }
          }
        }
      }
    }

    return usesUnmappableElements;
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
    configuration.setObjectWrapper(new CXMLClientObjectWrapper());
    Template template = configuration.getTemplate(templateURL.toString());
    StringWriter unhandledOutput = new StringWriter();
    template.process(model, unhandledOutput);
    unhandledOutput.close();
    return unhandledOutput.toString();
  }

  /**
   * Reads a resource into string form.
   *
   * @param resource The resource to read.
   * @return The string form of the resource.
   */
  protected String readResource(String resource, Map<String, Object> model, NameForTypeDefinitionMethod nameForTypeDefinition) {
    Method exampleResource = findExampleResourceMethod();

    if (exampleResource != null) {
      TypeDefinition typeDefinition = findRequestElement(exampleResource);
      if (typeDefinition != null) {
        model.put("input_element_name", nameForTypeDefinition.calculateName(typeDefinition));
      }

      typeDefinition = findResponseElement(exampleResource);
      if (typeDefinition != null) {
        model.put("output_element_name", nameForTypeDefinition.calculateName(typeDefinition));
      }
    }

    URL res = CXMLClientModule.class.getResource(resource);
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

  private TypeDefinition findRequestElement(Method exampleResource) {
    if (exampleResource.getRequestEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : exampleResource.getRequestEntity().getMediaTypes()) {
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
          DataTypeReference dataType = mediaTypeDescriptor.getDataType();
          if (dataType instanceof DataTypeReferenceImpl) {
            XmlType xmlType = ((DataTypeReferenceImpl) dataType).getXmlType();
            if (xmlType instanceof XmlClassType) {
              return ((XmlClassType) xmlType).getTypeDefinition();
            }
          }
        }
      }
    }
    return null;
  }

  private TypeDefinition findResponseElement(Method exampleResource) {
    if (exampleResource.getResponseEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : exampleResource.getResponseEntity().getMediaTypes()) {
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaTypeDescriptor.getSyntax())) {
          DataTypeReference dataType = mediaTypeDescriptor.getDataType();
          if (dataType instanceof DataTypeReferenceImpl) {
            XmlType xmlType = ((DataTypeReferenceImpl) dataType).getXmlType();
            if (xmlType instanceof XmlClassType) {
              return ((XmlClassType) xmlType).getTypeDefinition();
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Finds an example resource method, according to the following preference order:
   *
   * <ol>
   * <li>The first method annotated with {@link DocumentationExample}.
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

  protected String getSourceFileName(String label) {
    return label + ".c";
  }

  protected File getSourceDir() {
    return new File(new File(this.enunciate.getBuildDir(), getName()), "src");
  }

  /**
   * Get a template URL for the template of the given name.
   *
   * @param template The specified template.
   * @return The URL to the specified template.
   */
  protected URL getTemplateURL(String template) {
    return CXMLClientModule.class.getResource(template);
  }

  /**
   * The slug for the C API.
   *
   * @return The slug for the C API.
   */
  public String getSlug() {
    return this.config.getString("[@slug]", this.enunciate.getConfiguration().getSlug());
  }

  /**
   * The pattern for converting a type definition to a unique C-style type name.
   *
   * @return The pattern for converting a type definition to a unique C-style type name.
   */
  public String getTypeDefinitionNamePattern() {
    return this.config.getString("[@typeDefinitionNamePattern]", "%1$s_%2$s_%3$s");
  }

  /**
   * The pattern for converting an enum constant to a unique C-style type name.
   *
   * @return The pattern for converting an enum constant to a unique C-style type name.
   */
  public String getEnumConstantNamePattern() {
    return this.config.getString("[@enumConstantNamePattern]", "%1$S_%2$S_%3$S_%9$S");
  }


  /**
   * Whether to separate the common code from the project-specific code.
   *
   * @return Whether to separate the common code from the project-specific code.
   */
  public boolean isSeparateCommonCode() {
    return this.config.getBoolean("[@separateCommonCode]", true);
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
