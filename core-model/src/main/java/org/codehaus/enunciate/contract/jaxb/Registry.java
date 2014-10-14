package org.codehaus.enunciate.contract.jaxb;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.ClassType;

import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.JAXBElement;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a JAXB XmlRegistry. This is used to support contract-first development.
 *
 * @author Ryan Heaton
 */
public class Registry extends DecoratedClassDeclaration {

  private final Schema schema;

  public Registry(ClassDeclaration delegate) {
    super(delegate);

    if (delegate.getAnnotation(XmlRegistry.class) == null) {
      throw new IllegalArgumentException("Not a registry: " + delegate.getQualifiedName());
    }

    Package pckg;
    try {
      //if this is an already-compiled class, APT has a problem looking up the package info on the classpath...
      pckg = Class.forName(getQualifiedName()).getPackage();
    }
    catch (Throwable e) {
      pckg = null;
    }
    this.schema = new Schema(getPackage(), pckg);
  }

  /**
   * The instance factory methods.
   *
   * @return The instance factory methods.
   */
  public Collection<MethodDeclaration> getInstanceFactoryMethods() {
    ArrayList<MethodDeclaration> instanceFactoryMethods = new ArrayList<MethodDeclaration>();
    for (MethodDeclaration methodDeclaration : getMethods()) {
      if (((DecoratedMethodDeclaration)methodDeclaration).isPublic()
        && methodDeclaration.getSimpleName().startsWith("create")
        && methodDeclaration.getParameters().isEmpty()
        && methodDeclaration.getReturnType() instanceof DeclaredType
        && ((DeclaredType)methodDeclaration.getReturnType()).getDeclaration() != null) {
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
    for (MethodDeclaration methodDeclaration : getMethods()) {
      if (((DecoratedMethodDeclaration)methodDeclaration).isPublic()
        && methodDeclaration.getAnnotation(XmlElementDecl.class) != null
        && methodDeclaration.getSimpleName().startsWith("create")
        && methodDeclaration.getReturnType() instanceof DeclaredType
        && ((DeclaredType)methodDeclaration.getReturnType()).getDeclaration() != null
        && JAXBElement.class.getName().equals(((DeclaredType)methodDeclaration.getReturnType()).getDeclaration().getQualifiedName())
        && methodDeclaration.getParameters().size() == 1
        && methodDeclaration.getParameters().iterator().next().getType() instanceof ClassType) {
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
