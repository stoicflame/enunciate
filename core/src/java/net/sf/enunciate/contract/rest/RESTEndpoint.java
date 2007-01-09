package net.sf.enunciate.contract.rest;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.Declarations;
import net.sf.enunciate.rest.annotations.Verb;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A class declaration decorated as a REST endpoint.
 *
 * @author Ryan Heaton
 */
public class RESTEndpoint extends DecoratedClassDeclaration {

  //todo: support versioning a REST endpoint.

  private final Collection<RESTMethod> RESTMethods;

  public RESTEndpoint(ClassDeclaration delegate) {
    super(delegate);

    ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
    //first iterate through all direct superinterfaces and add their methods if they are annotated as a REST endpoint:
    for (InterfaceType interfaceType : delegate.getSuperinterfaces()) {
      InterfaceDeclaration interfaceDeclaration = interfaceType.getDeclaration();
      if ((interfaceDeclaration != null) && (interfaceDeclaration.getAnnotation(net.sf.enunciate.rest.annotations.RESTEndpoint.class) != null)) {
        for (MethodDeclaration methodDeclaration : interfaceDeclaration.getMethods()) {
          if (methodDeclaration.getAnnotation(Verb.class) != null) {
            methods.add(methodDeclaration);
          }
        }
      }
    }


    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Declarations utils = env.getDeclarationUtils();

    CLASS_METHODS : for (MethodDeclaration methodDeclaration : delegate.getMethods()) {
      //first make sure that this method isn't just an implementation of an interface method already added.
      for (MethodDeclaration method : methods) {
        if (utils.overrides(methodDeclaration, method)) {
          break CLASS_METHODS;
        }
      }

      if ((methodDeclaration.getModifiers().contains(Modifier.PUBLIC)) && (methodDeclaration.getAnnotation(Verb.class) != null)) {
        methods.add(methodDeclaration);
      }
    }

    this.RESTMethods = new ArrayList<RESTMethod>();
    for (MethodDeclaration methodDeclaration : methods) {
      this.RESTMethods.add(new RESTMethod(methodDeclaration));
    }
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
