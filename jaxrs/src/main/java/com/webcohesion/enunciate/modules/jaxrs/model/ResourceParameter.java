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
package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.element.*;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;
import com.webcohesion.enunciate.util.AnnotationUtils;
import com.webcohesion.enunciate.util.FieldOrRecordUtil;
import com.webcohesion.enunciate.util.TypeHintUtils;

import jakarta.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import java.util.*;

import static com.webcohesion.enunciate.util.AnnotationUtils.isIgnored;

/**
 * Parameter for a JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public class ResourceParameter extends DecoratedElement<Element> implements Comparable<ResourceParameter> {

  public static final List<String> FORM_BEAN_ANNOTATIONS = Arrays.asList("org.jboss.resteasy.annotations.Form", "jakarta.ws.rs.BeanParam");

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
  private ResourceParameterConstraints constraints;
  private ResourceParameterDataType dataType;

  public ResourceParameter(Element declaration, PathContext context) {
    this(declaration, context, null);
  }

  protected ResourceParameter(Element declaration, PathContext context, @Nullable Boolean multivalued) {
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

    jakarta.ws.rs.MatrixParam matrixParam = declaration.getAnnotation(jakarta.ws.rs.MatrixParam.class);
    if (matrixParam != null) {
      parameterName = matrixParam.value();
      typeName = "matrix";
      matrix = true;
    }

    jakarta.ws.rs.QueryParam queryParam = declaration.getAnnotation(jakarta.ws.rs.QueryParam.class);
    if (queryParam != null) {
      parameterName = queryParam.value();
      typeName = "query";
      query = true;
    }

    jakarta.ws.rs.PathParam pathParam = declaration.getAnnotation(jakarta.ws.rs.PathParam.class);
    if (pathParam != null) {
      parameterName = pathParam.value();
      typeName = "path";
      path = true;
    }

    jakarta.ws.rs.CookieParam cookieParam = declaration.getAnnotation(jakarta.ws.rs.CookieParam.class);
    if (cookieParam != null) {
      parameterName = cookieParam.value();
      typeName = "cookie";
      cookie = true;
    }

    jakarta.ws.rs.HeaderParam headerParam = declaration.getAnnotation(jakarta.ws.rs.HeaderParam.class);
    if (headerParam != null) {
      parameterName = headerParam.value();
      typeName = "header";
      header = true;
    }

    jakarta.ws.rs.FormParam formParam = declaration.getAnnotation(jakarta.ws.rs.FormParam.class);
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

    this.multivalued = multivalued != null ? multivalued : multivaluedAutoDetection();

    this.parameterName = parameterName;
    this.matrixParam = matrix;
    this.queryParam = query;
    this.pathParam = path;
    this.cookieParam = cookie;
    this.headerParam = header;
    this.formParam = form;
    this.typeName = typeName;

    jakarta.ws.rs.DefaultValue defaultValue = declaration.getAnnotation(jakarta.ws.rs.DefaultValue.class);
    if (defaultValue != null) {
      this.defaultValue = defaultValue.value();
    }
    else {
      this.defaultValue = null;
    }
  }

  private boolean multivaluedAutoDetection() {
    DecoratedTypeMirror parameterType = loadType();
    return parameterType.isArray() || parameterType.isCollection() || parameterType.isStream();
  }

  public DecoratedTypeMirror loadType() {
    TypeHint hint = getAnnotation(TypeHint.class);
    if (hint != null) {
      return (DecoratedTypeMirror) TypeHintUtils.getTypeHint(hint, getContext().getContext().getContext().getProcessingEnvironment(), asType());
    }
    return (DecoratedTypeMirror) asType();
  }

  public static boolean isResourceParameter(Element candidate, EnunciateJaxrsContext context) {
    if (AnnotationUtils.isIgnored(candidate)) {
      return false;
    }

    if (!isSystemParameter(candidate, context)) {
      for (AnnotationMirror annotation : candidate.getAnnotationMirrors()) {
        TypeElement declaration = (TypeElement) annotation.getAnnotationType().asElement();
        if (declaration != null) {
          String fqn = declaration.getQualifiedName().toString();
          if ((jakarta.ws.rs.MatrixParam.class.getName().equals(fqn))
            || jakarta.ws.rs.QueryParam.class.getName().equals(fqn)
            || jakarta.ws.rs.PathParam.class.getName().equals(fqn)
            || jakarta.ws.rs.CookieParam.class.getName().equals(fqn)
            || jakarta.ws.rs.HeaderParam.class.getName().equals(fqn)
            || jakarta.ws.rs.FormParam.class.getName().equals(fqn)) {
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
        if (jakarta.ws.rs.core.Context.class.getName().equals(fqn) && candidate.getAnnotation(TypeHint.class) == null) {
          return true;
        }
        if (jakarta.ws.rs.container.Suspended.class.getName().equals(fqn) && candidate.getAnnotation(TypeHint.class) == null) {
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
      for (Element field : FieldOrRecordUtil.fieldsOrRecordComponentsIn(typeDeclaration)) {
        if (isResourceParameter(field, context.getContext())) {
          beanParams.add(new ResourceParameter(field, context));
        }
        else if (isBeanParameter(field)) {
          gatherFormBeanParameters(field.asType(), beanParams, context);
        }
      }

      List<PropertyElement> properties = new ArrayList<PropertyElement>(typeDeclaration.getProperties(new JaxRsResourceParameterPropertySpec(context.getContext().getContext().getProcessingEnvironment())));
      for (PropertyElement property : properties) {
        if (isResourceParameter(property, context.getContext())) {
          beanParams.add(new ResourceParameter(property, context));
        }
        else if (isBeanParameter(property)) {
          gatherFormBeanParameters(property.getPropertyType(), beanParams, context);
        }
      }

      if (typeDeclaration.getKind() == ElementKind.CLASS  || typeDeclaration.getKind().name().equals("RECORD")) {
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

  /**
   * The constraints of the resource parameter.
   *
   * @return The constraints of the resource parameter.
   */
  public ResourceParameterConstraints getConstraints() {
    if (this.constraints == null) {
      this.constraints = loadConstraints();
    }

    return this.constraints;
  }

  public ResourceParameterConstraints loadConstraints() {
    String regex = null;
    String componentName = "{" + getParameterName() + "}";
    for (PathSegment component : this.context.getPathComponents()) {
      if (componentName.equals(component.getValue())) {
        regex = component.getRegex();
        break;
      }
    }

    if (regex != null) {
      return new ResourceParameterConstraints.Regex(regex);
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
        if (isIgnored(enumConstant)) {
          continue;
        }

        values.add(getEnumParameterLabel(enumConstant));
      }
      return new ResourceParameterConstraints.Enumeration(values);
    }

    return ResourceParameterConstraints.Unbound.STRING;
  }

  private String getEnumParameterLabel(VariableElement enumConstant) {
    String label = enumConstant.getSimpleName().toString();

    String specifiedLabel = AnnotationUtils.getSpecifiedLabel(enumConstant);
    if (specifiedLabel != null) {
      label = specifiedLabel;
    }

    return label;
  }

  public ResourceParameterDataType getDataType() {
    if (this.dataType == null) {
      this.dataType = loadDataType();
    }

    return this.dataType;
  }

  private ResourceParameterDataType loadDataType() {
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
        case SHORT:
        case INT:
          return ResourceParameterDataType.INT32;
        case LONG:
          return ResourceParameterDataType.INT64;
        case DOUBLE:
          return ResourceParameterDataType.DOUBLE;
        case FLOAT:
          return ResourceParameterDataType.FLOAT;
        default:
          return ResourceParameterDataType.STRING;
      }
    }
    else if (type.isEnum()) {
      return ResourceParameterDataType.STRING;
    }
    else if (isFormParam()) {
      return ResourceParameterDataType.STRING;
    }
    else {
      //some _other_ kind of form; probably a file upload?
      if (getTypeName().contains("form")) {
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
    }
  }

  @Override
  protected JavaDoc getJavaDoc(JavaDocTagHandler tagHandler, boolean useDelegate) {
    return super.getJavaDoc(tagHandler, true);
  }

  public PathContext getContext() {
    return context;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ResourceParameter)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ResourceParameter that = (ResourceParameter) o;
    return !(getParameterName() != null ? !getParameterName().equals(that.getParameterName()) : that.getParameterName() != null) && !(getTypeName() != null ? !getTypeName().equals(that.getTypeName()) : that.getTypeName() != null);
  }

  @Override
  public int hashCode() {
    String pName = getParameterName();
    String tName = getTypeName();

    int result = pName != null ? pName.hashCode() : 0;
    result = 31 * result + (tName != null ? tName.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(ResourceParameter other) {
    return (this.getTypeName() + this.getParameterName()).compareTo(other.getTypeName() + other.getParameterName());
  }

  private static class JaxRsResourceParameterPropertySpec extends ElementUtils.DefaultPropertySpec {
    JaxRsResourceParameterPropertySpec(DecoratedProcessingEnvironment env) {
      super(env);
    }

    @Override
    public boolean isGetter(DecoratedExecutableElement executable) {
      //JAX-RS considers non-public methods as potential properties, too:
      return executable.isGetter();
    }

    @Override
    public boolean isSetter(DecoratedExecutableElement executable) {
      //JAX-RS considers non-public methods as potential properties, too:
      return executable.isSetter();
    }
  }
}
