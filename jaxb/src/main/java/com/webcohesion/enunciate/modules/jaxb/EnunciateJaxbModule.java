package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.DependencySpec;
import com.webcohesion.enunciate.modules.jaxb.model.Registry;

import javax.lang.model.element.*;
import javax.xml.bind.annotation.XmlRegistry;
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
public class EnunciateJaxbModule extends BasicEnunicateModule {

  @Override
  public String getName() {
    return "jaxb";
  }

  @Override
  public List<DependencySpec> getDependencies() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEnabled() {
    return !this.context.getConfiguration().getSource().getBoolean("enunciate.modules.jaxb[@disabled]")
      && (this.dependingModules == null || !this.dependingModules.isEmpty());
  }

  @Override
  public void call(EnunciateContext context) {
    EnunciateJaxbContext jaxbContext = new EnunciateJaxbContext(context);
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      XmlRegistry registryMetadata = declaration.getAnnotation(XmlRegistry.class);
      if (registryMetadata != null) {
        debug("%s.%s to be considered as an XML registry.", packageOf(declaration), declaration.getSimpleName());
        Registry registry = new Registry((TypeElement) declaration, jaxbContext);
        jaxbContext.add(registry);
      }
      else if (isJaxbTypeDefinition(declaration) && jaxbContext.findTypeDefinition(declaration) == null) {
        debug("%s.%s to be considered as an XML type.", packageOf(declaration), declaration.getSimpleName());
        jaxbContext.add(jaxbContext.createTypeDefinition((TypeElement) declaration));
      }
    }
  }

  protected CharSequence packageOf(Element declaration) {
    return this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration).getQualifiedName();
  }

  protected boolean isJaxbTypeDefinition(Element declaration) {
    if (declaration.getKind() != ElementKind.CLASS) {
      debug("%s isn't a potential schema type because it's not a class.", declaration);
      return false;
    }

    PackageElement pckg = this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration);
    if ((pckg != null) && (pckg.getAnnotation(Ignore.class) != null)) {
      debug("%s isn't a potential schema type because its package is annotated as to be ignored.", declaration);
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
          debug("%s isn't a potential schema type because of annotation %s.", declaration, fqn);
          return false;
        }
        else {
          explicitXMLTypeOrElement = (XmlType.class.getName().equals(fqn)) || (XmlRootElement.class.getName().equals(fqn));
        }
      }
    }

    return explicitXMLTypeOrElement || !isThrowable(declaration);
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
}
