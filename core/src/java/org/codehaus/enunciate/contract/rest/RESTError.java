package org.codehaus.enunciate.contract.rest;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;

/**
 * An error that can be thrown as a result of a REST method invocation.
 * 
 * @author Ryan Heaton
 */
public class RESTError extends DecoratedClassDeclaration {

  public RESTError(ClassDeclaration delegate) {
    super(delegate);
  }

  /**
   * The error code for this REST error.
   *
   * @return The error code for this REST error.
   */
  public int getErrorCode() {
    int errorCode = 500;

    org.codehaus.enunciate.rest.annotations.RESTError errorInfo = getAnnotation(org.codehaus.enunciate.rest.annotations.RESTError.class);
    if (errorInfo != null) {
      errorCode = errorInfo.errorCode();
    }

    return errorCode;
  }
}
