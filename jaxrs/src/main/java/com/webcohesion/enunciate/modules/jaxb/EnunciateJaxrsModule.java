package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.module.TypeFilteringModule;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.*;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxrsModule extends BasicEnunicateModule implements TypeFilteringModule {

  @Override
  public String getName() {
    return "jax-rs";
  }

  @Override
  public List<DependencySpec> getDependencies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.enunciate.getConfiguration().getSource().getBoolean("enunciate.modules.jaxb[@disabled]", false)
      && (this.dependingModules == null || !this.dependingModules.isEmpty());
  }

  @Override
  public void call(EnunciateContext context) {
    EnunciateJaxrsContext jaxrsContext = new EnunciateJaxrsContext(context);
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      if (declaration instanceof TypeElement) {
        ///
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
        if ((Path.class.getName().equals(classAnnotation))
          || (Provider.class.getName().equals(classAnnotation))
          || (ApplicationPath.class.getName().equals(classAnnotation))) {
          return true;
        }
      }
    }
    return false;
  }
}
