package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.ApiRegistryProviderModule;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.MediaTypeDefinitionModule;
import com.webcohesion.enunciate.module.TypeFilteringModule;
import com.webcohesion.enunciate.modules.jaxb.model.Registry;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.*;
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
public class EnunciateJaxbModule extends BasicEnunicateModule implements TypeFilteringModule, MediaTypeDefinitionModule, ApiRegistryProviderModule {

  private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
  private EnunciateJaxbContext jaxbContext;
  private ApiRegistry apiRegistry;

  @Override
  public String getName() {
    return "jaxb";
  }

  public EnunciateJaxbContext getJaxbContext() {
    return jaxbContext;
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
  public void addDataTypeDefinition(Element element, Set<String> declaredMediaTypes, LinkedList<Element> contextStack) {
    boolean jaxbApplies = false;
    for (String mediaType : declaredMediaTypes) {
      if ("*/*".equals(mediaType) || "text/*".equals(mediaType) || "application/*".equals(mediaType) || "text/xml".equals(mediaType) || "application/xml".equals(mediaType) || mediaType.endsWith("+xml")) {
        jaxbApplies = true;
        break;
      }
    }

    if (jaxbApplies) {
      addPotentialJaxbElement(element, contextStack);
    }
    else {
      debug("Element %s is NOT to be added as a JAXB data type because %s doesn't seem to include XML.", element, declaredMediaTypes);
    }
  }

  @Override
  public void call(EnunciateContext context) {
    this.jaxbContext = new EnunciateJaxbContext(context);
    if (this.defaultDataTypeDetectionStrategy != DataTypeDetectionStrategy.PASSIVE) {
      Set<Element> elements = context.getApiElements();
      for (Element declaration : elements) {
        addPotentialJaxbElement(declaration, new LinkedList<Element>());
      }
    }
  }

  protected void addPotentialJaxbElement(Element declaration, LinkedList<Element> contextStack) {
    if (declaration instanceof TypeElement) {
      XmlRegistry registryMetadata = declaration.getAnnotation(XmlRegistry.class);
      if (registryMetadata != null) {
        Registry registry = new Registry((TypeElement) declaration, jaxbContext);
        this.jaxbContext.add(registry);
      }
      else if (!this.jaxbContext.isKnownTypeDefinition((TypeElement) declaration) && isExplicitTypeDefinition(declaration)) {
        this.jaxbContext.add(this.jaxbContext.createTypeDefinition((TypeElement) declaration), contextStack);
      }
    }
  }

  protected boolean isExplicitTypeDefinition(Element declaration) {
    if (declaration.getKind() != ElementKind.CLASS) {
      debug("%s isn't a potential JAXB type because it's not a class.", declaration);
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
  public boolean acceptType(Object type, MetadataAdapter metadata) {
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
