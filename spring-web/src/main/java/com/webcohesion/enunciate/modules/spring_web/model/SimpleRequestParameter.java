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

import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * PathParameter for a Spring Web request.
 *
 * @author Ryan Heaton
 */
public class SimpleRequestParameter extends RequestParameter {

  private final PathContext context;
  private final String parameterName;
  private final String defaultValue;
  private final String typeName;

  private final boolean matrixParam;
  private final boolean queryParam;
  private final boolean pathParam;
  private final boolean cookieParam;
  private final boolean headerParam;
  private final boolean formParam;
  private final boolean multivalued;

  public SimpleRequestParameter(Element declaration, PathContext context) {
    super(declaration, context.getContext().getContext().getProcessingEnvironment());
    this.context = context;

    String parameterName = declaration.getSimpleName().toString();
    String typeName = "query";
    boolean query = true;
    String defaultValue = null;
    boolean matrix = false;
    boolean path = false;
    boolean cookie = false;
    boolean header = false;
    boolean form = false;

    MatrixVariable matrixParam = declaration.getAnnotation(MatrixVariable.class);
    if (matrixParam != null) {
      parameterName = matrixParam.value();
      if (parameterName.isEmpty()) {
        parameterName = matrixParam.name();
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(matrixParam.defaultValue())) {
        defaultValue = matrixParam.defaultValue();
      }
      typeName = "matrix";
      matrix = true;
      query = false;
    }

    RequestParam queryParam = declaration.getAnnotation(RequestParam.class);
    if (queryParam != null) {
      parameterName = queryParam.value();
      if (parameterName.isEmpty()) {
        parameterName = queryParam.name();
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(queryParam.defaultValue())) {
        defaultValue = queryParam.defaultValue();
      }
      typeName = "query";
      query = true;
    }

    PathVariable pathParam = declaration.getAnnotation(PathVariable.class);
    if (pathParam != null) {
      parameterName = pathParam.value();
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      typeName = "path";
      path = true;
      query = false;
    }

    RequestHeader headerParam = declaration.getAnnotation(RequestHeader.class);
    if (headerParam != null) {
      parameterName = headerParam.value();
      if (parameterName.isEmpty()) {
        parameterName = headerParam.name();
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(headerParam.defaultValue())) {
        defaultValue = headerParam.defaultValue();
      }
      typeName = "header";
      header = true;
      query = false;
    }

    CookieValue cookieParam = declaration.getAnnotation(CookieValue.class);
    if (cookieParam != null) {
      parameterName = cookieParam.value();
      if (parameterName.isEmpty()) {
        parameterName = cookieParam.name();
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(cookieParam.defaultValue())) {
        defaultValue = cookieParam.defaultValue();
      }
      typeName = "cookie";
      cookie = true;
      query = false;
    }

    RequestPart formParam = declaration.getAnnotation(RequestPart.class);
    if (formParam != null) {
      parameterName = formParam.value();
      if (parameterName.isEmpty()) {
        parameterName = formParam.name();
      }
      typeName = "form";
      form = true;
      query = false;
    }

    if (typeName == null) {
      typeName = "custom";
    }

    DecoratedTypeMirror parameterType = (DecoratedTypeMirror) declaration.asType();
    this.multivalued = parameterType.isArray() || parameterType.isCollection();

    this.parameterName = parameterName;
    this.matrixParam = matrix;
    this.queryParam = query;
    this.pathParam = path;
    this.cookieParam = cookie;
    this.headerParam = header;
    this.formParam = form;
    this.typeName = typeName;
    this.defaultValue = defaultValue;

    if (delegate instanceof DecoratedVariableElement) {
      getJavaDoc().setValue(((DecoratedVariableElement)delegate).getDocComment());
    }
  }

  /**
   * The parameter name.
   *
   * @return The parameter name.
   */
  @Override
  public String getParameterName() {
    return parameterName;
  }

  /**
   * The default value.
   *
   * @return The default value.
   */
  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Whether this is a matrix parameter.
   *
   * @return Whether this is a matrix parameter.
   */
  @Override
  public boolean isMatrixParam() {
    return matrixParam;
  }

  /**
   * Whether this is a query parameter.
   *
   * @return Whether this is a query parameter.
   */
  @Override
  public boolean isQueryParam() {
    return queryParam;
  }

  /**
   * Whether this is a path parameter.
   *
   * @return Whether this is a path parameter.
   */
  @Override
  public boolean isPathParam() {
    return pathParam;
  }

  /**
   * Whether this is a cookie parameter.
   *
   * @return Whether this is a cookie parameter.
   */
  @Override
  public boolean isCookieParam() {
    return cookieParam;
  }

  /**
   * Whether this is a header parameter.
   *
   * @return Whether this is a header parameter.
   */
  @Override
  public boolean isHeaderParam() {
    return headerParam;
  }

  /**
   * Whether this is a form parameter.
   *
   * @return Whether this is a form parameter.
   */
  @Override
  public boolean isFormParam() {
    return formParam;
  }

  /**
   * The type of the parameter.
   *
   * @return The type of the parameter.
   */
  @Override
  public String getTypeName() {
    return this.typeName;
  }

  /**
   * Whether this parameter is multi-valued.
   *
   * @return Whether this parameter is multi-valued.
   */
  @Override
  public boolean isMultivalued() {
    return multivalued;
  }

  @Override
  protected ResourceParameterConstraints loadConstraints() {
    for (PathSegment segment : this.context.getPathSegments()) {
      if (getParameterName().equals(segment.getVariable()) && segment.getRegex() != null) {
        return new ResourceParameterConstraints.Regex(segment.getRegex());
      }
    }

    DecoratedTypeMirror type = (DecoratedTypeMirror) asType();

    //unwrap it, if possible.
    DecoratedTypeMirror componentType = TypeMirrorUtils.getComponentType(type, this.context.getContext().getContext().getProcessingEnvironment());
    if (componentType != null) {
      type = componentType;
    }

    //unbox it, if possible.
    try {
      type = (DecoratedTypeMirror) this.context.getContext().getContext().getProcessingEnvironment().getTypeUtils().unboxedType(type);
    }
    catch (Exception e) {
      //no-op; not unboxable.
    }

    if (type.isPrimitive()) {
      return new ResourceParameterConstraints.Primitive(type.getKind());
    }
    else if (type.isEnum()) {
      List<VariableElement> enumConstants = ((DecoratedTypeElement) ((DeclaredType) type).asElement()).getEnumConstants();
      Set<String> values = new TreeSet<String>();
      for (VariableElement enumConstant : enumConstants) {
        values.add(enumConstant.getSimpleName().toString());
      }
      return new ResourceParameterConstraints.Enumeration(values);
    }

    return new ResourceParameterConstraints.UnboundString();
  }

}
