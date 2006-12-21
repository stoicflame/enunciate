package net.sf.enunciate.contract.rest;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import net.sf.enunciate.rest.annotations.Exclude;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A class declaration decorated as a REST endpoint.
 *
 * @author Ryan Heaton
 */
public class RESTEndpoint extends DecoratedClassDeclaration {

  private final Collection<RESTMethod> RESTMethods;

  public RESTEndpoint(ClassDeclaration delegate) {
    super(delegate);

    this.RESTMethods = new ArrayList<RESTMethod>();
    for (MethodDeclaration methodDeclaration : delegate.getMethods()) {
      if ((methodDeclaration.getModifiers().contains(Modifier.PUBLIC)) && (!methodDeclaration.getModifiers().contains(Modifier.ABSTRACT))) {
        if (methodDeclaration.getAnnotation(Exclude.class) == null) {
          this.RESTMethods.add(new RESTMethod(methodDeclaration));
        }
      }
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
}
