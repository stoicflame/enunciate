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
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import com.webcohesion.enunciate.util.TypeHintUtils;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.IncompleteAnnotationException;
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
  private final boolean multivalued;

  public SimpleRequestParameter(Element declaration, PathContext context) {
    this(declaration, context, null);
  }

  public SimpleRequestParameter(Element declaration, PathContext context, ResourceParameterType defaultType) {
    super(declaration, context.getContext().getContext().getProcessingEnvironment());
    this.context = context;

    String parameterName = declaration.getSimpleName().toString();
    String typeName = null;
    String defaultValue = null;

    MatrixVariable matrixParam = declaration.getAnnotation(MatrixVariable.class);
    if (matrixParam != null) {
      parameterName = matrixParam.value();
      if (parameterName.isEmpty()) {
        try {
          parameterName = matrixParam.name();
        }
        catch (IncompleteAnnotationException e) {
          //fall through; 'matrixParam.name' was added in 4.2.
        }
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(matrixParam.defaultValue())) {
        defaultValue = matrixParam.defaultValue();
      }
      typeName = "matrix";
    }

    RequestParam queryParam = declaration.getAnnotation(RequestParam.class);
    if (queryParam != null) {
      parameterName = queryParam.value();
      if (parameterName.isEmpty()) {
        try {
          parameterName = queryParam.name();
        }
        catch (IncompleteAnnotationException e) {
          //fall through; 'queryParameter.name' was added in 4.2.
        }
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(queryParam.defaultValue())) {
        defaultValue = queryParam.defaultValue();
      }
      typeName = "query";
    }

    PathVariable pathParam = declaration.getAnnotation(PathVariable.class);
    if (pathParam != null) {
      parameterName = pathParam.value();
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      typeName = "path";
    }

    RequestHeader headerParam = declaration.getAnnotation(RequestHeader.class);
    if (headerParam != null) {
      parameterName = headerParam.value();
      if (parameterName.isEmpty()) {
        try {
          parameterName = headerParam.name();
        }
        catch (IncompleteAnnotationException e) {
          //fall through; 'headerParam.name' was added in 4.2.
        }
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(headerParam.defaultValue())) {
        defaultValue = headerParam.defaultValue();
      }
      typeName = "header";
    }

    CookieValue cookieParam = declaration.getAnnotation(CookieValue.class);
    if (cookieParam != null) {
      parameterName = cookieParam.value();
      if (parameterName.isEmpty()) {
        try {
          parameterName = cookieParam.name();
        }
        catch (IncompleteAnnotationException e) {
          //fall through; 'name' was added in a later spring version.
        }
      }
      if (parameterName.isEmpty()) {
        parameterName = declaration.getSimpleName().toString();
      }
      if (!ValueConstants.DEFAULT_NONE.equals(cookieParam.defaultValue())) {
        defaultValue = cookieParam.defaultValue();
      }
      typeName = "cookie";
    }

    RequestPart formParam = declaration.getAnnotation(RequestPart.class);
    if (formParam != null) {
      parameterName = formParam.value();
      if (parameterName.isEmpty()) {
        try {
          parameterName = formParam.name();
        }
        catch (IncompleteAnnotationException e) {
          //fall through; 'formParam.name' was added in 4.2.
        }
      }
      typeName = "form";
    }

    if (typeName == null) {
      typeName = defaultType == null ? "custom" : defaultType.name().toLowerCase();
    }

    DecoratedTypeMirror parameterType = loadType();
    this.multivalued = parameterType.isArray() || parameterType.isCollection();

    this.parameterName = parameterName;
    this.typeName = typeName;
    this.defaultValue = defaultValue;

    if (delegate instanceof DecoratedVariableElement) {
      getJavaDoc().setValue(((DecoratedVariableElement)delegate).getDocComment());
    }
  }

  protected DecoratedTypeMirror loadType() {
    TypeHint hint = getAnnotation(TypeHint.class);
    if (hint != null) {
      return (DecoratedTypeMirror) TypeHintUtils.getTypeHint(hint, context.getContext().getContext().getProcessingEnvironment(), asType());
    }
    return (DecoratedTypeMirror) asType();
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

    DecoratedTypeMirror type = loadType();

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
      List<VariableElement> enumConstants = ((DecoratedTypeElement) ((DeclaredType) type).asElement()).enumValues();
      Set<String> values = new TreeSet<String>();
      for (VariableElement enumConstant : enumConstants) {
        values.add(enumConstant.getSimpleName().toString());
      }
      return new ResourceParameterConstraints.Enumeration(values);
    }

    return new ResourceParameterConstraints.UnboundString();
  }

  protected ResourceParameterDataType loadDataType() {
    DecoratedTypeMirror type = loadType();

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
      switch (type.getKind()) {
        case BOOLEAN:
          return ResourceParameterDataType.BOOLEAN;
        case INT:
          return ResourceParameterDataType.INTEGER;
        case DOUBLE:
        case FLOAT:
        case LONG:
        case SHORT:
          return ResourceParameterDataType.NUMBER;
        default:
          return ResourceParameterDataType.STRING;
      }
    }
    else if (type.isEnum()) {
      return ResourceParameterDataType.STRING;
    }
    else if (getTypeName().contains("form")) {
      if (type.isInstanceOf(String.class)) {
        return ResourceParameterDataType.STRING;
      }
      else {
        return ResourceParameterDataType.FILE;
      }
    }
    else {
      return ResourceParameterDataType.STRING;
    }
  }}
