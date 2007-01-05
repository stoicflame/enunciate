package net.sf.enunciate.contract.rest;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.type.InterfaceType;
import net.sf.enunciate.rest.annotations.Exclude;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import net.sf.jelly.apt.Context;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A class declaration decorated as a REST endpoint.
 *
 * @author Ryan Heaton
 */
public class RESTEndpoint extends DecoratedTypeDeclaration {

  //todo: support versioning a REST endpoint.

  private final ClassDeclaration implementation;
  private final Collection<RESTMethod> RESTMethods;

  public RESTEndpoint(TypeDeclaration delegate) {
    super(delegate);

    this.RESTMethods = new ArrayList<RESTMethod>();
    for (MethodDeclaration methodDeclaration : delegate.getMethods()) {
      if ((methodDeclaration.getModifiers().contains(Modifier.PUBLIC)) && (methodDeclaration.getAnnotation(Exclude.class) == null)) {
        this.RESTMethods.add(new RESTMethod(methodDeclaration));
      }
    }

    ClassDeclaration implementation = null;
    if (isClass()) {
      //if the declaration is a class, it is its own implementation.
      implementation = (ClassDeclaration) this;
    }
    else {
      String interfaceName = getQualifiedName();
      for (TypeDeclaration declaration : getAnnotationProcessorEnvironment().getTypeDeclarations()) {
        if (declaration instanceof ClassDeclaration) {
          for (InterfaceType interfaceType : declaration.getSuperinterfaces()) {
            if ((interfaceType.getDeclaration() != null) && (interfaceName.equals(interfaceType.getDeclaration().getQualifiedName()))) {
              if (implementation != null) {
                throw new ValidationException(getPosition(), "REST endpoint must not have more than one implementation.  Found " +
                  implementation.getPosition() + " and " + declaration.getPosition() + ".");
              }

              implementation = (ClassDeclaration) declaration;
            }
          }
        }
      }
    }

    if (implementation == null) {
      throw new ValidationException(getPosition(), "No implementations of the REST endpoint were found.");
    }

    this.implementation = implementation;
  }

  /**
   * The implementation of this REST endpoint.  If this is an interface, it is its implementation.  
   * Otherwise it is <code>this</code>.
   *
   * @return The implementation of the endpoint.
   */
  public ClassDeclaration getImplementation() {
    return implementation;
  }

  /**
   * The rest methods on this REST endpoint.
   *
   * @return The rest methods on this REST endpoint.
   */
  public Collection<RESTMethod> getRESTMethods() {
    return RESTMethods;
  }

  /**
   * The current annotation processing environment.
   *
   * @return The current annotation processing environment.
   */
  protected AnnotationProcessorEnvironment getAnnotationProcessorEnvironment() {
    return Context.getCurrentEnvironment();
  }

}
