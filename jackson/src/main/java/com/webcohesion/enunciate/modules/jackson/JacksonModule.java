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
package com.webcohesion.enunciate.modules.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jackson.model.types.KnownJsonType;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class JacksonModule extends BasicProviderModule implements TypeDetectingModule, MediaTypeDefinitionModule, ApiRegistryProviderModule, ApiFeatureProviderModule {

  private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
  private boolean jacksonDetected = false;
  private boolean jaxbSupportDetected = false;
  private EnunciateJacksonContext jacksonContext;

  @Override
  public String getName() {
    return "jackson";
  }

  public boolean isHonorJaxbAnnotations() {
    return this.config.getBoolean("[@honorJaxb]", this.jaxbSupportDetected);
  }

  public boolean isCollapseTypeHierarchy() {
    return this.config.getBoolean("[@collapse-type-hierarchy]", false);
  }

  public KnownJsonType getDateFormat() {
    String dateFormatString = this.config.getString("[@dateFormat]", KnownJsonType.WHOLE_NUMBER.name());
    return KnownJsonType.valueOf(dateFormatString.toUpperCase());
  }

  @Override
  public ApiRegistry getApiRegistry() {
    return new JacksonApiRegistry(this.jacksonContext);
  }

  @Override
  public boolean isEnabled() {
    return !this.config.getBoolean("[@disabled]", !jacksonDetected);
  }

  public boolean isDisableExamples() {
    return this.config.getBoolean("[@disableExamples]", false);
  }

  public EnunciateJacksonContext getJacksonContext() {
    return jacksonContext;
  }

  public boolean isJacksonDetected() {
    return jacksonDetected;
  }

  @Override
  public void call(EnunciateContext context) {
    for (EnunciateModule module : this.enunciate.getModules()) {
      if ("jackson1".equals(module.getName()) && module.isEnabled()) {
        error("");
        error("Both Jackson 1 and Jackson 2 Enunciate modules are enabled.");
        error("This may cause duplicated and/or inconsistent JSON documentation.");
        error("It is recommended that you disable one of the modules by either");
        error("pruning your classpath of one of the versions of the Jackson");
        error("library, or by explicitly disabling one of the modules in the");
        error("Enunciate configuration file. For more information, see");
        error("https://github.com/stoicflame/enunciate/wiki/Two-JSON-Sections");
        error("");
      }
    }

    this.jacksonContext = new EnunciateJacksonContext(context, isHonorJaxbAnnotations(), getDateFormat(), isCollapseTypeHierarchy(), getMixins(), isDisableExamples());
    DataTypeDetectionStrategy detectionStrategy = getDataTypeDetectionStrategy();
    switch (detectionStrategy) {
      case aggressive:
        for (Element declaration : context.getApiElements()) {
          addPotentialJacksonElement(declaration, new LinkedList<Element>());
        }
        break;
      case local:
        for (Element declaration : context.getLocalApiElements()) {
          addPotentialJacksonElement(declaration, new LinkedList<Element>());
        }
        //no break, add explicit includes:
      default:
        if (context.hasExplicitIncludes()) { //if we're not aggressive, we only want to add the api elements if they've been explicitly included
          for (Element declaration : context.getApiElements()) {
            addPotentialJacksonElement(declaration, new LinkedList<Element>());
          }
        }
    }
  }

  public Map<String, String> getMixins() {
    HashMap<String, String> mixins = new HashMap<String, String>();
    List<HierarchicalConfiguration> mixinElements = this.config.configurationsAt("mixin");
    for (HierarchicalConfiguration mixinElement : mixinElements) {
      mixins.put(mixinElement.getString("[@target]", ""), mixinElement.getString("[@source]", ""));
    }
    return mixins;
  }

  public DataTypeDetectionStrategy getDataTypeDetectionStrategy() {
    String dataTypeDetection = this.config.getString("[@datatype-detection]", null);

    if (dataTypeDetection != null) {
      try {
        return DataTypeDetectionStrategy.valueOf(dataTypeDetection);
      }
      catch (IllegalArgumentException e) {
        //fall through...
      }
    }

    return this.defaultDataTypeDetectionStrategy == null ? DataTypeDetectionStrategy.local : this.defaultDataTypeDetectionStrategy;
  }

  @Override
  public void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy) {
    this.defaultDataTypeDetectionStrategy = strategy;
  }

  @Override
  public void addDataTypeDefinitions(TypeMirror type, Set<String> declaredMediaTypes, LinkedList<Element> contextStack) {
    boolean jsonApplies = false;
    for (String mediaType : declaredMediaTypes) {
      if ("*/*".equals(mediaType) || "text/*".equals(mediaType) || "application/*".equals(mediaType) || "application/json".equals(mediaType) || mediaType.endsWith("+json")) {
        jsonApplies = true;
        break;
      }
    }

    if (jsonApplies) {
      type = this.jacksonContext.resolveSyntheticType((DecoratedTypeMirror) type);
      this.jacksonContext.addReferencedTypeDefinitions(type, contextStack);
    }
    else {
      debug("Element %s is NOT to be added as a Jackson data type because %s doesn't seem to include JSON.", type, declaredMediaTypes);
    }
  }

  protected void addPotentialJacksonElement(Element declaration, LinkedList<Element> contextStack) {
    if (declaration instanceof TypeElement) {
      if (!this.jacksonContext.isKnownTypeDefinition((TypeElement) declaration) && isExplicitTypeDefinition(declaration, this.jacksonContext.isHonorJaxb())) {
        this.jacksonContext.add(this.jacksonContext.createTypeDefinition((TypeElement) declaration), contextStack);
      }
    }
  }

  protected boolean isExplicitTypeDefinition(Element declaration, boolean honorJaxb) {
    if (declaration.getKind() != ElementKind.CLASS && declaration.getKind() != ElementKind.ENUM) {
      debug("%s isn't a potential Jackson type because it's not a class or an enum.", declaration);
      return false;
    }

    PackageElement pckg = this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration);
    if ((pckg != null) && (pckg.getAnnotation(Ignore.class) != null)) {
      debug("%s isn't a potential Jackson type because its package is annotated as to be ignored.", declaration);
      return false;
    }

    if (isThrowable(declaration)) {
      debug("%s isn't a potential Jackson type because it's an instance of java.lang.Throwable.", declaration);
      return false;
    }

    List<? extends AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
    boolean explicitXMLTypeOrElement = false;
    for (AnnotationMirror mirror : annotationMirrors) {
      Element annotationDeclaration = mirror.getAnnotationType().asElement();
      if (annotationDeclaration != null) {
        String fqn = annotationDeclaration instanceof TypeElement ? ((TypeElement)annotationDeclaration).getQualifiedName().toString() : "";
        //exclude all XmlTransient types and all jaxws types.
        if (JsonIgnore.class.getName().equals(fqn)
          || fqn.startsWith("javax.xml.ws")
          || fqn.startsWith("javax.ws.rs")
          || fqn.startsWith("javax.jws")) {
          debug("%s isn't a potential Jackson type because of annotation %s.", declaration, fqn);
          return false;
        }
        else {
          if (honorJaxb) {
            if (XmlTransient.class.getName().equals(fqn)) {
              debug("%s isn't a potential Jackson type because of annotation %s.", declaration, fqn);
              return false;
            }

            if ((XmlType.class.getName().equals(fqn)) || (XmlRootElement.class.getName().equals(fqn))) {
              debug("%s will be considered a Jackson type because we're honoring the %s annotation.", declaration, fqn);
              explicitXMLTypeOrElement = true;
            }
          }

          explicitXMLTypeOrElement = explicitXMLTypeOrElement || isJacksonSerializationAnnotation(fqn);
        }
      }

      if (explicitXMLTypeOrElement) {
        break;
      }
    }

    return explicitXMLTypeOrElement;
  }

  /**
   * Whether the specified declaration is throwable.
   *
   * @param declaration The declaration to determine whether it is throwable.
   * @return Whether the specified declaration is throwable.
   */
  protected boolean isThrowable(Element declaration) {
    return declaration.getKind() == ElementKind.CLASS && ((DecoratedTypeMirror) declaration.asType()).isInstanceOf(Throwable.class);
  }

  @Override
  public boolean typeDetected(Object type, MetadataAdapter metadata) {
    String classname = metadata.getClassName(type);
    this.jacksonDetected |= ObjectMapper.class.getName().equals(classname);
    this.jaxbSupportDetected |= "com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector".equals(classname);

    if (classname.startsWith("com.fasterxml.jackson")) {
      //don't accept jackson system specific types
      return false;
    }

    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String fqn : classAnnotations) {
        if (isJacksonSerializationAnnotation(fqn)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean isJacksonSerializationAnnotation(String fqn) {
    return !JacksonAnnotation.class.getName().equals(fqn) && (JsonSerialize.class.getName().equals(fqn) || fqn.startsWith(JsonFormat.class.getPackage().getName()));
  }
}
