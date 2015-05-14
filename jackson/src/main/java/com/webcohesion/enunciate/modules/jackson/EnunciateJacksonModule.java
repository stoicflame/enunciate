package com.webcohesion.enunciate.modules.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.TypeFilteringModule;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJacksonModule extends BasicEnunicateModule implements TypeFilteringModule {

  @Override
  public String getName() {
    return "jackson";
  }

  @Override
  public List<DependencySpec> getDependencies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.enunciate.getConfiguration().getSource().getBoolean("enunciate.modules.jackson[@disabled]", false)
      && (this.dependingModules == null || !this.dependingModules.isEmpty());
  }

  @Override
  public void call(EnunciateContext context) {
    EnunciateJacksonContext jaxbContext = new EnunciateJacksonContext(context);
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      if (declaration instanceof TypeElement) {
        if (!jaxbContext.isKnownTypeDefinition((TypeElement) declaration) && isExplicitTypeDefinition(declaration)) {
          jaxbContext.add(jaxbContext.createTypeDefinition((TypeElement) declaration));
        }
      }
    }
  }

  protected boolean isExplicitTypeDefinition(Element declaration) {
    if (declaration.getKind() != ElementKind.CLASS) {
      debug("%s isn't a potential Jackson type because it's not a class.", declaration);
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
          explicitXMLTypeOrElement = fqn.startsWith(JsonSerialize.class.getPackage().getName()) || fqn.startsWith(JsonFormat.class.getPackage().getName());
        }
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
      for (String fqn : classAnnotations) {
        if (fqn.startsWith(JsonSerialize.class.getPackage().getName()) || fqn.startsWith(JsonFormat.class.getPackage().getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
