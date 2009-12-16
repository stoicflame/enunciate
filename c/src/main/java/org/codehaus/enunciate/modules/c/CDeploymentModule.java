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

package org.codehaus.enunciate.modules.c;

import freemarker.template.*;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.freemarker.FreemarkerJavaDoc;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.template.freemarker.AccessorOverridesAnotherMethod;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.LocalElementDeclaration;
import org.codehaus.enunciate.contract.common.rest.RESTResource;
import org.codehaus.enunciate.main.ClientLibraryArtifact;
import org.codehaus.enunciate.main.NamedFileArtifact;
import org.codehaus.enunciate.main.ArtifactType;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.c.config.CRuleSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.sun.mirror.declaration.ClassDeclaration;

/**
 * <h1>C Module</h1>
 *
 * <p>The C module generates C data structures and (de)serialization functions that can be used in conjunction with <a href="http://xmlsoft.org/">libxml2</a>
 * to (de)serialize the REST resources as they are represented as XML data.</p>
 *
 * <p>The order of the C deployment module is 0, as it doesn't depend on any artifacts exported by any other module.</p>
 *
 * <ul>
 * <li><a href="#config">configuration</a></li>
 * </ul>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The C module is configured with the "c" element under the "modules" element of the enunciate configuration file. It supports the following
 * attributes:</p>
 *
 * <ul>
 * <li>The "label" attribute is the label for the C API.  This is the name by which the file will be identified (producing [label].c).
 * By default the label is the same as the Enunciate project label.</li>
 * <li>The "forceEnable" attribute is used to force-enable the C module. By default, the C module is enabled only if REST endpoints are found in the project.</li>
 * <li>The "enumConstantNamePattern" attribute defines the <a href="http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">format string</a> for
 * converting an enum constant name to a unique c-style constant name. The arguments passed to the format string are: (1) the project label (2) the namespace id
 * of the type definition (3) the name of the type definition (4) the NOT decapitalized annotation-specified client name of the type declaration
 * (5) the decapitalized annotation-specified client name of the type declaration (6) the NOT-decapitalized simple name of the type declaration
 * (7) the decapitalized simple name of the type declaration (8) the package name (9) the annotation-specified client name of the enum contant (10) the simple name of the enum constant. All tokens will
 * be "scrubbed" by replacing any non-word character with the "_" character. The default value for this pattern is "%1$S_%2$S_%3$S_%9$S".</li>
 * <li>The "typeDefinitionNamePattern" attribute defines the <a href="http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">format string</a> for
 * converting an type definition name to a unique c-style name. The arguments passed to the format string are: (1) the project label (2) the namespace id
 * of the type definition (3) the name of the type definition (4) the NOT decapitalized annotation-specified client name of the type declaration
 * (5) the decapitalized annotation-specified client name of the type declaration (6) the NOT-decapitalized simple name of the type declaration
 * (7) the decapitalized simple name of the type declaration (8) the package name. All tokens will be "scrubbed" by replacing any non-word character with the "_" character. The default value for this
 * pattern is "%1$s_%2$s_%3$s".</li> 
 * </ul>
 *
 * @author Ryan Heaton
 * @docFileName module_c.html
 */
public class CDeploymentModule extends FreemarkerDeploymentModule {

  /**
   * The pattern to scrub is any non-word character.
   */
  private static final Pattern SCRUB_PATTERN = Pattern.compile("\\W");

  private boolean forceEnable = false;
  private String label = null;
  private String typeDefinitionNamePattern = "%1$s_%2$s_%3$s";
  private String enumConstantNamePattern = "%1$S_%2$S_%3$S_%9$S";

  /**
   * @return "c"
   */
  @Override
  public String getName() {
    return "c";
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
  public void doFreemarkerGenerate() throws IOException, TemplateException, EnunciateException {
    File genDir = getGenerateDir();
    if (!enunciate.isUpToDateWithSources(genDir)) {
      EnunciateFreemarkerModel model = getModel();

      String label = getLabel() == null ? getEnunciate().getConfig() == null ? "enunciate" : getEnunciate().getConfig().getLabel() : getLabel();
      NameForTypeDefinitionMethod nameForTypeDefinition = new NameForTypeDefinitionMethod(getTypeDefinitionNamePattern(), label, model.getNamespacesToPrefixes());
      model.put("nameForTypeDefinition", nameForTypeDefinition);
      model.put("nameForEnumConstant", new NameForEnumConstantMethod(getEnumConstantNamePattern(), label, model.getNamespacesToPrefixes()));
      TreeMap<String, String> conversions = new TreeMap<String, String>();
      for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          if (typeDefinition.isEnum()) {
            conversions.put(typeDefinition.getQualifiedName(), "enum " + nameForTypeDefinition.calculateName(typeDefinition));
          }
          else {
            conversions.put(typeDefinition.getQualifiedName(), "struct " + nameForTypeDefinition.calculateName(typeDefinition));
          }
        }
      }
      ClientClassnameForMethod classnameFor = new ClientClassnameForMethod(conversions);
      model.put("classnameFor", classnameFor);
      model.put("cFileName", getSourceFileName());
      model.put("forAllAccessors", new ForAllAccessorsTransform(null));
      model.put("findRootElement", new FindRootElementMethod());
      model.put("referencedNamespaces", new ReferencedNamespacesMethod());
      model.put("prefix", new PrefixMethod());
      model.put("xmlFunctionIdentifier", new XmlFunctionIdentifierMethod());
      model.put("accessorOverridesAnother", new AccessorOverridesAnotherMethod());

      debug("Generating the C data structures and (de)serialization functions...");
      URL apiTemplate = getTemplateURL("api.fmt");
      processTemplate(apiTemplate, model);
    }
    else {
      info("Skipping C code generation because everything appears up-to-date.");
    }

    ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "c.client.library", "C Client Library");
    NamedFileArtifact sourceScript = new NamedFileArtifact(getName(), "c.client", new File(getGenerateDir(), getSourceFileName()));
    sourceScript.setArtifactType(ArtifactType.sources);
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
    RESTResource exampleResource = getModelInternal().findExampleResource();
    model.put("filename", getSourceFileName());
    String label = getLabel() == null ? getEnunciate().getConfig() == null ? "enunciate" : getEnunciate().getConfig().getLabel() : getLabel();
    NameForTypeDefinitionMethod nameForTypeDefinition = new NameForTypeDefinitionMethod(getTypeDefinitionNamePattern(), label, getModelInternal().getNamespacesToPrefixes());

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
    }

    URL res = CDeploymentModule.class.getResource(resource);
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
      label = getEnunciate().getConfig() == null ? "enunciate" : getEnunciate().getConfig().getLabel();
    }
    return label + ".c";
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
    return CDeploymentModule.class.getResource(template);
  }

  /**
   * The label for the C API.
   *
   * @return The label for the C API.
   */
  public String getLabel() {
    return label;
  }

  /**
   * The label for the C API.
   *
   * @param label The label for the C API.
   */
  public void setLabel(String label) {
    this.label = label;
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

  @Override
  public RuleSet getConfigurationRules() {
    return new CRuleSet();
  }

  @Override
  public Validator getValidator() {
    return new CValidator();
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (isForceEnable()) {
      debug("C module is force-enabled via the 'require' attribute in the configuration.");
      return false;
    }
    else if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getNamespacesToSchemas().isEmpty()) {
      debug("C module is disabled because there are no schema types.");
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getRootResources().isEmpty() && getModelInternal().getRESTEndpoints().isEmpty()) {
      debug("C module is disabled because there are no REST resources.");
      return true;
    }

    return false;
  }
}
