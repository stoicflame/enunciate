/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
  private ResourceParameterDataType dataType;

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
   * Whether this parameter is required.
   *
   * @return Whether this parameter is required.
   */
  public abstract boolean isRequired();

  /**
   * The processing environment.
   *
   * @return The processing environment.
   */
  public abstract DecoratedProcessingEnvironment getEnvironment();

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

  public ResourceParameterDataType getDataType() {
    if (this.dataType == null) {
      this.dataType = loadDataType();
    }

    return this.dataType;
  }

  protected abstract ResourceParameterConstraints loadConstraints();

  protected abstract ResourceParameterDataType loadDataType();

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
