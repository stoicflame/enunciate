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

package org.codehaus.enunciate.modules.objc;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.ClassType;
import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.common.rest.RESTResource;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.LocalElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.main.NamedFileArtifact;
import org.codehaus.enunciate.main.ArtifactType;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.objc.config.ObjCRuleSet;
import org.codehaus.enunciate.modules.objc.config.PackageIdentifier;
import org.codehaus.enunciate.template.freemarker.AccessorOverridesAnotherMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <h1>Objective C Module</h1>
 *
 * <p>The Objective C module generates Objective C classes and (de)serialization functions that can be used in conjunction with <a href="http://xmlsoft.org/">libxml2</a>
 * to (de)serialize the REST resources as they are represented as XML data.</p>
 *
 * <p>The order of the Objective C deployment module is 0, as it doesn't depend on any artifacts exported by any other module.</p>
 *
 * <ul>
 * <li><a href="#config">configuration</a></li>
 * </ul>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The Objective C module is configured with the "obj-c" element under the "modules" element of the enunciate configuration file. It supports the following
 * attributes:</p>
 *
 * <ul>
 * <li>The "label" attribute is the label for the Objective C API.  This is the name by which the file will be identified (producing [label].m and [label].h).
 * By default the label is the same as the Enunciate project label.</li>
 * <li>The "forceEnable" attribute is used to force-enable the Objective C module. By default, the Objective C module is enabled only if REST endpoints are found in the project.</li>
 * <li>The "enumConstantNamePattern" attribute defines the <a href="http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">format string</a> for
 * converting an enum constant name to a unique c-style constant name. The arguments passed to the format string are: (1) the project label (2) the namespace id
 * of the type definition (3) the name of the type definition (4) the NOT decapitalized annotation-specified client name of the type declaration
 * (5) the decapitalized annotation-specified client name of the type declaration (6) the NOT-decapitalized simple name of the type declaration
 * (7) the decapitalized simple name of the type declaration (8) the package identifier (9) the annotation-specified client name of the enum contant
 * (10) the simple name of the enum constant. All tokens will be "scrubbed" by replacing any non-word character with the "_" character.
 * The default value for this pattern is "%1$S_%2$S_%3$S_%9$S".</li>
 * <li>The "typeDefinitionNamePattern" attribute defines the <a href="http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">format string</a> for
 * converting an type definition name to a unique c-style name. The arguments passed to the format string are: (1) the project label (2) the namespace id
 * of the type definition (3) the name of the type definition (4) the NOT decapitalized annotation-specified client name of the type declaration
 * (5) the decapitalized annotation-specified client name of the type declaration (6) the NOT-decapitalized simple name of the type declaration
 * (7) the decapitalized simple name of the type declaration (8) the package identifier. All tokens will be "scrubbed" by replacing any non-word character
 * with the "_" character. The default value for this pattern is "%1$S%2$S%4$s".</li>
 * <li>The "packageIdentifierPattern" attribute defines the <a href="http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">format string</a> for
 * creating a unique package identifier for a given package. The arguments passed to the format string will be each subpackage. So, for package "org.codehaus.enunciate.samples.c",
 * The arguments are (1) org (2) codehaus (3) enunciate (4) samples (5) c. The default package identifier is the package name. The package identifier
 * is in turn passed as an argument to the "enumConstantNamePattern" and to the "typeDefinitionNamePattern".</li>
 * <li>The 'translateIdTo' attribute specifies what to use as the name of an accessor when in Java it's named 'id' (which is a keyword in Objective C).</li>
 * </ul>
 *
 * <p>In addition to the attributes specified above, the Objective C module configuration supports an arbitrary number of "package" child elements, used to
 * explicitly assign package identifiers to each package. The "package" child element supports a "name" attribute (used to name the package) and an "identifier" attribute.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_obj_c.html
 */
public class ObjCDeploymentModule extends FreemarkerDeploymentModule {

  /**
   * The pattern to scrub is any non-word character.
   */
  private static final Pattern SCRUB_PATTERN = Pattern.compile("\\W");

  private boolean forceEnable = false;
  private String label = null;
  private String packageIdentifierPattern = null;
  private String typeDefinitionNamePattern = "%1$S%2$S%4$s";
  private String enumConstantNamePattern = "%1$S_%2$S_%3$S_%9$S";
  private final Map<String, String> packageIdentifiers = new HashMap<String, String>();
  private String translateIdTo = "identifier";

  /**
   * @return "obj-c"
   */
  @Override
  public String getName() {
    return "obj-c";
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
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled() && (this.packageIdentifierPattern != null)) {
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          String pckg = typeDefinition.getPackage().getQualifiedName();
          if (!this.packageIdentifiers.containsKey(pckg)) {
            try {
              this.packageIdentifiers.put(pckg, String.format(this.packageIdentifierPattern, pckg.split("\\.", 9)));
            }
            catch (IllegalFormatException e) {
              warn("Unable to format package %s with format pattern %s (%s)", pckg, this.packageIdentifierPattern, e.getMessage());
            }
          }
        }
      }
    }
  }
  @Override
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    File genDir = getGenerateDir();
    String label = getLabel() == null ? getEnunciate().getConfig() == null ? "enunciate" : getEnunciate().getConfig().getLabel() : getLabel();
    if (!enunciate.isUpToDateWithSources(genDir)) {
      EnunciateFreemarkerModel model = getModel();
      TreeMap<String, String> translations = new TreeMap<String, String>();
      translations.put("id", this.translateIdTo);
      model.put("clientSimpleName", new ClientSimpleNameMethod(translations));

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

      NameForTypeDefinitionMethod nameForTypeDefinition = new NameForTypeDefinitionMethod(getTypeDefinitionNamePattern(), label, model.getNamespacesToPrefixes(), this.packageIdentifiers);
      model.put("nameForTypeDefinition", nameForTypeDefinition);
      model.put("nameForEnumConstant", new NameForEnumConstantMethod(getEnumConstantNamePattern(), label, model.getNamespacesToPrefixes(), this.packageIdentifiers));
      TreeMap<String, String> conversions = new TreeMap<String, String>();
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          if (typeDefinition.isEnum()) {
            conversions.put(typeDefinition.getQualifiedName(), "enum " + nameForTypeDefinition.calculateName(typeDefinition));
          }
          else {
            conversions.put(typeDefinition.getQualifiedName(), (String) nameForTypeDefinition.calculateName(typeDefinition));
          }
        }
      }
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
      model.put("classnameFor", classnameFor);
      model.put("functionIdentifierFor", new FunctionIdentifierForMethod(nameForTypeDefinition));
      model.put("objcBaseName", label);
      model.put("findRootElement", new FindRootElementMethod());
      model.put("referencedNamespaces", new ReferencedNamespacesMethod());
      model.put("prefix", new PrefixMethod());
      model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());

      debug("Generating the C data structures and (de)serialization functions...");
      URL apiTemplate = getTemplateURL("api.fmt");
      processTemplate(apiTemplate, model);
    }
    else {
      info("Skipping C code generation because everything appears up-to-date.");
    }

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "objc.client.library", "Objective C Client Library");
    NamedFileArtifact sourceHeader = new NamedFileArtifact(getName(), "objc.client.h", new File(getGenerateDir(), label + ".h"));
    sourceHeader.setPublic(false);
    sourceHeader.setArtifactType(ArtifactType.sources);
    NamedFileArtifact sourceImpl = new NamedFileArtifact(getName(), "objc.client.m", new File(getGenerateDir(), label + ".m"));
    sourceImpl.setPublic(false);
    sourceImpl.setArtifactType(ArtifactType.sources);
    String description = readResource("library_description.fmt"); //read in the description from file
    artifactBundle.setDescription(description);
    artifactBundle.addArtifact(sourceHeader);
    artifactBundle.addArtifact(sourceImpl);
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
    RESTResource exampleResource = getModelInternal().findExampleResource();
    String label = getLabel() == null ? getEnunciate().getConfig() == null ? "enunciate" : getEnunciate().getConfig().getLabel() : getLabel();
    model.put("label", label);
    NameForTypeDefinitionMethod nameForTypeDefinition = new NameForTypeDefinitionMethod(getTypeDefinitionNamePattern(), label, getModelInternal().getNamespacesToPrefixes(), this.packageIdentifiers);

    if (exampleResource != null) {
      if (exampleResource.getInputPayload() != null && exampleResource.getInputPayload().getXmlElement() != null) {
        ElementDeclaration el = exampleResource.getInputPayload().getXmlElement();
        TypeDefinition typeDefinition = null;
        if (el instanceof RootElementDeclaration) {
          typeDefinition = getModelInternal().findTypeDefinition((RootElementDeclaration) el);
        }
        else if (el instanceof LocalElementDeclaration && ((LocalElementDeclaration) el).getElementTypeDeclaration() instanceof ClassDeclaration) {
          typeDefinition = getModelInternal().findTypeDefinition((ClassDeclaration) ((LocalElementDeclaration) el).getElementTypeDeclaration());
        }

        if (typeDefinition != null) {
          model.put("input_element_name", nameForTypeDefinition.calculateName(typeDefinition));
        }
      }

      if (exampleResource.getOutputPayload() != null && exampleResource.getOutputPayload().getXmlElement() != null) {
        ElementDeclaration el = exampleResource.getOutputPayload().getXmlElement();
        TypeDefinition typeDefinition = null;
        if (el instanceof RootElementDeclaration) {
          typeDefinition = getModelInternal().findTypeDefinition((RootElementDeclaration) el);
        }
        else if (el instanceof LocalElementDeclaration && ((LocalElementDeclaration) el).getElementTypeDeclaration() instanceof ClassDeclaration) {
          typeDefinition = getModelInternal().findTypeDefinition((ClassDeclaration) ((LocalElementDeclaration) el).getElementTypeDeclaration());
        }

        if (typeDefinition != null) {
          model.put("output_element_name", nameForTypeDefinition.calculateName(typeDefinition));
        }
      }

      model.put("resource_url", exampleResource.getPath());
      model.put("resource_method", exampleResource.getSupportedOperations() == null || exampleResource.getSupportedOperations().isEmpty() ? "GET" : exampleResource.getSupportedOperations().iterator().next());
    }

    URL res = ObjCDeploymentModule.class.getResource(resource);
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
    return ObjCDeploymentModule.class.getResource(template);
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
  public Map<String, String> getPackageIdentifiers() {
    return packageIdentifiers;
  }

  /**
   * Add a client package conversion.
   *
   * @param conversion The conversion to add.
   */
  public void addPackageIdentifier(PackageIdentifier conversion) {
    String name = conversion.getName();
    String identifier = conversion.getIdentifier();

    if (name == null) {
      throw new IllegalArgumentException("A 'name' attribute must be specified on a 'package' element.");
    }

    if (identifier == null) {
      throw new IllegalArgumentException("An 'identifer' attribute must be specified on 'package' element.");
    }

    this.packageIdentifiers.put(name, identifier);
  }

  /**
   * The format string creating a package identifier from a package name.
   *
   * @return The format string creating a package identifier from a package name.
   */
  public String getPackageIdentifierPattern() {
    return packageIdentifierPattern;
  }

  /**
   * The format string creating a package identifier from a package name.
   *
   * @param packageIdentifierPattern The format string creating a package identifier from a package name.
   */
  public void setPackageIdentifierPattern(String packageIdentifierPattern) {
    this.packageIdentifierPattern = packageIdentifierPattern;
  }

  /**
   * The pattern for converting a type definition to a unique C-style type name.
   *
   * @return The pattern for converting a type definition to a unique C-style type name.
   */
  public String getTypeDefinitionNamePattern() {
    return typeDefinitionNamePattern;
  }

  /**
   * The pattern for converting a type definition to a unique C-style type name.
   *
   * @param typeDefinitionNamePattern The pattern for converting a type definition to a unique C-style type name.
   */
  public void setTypeDefinitionNamePattern(String typeDefinitionNamePattern) {
    this.typeDefinitionNamePattern = typeDefinitionNamePattern;
  }

  /**
   * The pattern for converting an enum constant to a unique C-style type name.
   *
   * @return The pattern for converting an enum constant to a unique C-style type name.
   */
  public String getEnumConstantNamePattern() {
    return enumConstantNamePattern;
  }

  /**
   * The pattern for converting an enum constant to a unique C-style type name.
   *
   * @param enumConstantNamePattern The pattern for converting an enum constant to a unique C-style type name.
   */
  public void setEnumConstantNamePattern(String enumConstantNamePattern) {
    this.enumConstantNamePattern = enumConstantNamePattern;
  }

  /**
   * Whether to require this module (force-enable it).
   *
   * @return Whether to require this module (force-enable it).
   */
  public boolean isForceEnable() {
    return forceEnable;
  }

  /**
   * Whether to require this module (force-enable it).
   *
   * @param forceEnable Whether to require this module (force-enable it).
   */
  public void setForceEnable(boolean forceEnable) {
    this.forceEnable = forceEnable;
  }

  /**
   * What to translate 'id' to when writing out objective-c code.
   *
   * @return What to translate 'id' to when writing out objective-c code.
   */
  public String getTranslateIdTo() {
    return translateIdTo;
  }

  /**
   * What to translate 'id' to when writing out objective-c code.
   *
   * @param translateIdTo What to translate 'id' to when writing out objective-c code.
   */
  public void setTranslateIdTo(String translateIdTo) {
    this.translateIdTo = translateIdTo;
  }

  @Override
  public RuleSet getConfigurationRules() {
    return new ObjCRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new ObjCValidator(this.translateIdTo);
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (isForceEnable()) {
      debug("Objective C module is force-enabled via the 'require' attribute in the configuration.");
      return false;
    }
    else if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToSchemas().isEmpty()) {
      debug("Objective C module is disabled because there are no schema types.");
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getRootResources().isEmpty()) {
      debug("Objective C module is disabled because there are no REST resources.");
      return true;
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
