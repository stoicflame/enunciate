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

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.DeclarationDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parameter for a JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public class ResourceParameter extends DecoratedDeclaration {

  public static final List<String> FORM_BEAN_ANNOTATIONS = Arrays.asList("org.jboss.resteasy.annotations.Form");

  private final String parameterName;
  private final String defaultValue;
  private final String typeName;
  private final XmlType xmlType;

  private final boolean matrixParam;
  private final boolean queryParam;
  private final boolean pathParam;
  private final boolean cookieParam;
  private final boolean headerParam;
  private final boolean formParam;

  public ResourceParameter(Declaration declaration) {
    super(declaration);

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
        AnnotationTypeDeclaration decl = annotation.getAnnotationType().getDeclaration();
        if (decl != null) {
          String fqn = decl.getQualifiedName();
          EnunciateConfiguration config = ((EnunciateFreemarkerModel) FreemarkerModel.get()).getEnunciateConfig();
          if (config != null && config.getCustomResourceParameterAnnotations().contains(fqn)) {
            parameterName = declaration.getSimpleName();
            typeName = decl.getSimpleName().toLowerCase().replaceAll("param", "");
            break;
          }
        }
      }
    }

    if (typeName == null) {
      typeName = "custom";
    }

    XmlType xmlType = null;
    if (declaration instanceof ParameterDeclaration) {
      try {
        xmlType = XmlTypeFactory.getXmlType(((ParameterDeclaration) declaration).getType());
      }
      catch (XmlTypeException e) {
        xmlType = null;
      }
    }

    this.parameterName = parameterName;
    this.matrixParam = matrix;
    this.queryParam = query;
    this.pathParam = path;
    this.cookieParam = cookie;
    this.headerParam = header;
    this.formParam = form;
    this.typeName = typeName;
    this.xmlType = xmlType;

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

        EnunciateConfiguration config = ((EnunciateFreemarkerModel) FreemarkerModel.get()).getEnunciateConfig();
        if (config != null && config.getCustomResourceParameterAnnotations().contains(fqn)) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isFormBeanParameter(Declaration candidate) {
    for (AnnotationMirror annotation : candidate.getAnnotationMirrors()) {
      AnnotationTypeDeclaration declaration = annotation.getAnnotationType().getDeclaration();
      if (declaration != null) {
        String fqn = declaration.getQualifiedName();
        if (FORM_BEAN_ANNOTATIONS.contains(fqn)) {
          return true;
        }
      }
    }

    return false;
  }

  public static List<ResourceParameter> getFormBeanParameters(ParameterDeclaration parameterDeclaration) {
    ArrayList<ResourceParameter> formBeanParameters = new ArrayList<ResourceParameter>();
    gatherFormBeanParameters(parameterDeclaration.getType(), formBeanParameters);
    return formBeanParameters;
  }

  private static void gatherFormBeanParameters(TypeMirror type, ArrayList<ResourceParameter> formBeanParameters) {
    if (type instanceof DeclaredType) {
      DecoratedTypeDeclaration typeDeclaration = (DecoratedTypeDeclaration) DeclarationDecorator.decorate(((DeclaredType) type).getDeclaration());
      for (FieldDeclaration field : typeDeclaration.getFields()) {
        if (isResourceParameter(field)) {
          formBeanParameters.add(new ResourceParameter(field));
        }
        else if (isFormBeanParameter(field)) {
          gatherFormBeanParameters(field.getType(), formBeanParameters);
        }
      }

      for (PropertyDeclaration property : typeDeclaration.getProperties()) {
        if (isResourceParameter(property)) {
          formBeanParameters.add(new ResourceParameter(property));
        }
        else if (isFormBeanParameter(property)) {
          gatherFormBeanParameters(property.getPropertyType(), formBeanParameters);
        }
      }

      if (typeDeclaration instanceof ClassDeclaration) {
        gatherFormBeanParameters(((ClassDeclaration) typeDeclaration).getSuperclass(), formBeanParameters);
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
   * The xml type of the parameter, if applicable.
   *
   * @return The xml type of the parameter, if applicable.
   */
  public XmlType getXmlType() {
    return xmlType;
  }
}
