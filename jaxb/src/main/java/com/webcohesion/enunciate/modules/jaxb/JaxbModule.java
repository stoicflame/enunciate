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
package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxb.model.Registry;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class JaxbModule extends BasicProviderModule implements TypeDetectingModule, MediaTypeDefinitionModule, ApiRegistryProviderModule, ApiFeatureProviderModule {

  private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
  private EnunciateJaxbContext jaxbContext;
  private ApiRegistry apiRegistry;
  static final String NAME = "jaxb";

  @Override
  public String getName() {
    return NAME;
  }

  public EnunciateJaxbContext getJaxbContext() {
    return jaxbContext;
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

  public boolean isDisableExamples() {
    return this.config.getBoolean("[@disableExamples]", false);
  }

  @Override
  public void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy) {
    this.defaultDataTypeDetectionStrategy = strategy;
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  @Override
  public void addDataTypeDefinitions(TypeMirror type, Set<String> declaredMediaTypes, LinkedList<Element> contextStack) {
    boolean jaxbApplies = false;
    for (String mediaType : declaredMediaTypes) {
      if ("*/*".equals(mediaType) || "text/*".equals(mediaType) || "application/*".equals(mediaType) || "text/xml".equals(mediaType) || "application/xml".equals(mediaType) || mediaType.endsWith("+xml")) {
        jaxbApplies = true;
        break;
      }
    }

    if (jaxbApplies) {
      boolean wasEmpty = this.jaxbContext.isEmpty();
      this.jaxbContext.addReferencedTypeDefinitions(type, contextStack);
      if (wasEmpty && !this.jaxbContext.isEmpty()) {
        this.apiRegistry.getSyntaxes().add(this.jaxbContext);
      }
    }
    else {
      debug("Element %s is NOT to be added as a JAXB data type because %s doesn't seem to include XML.", type, declaredMediaTypes);
    }
  }

  @Override
  public void call(EnunciateContext context) {
    this.jaxbContext = new EnunciateJaxbContext(context, isDisableExamples());
    DataTypeDetectionStrategy detectionStrategy = getDataTypeDetectionStrategy();
    switch (detectionStrategy) {
      case aggressive:
        for (Element declaration : context.getApiElements()) {
          addPotentialJaxbElement(declaration, new LinkedList<Element>());
        }
        break;
      case local:
        for (Element declaration : context.getLocalApiElements()) {
          addPotentialJaxbElement(declaration, new LinkedList<Element>());
        }
        //no break, add explicit includes:
      default:
        if (context.hasExplicitIncludes()) { //if we're not aggressive, we only want to add the api elements if they've been explicitly included
          for (Element declaration : context.getApiElements()) {
            addPotentialJaxbElement(declaration, new LinkedList<Element>());
          }
        }
    }

    this.enunciate.addArtifact(new JaxbContextClassListArtifact(this.jaxbContext));
    this.enunciate.addArtifact(new NamespacePropertiesArtifact(this.jaxbContext));
  }

  public void addPotentialJaxbElement(Element declaration, LinkedList<Element> contextStack) {
    if (declaration instanceof TypeElement) {
      boolean addSyntax = false;
      XmlRegistry registryMetadata = declaration.getAnnotation(XmlRegistry.class);
      if (registryMetadata != null) {
        addSyntax = this.jaxbContext.isEmpty();
        Registry registry = new Registry((TypeElement) declaration, jaxbContext);
        this.jaxbContext.add(registry);
      }
      else if (!this.jaxbContext.isKnownTypeDefinition((TypeElement) declaration) && isExplicitTypeDefinition(declaration)) {
        addSyntax = this.jaxbContext.isEmpty();
        this.jaxbContext.add(this.jaxbContext.createTypeDefinition((TypeElement) declaration), contextStack);
      }

      if (addSyntax) {
        //if this is the first xml element, add the xml syntax to the registry.
        this.apiRegistry.getSyntaxes().add(this.jaxbContext);
      }
    }
  }

  protected boolean isExplicitTypeDefinition(Element declaration) {
    if (declaration.getKind() != ElementKind.CLASS && declaration.getKind() != ElementKind.ENUM) {
      debug("%s isn't a potential JAXB type because it's not a class or an enum.", declaration);
      return false;
    }

    PackageElement pckg = this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration);
    if ((pckg != null) && (pckg.getAnnotation(Ignore.class) != null)) {
      debug("%s isn't a potential JAXB type because its package is annotated as to be ignored.", declaration);
      return false;
    }

    if (isThrowable(declaration)) {
      debug("%s isn't a potential JAXB type because it's an instance of java.lang.Throwable.", declaration);
      return false;
    }

    List<? extends AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
    boolean explicitXMLTypeOrElement = false;
    for (AnnotationMirror mirror : annotationMirrors) {
      Element annotationDeclaration = mirror.getAnnotationType().asElement();
      if (annotationDeclaration != null) {
        String fqn = annotationDeclaration instanceof TypeElement ? ((TypeElement)annotationDeclaration).getQualifiedName().toString() : "";
        //exclude all XmlTransient types and all jaxws types.
        if (XmlTransient.class.getName().equals(fqn)
          || fqn.startsWith("javax.xml.ws")
          || fqn.startsWith("javax.ws.rs")
          || fqn.startsWith("javax.jws")) {
          debug("%s isn't a potential JAXB type because of annotation %s.", declaration, fqn);
          return false;
        }
        else {
          explicitXMLTypeOrElement = (XmlType.class.getName().equals(fqn)) || (XmlRootElement.class.getName().equals(fqn));
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
    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String classAnnotation : classAnnotations) {
        if ((XmlType.class.getName().equals(classAnnotation)) || (XmlRootElement.class.getName().equals(classAnnotation))) {
          return true;
        }
      }
    }
    return false;
  }
}
