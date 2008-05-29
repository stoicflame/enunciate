/*
 * Copyright 2006-2008 Web Cohesion
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

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.Declarations;
import org.codehaus.enunciate.rest.annotations.Verb;
import org.codehaus.enunciate.util.TypeDeclarationComparator;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class declaration decorated as a REST endpoint.
 *
 * @author Ryan Heaton
 */
public class RESTEndpoint extends DecoratedClassDeclaration {

  //todo: support versioning a REST endpoint.

  private final Set<TypeDeclaration> definingInterfaces;
  private final Collection<RESTMethod> RESTMethods;
  private final String name;
  private String baseURL;

  public RESTEndpoint(ClassDeclaration delegate) {
    super(delegate);

    this.definingInterfaces = new TreeSet<TypeDeclaration>(new TypeDeclarationComparator());

    ArrayList<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
    //first iterate through all direct superinterfaces and add their methods if they are annotated as a REST endpoint:
    for (InterfaceType interfaceType : delegate.getSuperinterfaces()) {
      InterfaceDeclaration interfaceDeclaration = interfaceType.getDeclaration();
      if ((interfaceDeclaration != null) && (interfaceDeclaration.getAnnotation(org.codehaus.enunciate.rest.annotations.RESTEndpoint.class) != null)) {
        for (MethodDeclaration methodDeclaration : interfaceDeclaration.getMethods()) {
          if (methodDeclaration.getAnnotation(Verb.class) != null) {
            this.definingInterfaces.add(interfaceDeclaration);
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
        definingInterfaces.add(delegate);
        methods.add(methodDeclaration);
      }
    }

    org.codehaus.enunciate.rest.annotations.RESTEndpoint endpointInfo = getAnnotation(org.codehaus.enunciate.rest.annotations.RESTEndpoint.class);
    if ((endpointInfo != null) && !"".equals(endpointInfo.name())) {
      this.name = endpointInfo.name();
    }
    else {
      this.name = getSimpleName() + "Endpoint";
    }

    this.RESTMethods = new ArrayList<RESTMethod>();
    for (MethodDeclaration methodDeclaration : methods) {
      this.RESTMethods.add(new RESTMethod(methodDeclaration, this));
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
   * The type declarations that define this REST endpoint.  Not necessarily an instance of {@link com.sun.mirror.declaration.InterfaceDeclaration} as
   * a REST method could be defined on a class.
   *
   * @return The type declarations that define this REST endpoint.
   */
  public Set<TypeDeclaration> getDefiningInterfaces() {
    return definingInterfaces;
  }

  /**
   * The name of this endpoint.
   *
   * @return The name of this endpoint.
   */
  public String getName() {
    return name;
  }

  /**
   * The base URL for REST endpoints.
   *
   * @return The base URL for REST endpoints.
   */
  public String getBaseURL() {
    return baseURL;
  }

  /**
   * The base URL for REST endpoints.
   *
   * @param baseURL The base URL for REST endpoints.
   */
  public void setBaseURL(String baseURL) {
    this.baseURL = baseURL;
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
