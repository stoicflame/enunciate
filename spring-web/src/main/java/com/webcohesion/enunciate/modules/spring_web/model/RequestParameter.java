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

package com.webcohesion.enunciate.modules.spring_web.model;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;

import javax.lang.model.element.Element;

/**
 * Base interface for a Spring Web request.
 *
 * @author Ryan Heaton
 */
public abstract class RequestParameter extends DecoratedElement<Element> implements Comparable<RequestParameter> {

  private ResourceParameterConstraints constraints;

  protected RequestParameter(Element declaration, DecoratedProcessingEnvironment env) {
    super(declaration, env);
  }

  /**
   * The parameter name.
   *
   * @return The parameter name.
   */
  public abstract String getParameterName();

  /**
   * The default value.
   *
   * @return The default value.
   */
  public abstract String getDefaultValue();

  /**
   * Whether this is a matrix parameter.
   *
   * @return Whether this is a matrix parameter.
   */
  public abstract boolean isMatrixParam();

  /**
   * Whether this is a query parameter.
   *
   * @return Whether this is a query parameter.
   */
  public abstract boolean isQueryParam();

  /**
   * Whether this is a path parameter.
   *
   * @return Whether this is a path parameter.
   */
  public abstract boolean isPathParam();

  /**
   * Whether this is a cookie parameter.
   *
   * @return Whether this is a cookie parameter.
   */
  public abstract boolean isCookieParam();

  /**
   * Whether this is a header parameter.
   *
   * @return Whether this is a header parameter.
   */
  public abstract boolean isHeaderParam();

  /**
   * Whether this is a form parameter.
   *
   * @return Whether this is a form parameter.
   */
  public abstract boolean isFormParam();

  /**
   * The type of the parameter.
   *
   * @return The type of the parameter.
   */
  public abstract String getTypeName();

  /**
   * Whether this parameter is multi-valued.
   *
   * @return Whether this parameter is multi-valued.
   */
  public abstract boolean isMultivalued();

  /**
   * The constraints of the resource parameter.
   *
   * @return The constraints of the resource parameter.
   */
  public final ResourceParameterConstraints getConstraints() {
    if (this.constraints == null) {
      this.constraints = loadConstraints();
    }

    return this.constraints;
  }

  protected abstract ResourceParameterConstraints loadConstraints();

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RequestParameter)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    String parameterName = getParameterName();
    String typeName = getTypeName();
    RequestParameter that = (RequestParameter) o;
    String thatParameterName = that.getParameterName();
    String thatTypeName = that.getTypeName();
    return !(parameterName != null ? !parameterName.equals(thatParameterName) : thatParameterName != null) && !(typeName != null ? !typeName.equals(thatTypeName) : thatTypeName != null);
  }

  @Override
  public final int hashCode() {
    String parameterName = getParameterName();
    String typeName = getTypeName();
    int result = parameterName != null ? parameterName.hashCode() : 0;
    result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
    return result;
  }

  @Override
  public final int compareTo(RequestParameter other) {
    String parameterName = getParameterName();
    String typeName = getTypeName();
    RequestParameter that = (RequestParameter) other;
    String thatParameterName = that.getParameterName();
    String thatTypeName = that.getTypeName();
    return (typeName + parameterName).compareTo(thatTypeName + thatParameterName);
  }
}
