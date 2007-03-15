/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
