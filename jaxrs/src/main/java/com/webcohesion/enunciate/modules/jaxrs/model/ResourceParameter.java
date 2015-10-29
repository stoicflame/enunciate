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

package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parameter for a JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public class ResourceParameter extends DecoratedElement<Element> {

  public static final List<String> FORM_BEAN_ANNOTATIONS = Arrays.asList("org.jboss.resteasy.annotations.Form", "javax.ws.rs.BeanParam");

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

  public ResourceParameter(Element declaration, PathContext context) {
    super(declaration, context.getContext().getContext().getProcessingEnvironment());
    this.context = context;

    String parameterName = null;
    String typeName = null;
    boolean matrix = false;
    boolean query = false;
    boolean path = false;
    boolean cookie = false;
    boolean header = false;
    boolean form = false;

    MatrixParam matrixParam = declaration.getAnnotation(MatrixParam.class);
    if (matrixParam != null) {
      parameterName = matrixParam.value();
      typeName = "matrix";
      matrix = true;
    }

    QueryParam queryParam = declaration.getAnnotation(QueryParam.class);
    if (queryParam != null) {
      parameterName = queryParam.value();
      typeName = "query";
      query = true;
    }

    PathParam pathParam = declaration.getAnnotation(PathParam.class);
    if (pathParam != null) {
      parameterName = pathParam.value();
      typeName = "path";
      path = true;
    }

    CookieParam cookieParam = declaration.getAnnotation(CookieParam.class);
    if (cookieParam != null) {
      parameterName = cookieParam.value();
      typeName = "cookie";
      cookie = true;
    }

    HeaderParam headerParam = declaration.getAnnotation(HeaderParam.class);
    if (headerParam != null) {
      parameterName = headerParam.value();
      typeName = "header";
      header = true;
    }

    FormParam formParam = declaration.getAnnotation(FormParam.class);
    if (formParam != null) {
      parameterName = formParam.value();
      typeName = "form";
      form = true;
    }

    if (typeName == null) {
      for (AnnotationMirror annotation : declaration.getAnnotationMirrors()) {
        TypeElement decl = (TypeElement) annotation.getAnnotationType().asElement();
        if (decl != null) {
          String fqn = decl.getQualifiedName().toString();
          if (this.context.getContext().getCustomResourceParameterAnnotations().contains(fqn)) {
            parameterName = declaration.getSimpleName().toString();
            typeName = decl.getSimpleName().toString().toLowerCase().replaceAll("param", "");
            break;
          }
        }
      }
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

    DefaultValue defaultValue = declaration.getAnnotation(DefaultValue.class);
    if (defaultValue != null) {
      this.defaultValue = defaultValue.value();
    }
    else {
      this.defaultValue = null;
    }

    if (delegate instanceof DecoratedVariableElement) {
      getJavaDoc().setValue(((DecoratedVariableElement)delegate).getDocComment());
    }
  }

  public static boolean isResourceParameter(Element candidate, EnunciateJaxrsContext context) {
    if (!isSystemParameter(candidate, context)) {
      for (AnnotationMirror annotation : candidate.getAnnotationMirrors()) {
        TypeElement declaration = (TypeElement) annotation.getAnnotationType().asElement();
        if (declaration != null) {
          String fqn = declaration.getQualifiedName().toString();
          if ((MatrixParam.class.getName().equals(fqn))
            || QueryParam.class.getName().equals(fqn)
            || PathParam.class.getName().equals(fqn)
            || CookieParam.class.getName().equals(fqn)
            || HeaderParam.class.getName().equals(fqn)
            || FormParam.class.getName().equals(fqn)) {
            return true;
          }

          if (context.getSystemResourceParameterAnnotations().contains(fqn)) {
            return false;
          }
          if (context.getCustomResourceParameterAnnotations().contains(fqn)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static boolean isSystemParameter(Element candidate, EnunciateJaxrsContext context) {
    for (AnnotationMirror annotation : candidate.getAnnotationMirrors()) {
      TypeElement declaration = (TypeElement) annotation.getAnnotationType().asElement();
      if (declaration != null) {
        String fqn = declaration.getQualifiedName().toString();
        if (Context.class.getName().equals(fqn) && candidate.getAnnotation(TypeHint.class) == null) {
          return true;
        }
        if (context.getSystemResourceParameterAnnotations().contains(fqn)) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isBeanParameter(Element candidate) {
    for (AnnotationMirror annotation : candidate.getAnnotationMirrors()) {
      TypeElement declaration = (TypeElement) annotation.getAnnotationType().asElement();
      if (declaration != null) {
        String fqn = declaration.getQualifiedName().toString();
        if (FORM_BEAN_ANNOTATIONS.contains(fqn)) {
          return true;
        }
      }
    }

    return false;
  }

  public static List<ResourceParameter> getFormBeanParameters(VariableElement parameterDeclaration, PathContext context) {
    ArrayList<ResourceParameter> formBeanParameters = new ArrayList<ResourceParameter>();
    gatherFormBeanParameters(parameterDeclaration.asType(), formBeanParameters, context);
    return formBeanParameters;
  }

  private static void gatherFormBeanParameters(TypeMirror type, ArrayList<ResourceParameter> beanParams, PathContext context) {
    if (type instanceof DeclaredType) {
      DecoratedTypeElement typeDeclaration = (DecoratedTypeElement) ElementDecorator.decorate(((DeclaredType) type).asElement(), context.getContext().getContext().getProcessingEnvironment());
      for (VariableElement field : ElementFilter.fieldsIn(typeDeclaration.getEnclosedElements())) {
        if (isResourceParameter(field, context.getContext())) {
          beanParams.add(new ResourceParameter(field, context));
        }
        else if (isBeanParameter(field)) {
          gatherFormBeanParameters(field.asType(), beanParams, context);
        }
      }

      for (PropertyElement property : typeDeclaration.getProperties()) {
        if (isResourceParameter(property, context.getContext())) {
          beanParams.add(new ResourceParameter(property, context));
        }
        else if (isBeanParameter(property)) {
          gatherFormBeanParameters(property.getPropertyType(), beanParams, context);
        }
      }

      if (typeDeclaration.getKind() == ElementKind.CLASS) {
        gatherFormBeanParameters(typeDeclaration.getSuperclass(), beanParams, context);
      }
    }
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

  /**
   * The type of the parameter.
   *
   * @return The type of the parameter.
   */
  public String getTypeName() {
    return this.typeName;
  }

  /**
   * Whether this parameter is multi-valued.
   *
   * @return Whether this parameter is multi-valued.
   */
  public boolean isMultivalued() {
    return multivalued;
  }
}
