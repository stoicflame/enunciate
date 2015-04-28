package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a JAXB XmlRegistry. This is used to support contract-first development.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class Registry extends DecoratedTypeElement {

  final Schema schema;
  private final EnunciateJaxbContext context;

  public Registry(javax.lang.model.element.TypeElement element, EnunciateJaxbContext context) {
    super(element, context.getContext().getProcessingEnvironment());
    if (element.getAnnotation(XmlRegistry.class) == null) {
      throw new IllegalArgumentException("Not a registry: " + element);
    }

    this.schema = new Schema(env.getElementUtils().getPackageOf(element.getEnclosingElement()), env);
    this.context = context;
  }

  /**
   * The instance factory methods.
   *
   * @return The instance factory methods.
   */
  public Collection<ExecutableElement> getInstanceFactoryMethods() {
    ArrayList<ExecutableElement> instanceFactoryMethods = new ArrayList<ExecutableElement>();
    for (ExecutableElement methodDeclaration : ElementFilter.methodsIn(this.delegate.getEnclosedElements())) {
      if (methodDeclaration.getModifiers().contains(Modifier.PUBLIC)
        && methodDeclaration.getSimpleName().toString().startsWith("create")
        && methodDeclaration.getParameters().isEmpty()
        && methodDeclaration.getReturnType().getKind() == TypeKind.DECLARED) {
        instanceFactoryMethods.add(methodDeclaration);
      }
    }
    return instanceFactoryMethods;
  }

  /**
   * The local element declarations.
   *
   * @return The local element declarations.
   */
  public Collection<LocalElementDeclaration> getLocalElementDeclarations() {
    ArrayList<LocalElementDeclaration> localElementDeclarations = new ArrayList<LocalElementDeclaration>();
    for (ExecutableElement methodDeclaration : ElementFilter.methodsIn(this.delegate.getEnclosedElements())) {
      if (methodDeclaration.getModifiers().contains(Modifier.PUBLIC)
        && methodDeclaration.getAnnotation(XmlElementDecl.class) != null
        && methodDeclaration.getSimpleName().toString().startsWith("create")
        && methodDeclaration.getReturnType().getKind() == TypeKind.DECLARED
        && ((DecoratedTypeMirror)methodDeclaration.getReturnType()).isInstanceOf(JAXBElement.class)
        && methodDeclaration.getParameters().size() == 1) {
        localElementDeclarations.add(new LocalElementDeclaration(methodDeclaration, this, context));
      }
    }
    return localElementDeclarations;

  }

  /**
   * The schema for this registry.
   *
   * @return The schema for this registry.
   */
  public Schema getSchema() {
    return schema;
  }
}
