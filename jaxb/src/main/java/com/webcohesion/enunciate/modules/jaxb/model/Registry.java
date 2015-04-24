package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.util.ContextAwareElement;

import javax.lang.model.element.*;
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
public class Registry extends ContextAwareElement {

  final Schema schema;
  final javax.lang.model.element.TypeElement element;

  public Registry(javax.lang.model.element.TypeElement element, EnunciateContext context) {
    super(context);
    if (element.getAnnotation(XmlRegistry.class) == null) {
      throw new IllegalArgumentException("Not a registry: " + element);
    }

    this.schema = Schema.buildSchemaFor(element.getEnclosingElement(), context);
    this.element = element;
  }

  /**
   * The instance factory methods.
   *
   * @return The instance factory methods.
   */
  public Collection<ExecutableElement> getInstanceFactoryMethods() {
    ArrayList<ExecutableElement> instanceFactoryMethods = new ArrayList<ExecutableElement>();
    for (ExecutableElement methodDeclaration : ElementFilter.methodsIn(this.element.getEnclosedElements())) {
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
    for (ExecutableElement methodDeclaration : ElementFilter.methodsIn(this.element.getEnclosedElements())) {
      if (methodDeclaration.getModifiers().contains(Modifier.PUBLIC)
        && methodDeclaration.getAnnotation(XmlElementDecl.class) != null
        && methodDeclaration.getSimpleName().toString().startsWith("create")
        && methodDeclaration.getReturnType().getKind() == TypeKind.DECLARED
        && isInstanceOf(methodDeclaration.getReturnType(), JAXBElement.class)
        && methodDeclaration.getParameters().size() == 1) {
        localElementDeclarations.add(new LocalElementDeclaration(methodDeclaration, this));
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
