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

package org.codehaus.enunciate.contract.jaxrs;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedDeclaration;
import org.codehaus.enunciate.contract.common.rest.RESTResourceParameter;
import org.codehaus.enunciate.contract.common.rest.RESTResourceParameterType;

import javax.ws.rs.*;

/**
 * Parameter for a JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public class ResourceParameter extends DecoratedDeclaration implements RESTResourceParameter {

  private final String parameterName;
  private final String defaultValue;

  private final boolean matrixParam;
  private final boolean queryParam;
  private final boolean pathParam;
  private final boolean cookieParam;
  private final boolean headerParam;
  private final boolean formParam;

  public ResourceParameter(Declaration declaration) {
    super(declaration);

    String parameterName = null;
    boolean matrix = false;
    boolean query = false;
    boolean path = false;
    boolean cookie = false;
    boolean header = false;
    boolean form = false;

    MatrixParam matrixParam = declaration.getAnnotation(MatrixParam.class);
    if (matrixParam != null) {
      parameterName = matrixParam.value();
      matrix = true;
    }

    QueryParam queryParam = declaration.getAnnotation(QueryParam.class);
    if (queryParam != null) {
      parameterName = queryParam.value();
      query = true;
    }

    PathParam pathParam = declaration.getAnnotation(PathParam.class);
    if (pathParam != null) {
      parameterName = pathParam.value();
      path = true;
    }

    CookieParam cookieParam = declaration.getAnnotation(CookieParam.class);
    if (cookieParam != null) {
      parameterName = cookieParam.value();
      cookie = true;
    }

    HeaderParam headerParam = declaration.getAnnotation(HeaderParam.class);
    if (headerParam != null) {
      parameterName = headerParam.value();
      header = true;
    }

    FormParam formParam = declaration.getAnnotation(FormParam.class);
    if (formParam != null) {
      parameterName = formParam.value();
      form = true;
    }

    this.parameterName = parameterName;
    this.matrixParam = matrix;
    this.queryParam = query;
    this.pathParam = path;
    this.cookieParam = cookie;
    this.headerParam = header;
    this.formParam = form;

    DefaultValue defaultValue = declaration.getAnnotation(DefaultValue.class);
    if (defaultValue != null) {
      this.defaultValue = defaultValue.value();
    }
    else {
      this.defaultValue = null;
    }
  }

  public static boolean isResourceParameter(Declaration candidate) {
    for (AnnotationMirror annotation : candidate.getAnnotationMirrors()) {
      AnnotationTypeDeclaration declaration = annotation.getAnnotationType().getDeclaration();
      if (declaration != null) {
        String fqn = declaration.getQualifiedName();
        if ((MatrixParam.class.getName().equals(fqn))
          || QueryParam.class.getName().equals(fqn)
          || PathParam.class.getName().equals(fqn)
          || CookieParam.class.getName().equals(fqn)
          || HeaderParam.class.getName().equals(fqn)
          || FormParam.class.getName().equals(fqn)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * The parameter name.
   *
   * @return The parameter name.
   */
  public String getParameterName() {
    return parameterName;
  }

  /**
   * The default value.
   *
   * @return The default value.
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Whether this is a matrix parameter.
   *
   * @return Whether this is a matrix parameter.
   */
  public boolean isMatrixParam() {
    return matrixParam;
  }

  /**
   * Whether this is a query parameter.
   *
   * @return Whether this is a query parameter.
   */
  public boolean isQueryParam() {
    return queryParam;
  }

  /**
   * Whether this is a path parameter.
   *
   * @return Whether this is a path parameter.
   */
  public boolean isPathParam() {
    return pathParam;
  }

  /**
   * Whether this is a cookie parameter.
   *
   * @return Whether this is a cookie parameter.
   */
  public boolean isCookieParam() {
    return cookieParam;
  }

  /**
   * Whether this is a header parameter.
   *
   * @return Whether this is a header parameter.
   */
  public boolean isHeaderParam() {
    return headerParam;
  }

  /**
   * Whether this is a form parameter.
   *
   * @return Whether this is a form parameter.
   */
  public boolean isFormParam() {
    return formParam;
  }

  // Inherited.
  public String getResourceParameterName() {
    return getParameterName();
  }

  // Inherited.
  public RESTResourceParameterType getResourceParameterType() {
    if (isPathParam()) {
      return RESTResourceParameterType.PATH;
    }
    else if (isQueryParam()) {
      return RESTResourceParameterType.QUERY;
    }
    else if (isHeaderParam()) {
      return RESTResourceParameterType.HEADER;
    }
    else if (isFormParam()) {
      return RESTResourceParameterType.FORM;
    }
    else if (isMatrixParam()) {
      return RESTResourceParameterType.MATRIX;
    }
    else if (isCookieParam()) {
      return RESTResourceParameterType.COOKIE;
    }
    else {
      return null;
    }
  }
}
