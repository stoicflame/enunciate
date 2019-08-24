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
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.util.AnnotationUtils;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class RequestParameterFactory {

  public static final Set<String> KNOWN_SYSTEM_MANAGED_PARAMETER_TYPES = new TreeSet<String>(Arrays.asList(
    //list of valid request mapping argument types that are supplied by the system, and not by the user.
    //see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-ann-arguments
    "javax.servlet.ServletContext", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse", "javax.servlet.http.HttpSession",
    "javax.servlet.http.HttpServletResponse", "javax.servlet.http.HttpServletRequest",
    "org.springframework.web.context.request.WebRequest", "java.util.Locale", "java.util.TimeZone", "java.time.ZoneId",
    "java.io.Writer", "java.io.OutputStream", "org.springframework.http.HttpMethod", "java.security.Principal", "org.springframework.ui.Model",
    "org.springframework.ui.ModelMap", "java.util.Map", "org.springframework.web.servlet.mvc.support.RedirectAttributes",
    "org.springframework.validation.Errors", "org.springframework.validation.BindingResult", "org.springframework.web.bind.support.SessionStatus",
    "org.springframework.web.util.UriComponentsBuilder"
  ));

  public static List<RequestParameter> getRequestParameters(ExecutableElement mapping, VariableElement candidate, RequestMapping context) {
    ArrayList<RequestParameter> parameters = new ArrayList<RequestParameter>();

    if (!gatherAnnotatedRequestParameters(mapping, candidate, parameters, context) && !isSystemManagedParameter(candidate)) {
      gatherFormObjectParameters(candidate.asType(), parameters, context);
    }

    return parameters;
  }

  private static boolean isSystemManagedParameter(VariableElement candidate) {
    if (candidate.getAnnotation(ModelAttribute.class) != null) {
      //model attribute parameters are system-managed.
      //see http://docs.spring.io/spring/docs/3.1.x/spring-framework-reference/html/mvc.html#mvc-ann-modelattrib-methods
      return true;
    }

    TypeMirror parameterType = candidate.asType();
    while (parameterType instanceof DeclaredType) {
      Element element = ((DeclaredType) parameterType).asElement();
      if (element instanceof TypeElement) {
        String fqn = ((TypeElement) element).getQualifiedName().toString();
        if (KNOWN_SYSTEM_MANAGED_PARAMETER_TYPES.contains(fqn)) {
          //if it's a system parameter, it's not a request parameter.
          return true;
        }

        parameterType = ((TypeElement) element).getSuperclass();
      }
      else {
        parameterType = null;
      }
    }

    return false;
  }

  private static boolean gatherAnnotatedRequestParameters(ExecutableElement mapping, VariableElement candidate, List<RequestParameter> parameters, PathContext context) {
    List<? extends AnnotationMirror> annotations = candidate.getAnnotationMirrors();

    boolean success = false;
    Boolean isMapStringString = null;
    for (AnnotationMirror annotation : annotations) {
      TypeElement declaration = (TypeElement) annotation.getAnnotationType().asElement();
      if (declaration != null) {
        String fqn = declaration.getQualifiedName().toString();
        if (AnnotationUtils.isIgnored(declaration)) {
          parameters.clear(); //we're told to ignore this, so clear it.
          return true;//and indicate successful gathering.
        }

        if (PathVariable.class.getName().equals(fqn)) {
          if ((isMapStringString == null && (isMapStringString = isMapStringString(candidate.asType(), new TypeVariableContext(), context.getContext().getContext().getProcessingEnvironment()))) || isMapStringString) {
            for (PathSegment segment : context.getPathSegments()) {
              if (segment.getVariable() != null) {
                parameters.add(new ExplicitRequestParameter(mapping, null, segment.getVariable(), ResourceParameterType.PATH, false, segment.getRegex() == null ? ResourceParameterConstraints.Unbound.STRING : new ResourceParameterConstraints.Regex(segment.getRegex()), context.getContext()));
              }
            }
          }
          else {
            parameters.add(new SimpleRequestParameter(candidate, context));
          }
          success = true;
        }
        else if (MatrixVariable.class.getName().equals(fqn)
          || RequestParam.class.getName().equals(fqn)
          || RequestHeader.class.getName().equals(fqn)) {
          if ((isMapStringString == null && (!(isMapStringString = isMapStringString(candidate.asType(), new TypeVariableContext(), context.getContext().getContext().getProcessingEnvironment())))) || !isMapStringString) {
            //only add it if it's _not_ a map-string-string because otherwise we can't name the request parameters.
            parameters.add(new SimpleRequestParameter(candidate, context));
          }
          success = true;
        }
        else if (CookieValue.class.getName().equals(fqn)
          || RequestPart.class.getName().equals(fqn)) {
          parameters.add(new SimpleRequestParameter(candidate, context));
          success = true;
        }
      }
    }

    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(candidate.asType(), context.getContext().getContext().getProcessingEnvironment());
    if (decorated.isInstanceOf("org.springframework.data.domain.Pageable")) {
      parameters.add(new ExplicitRequestParameter(mapping, null, "page", ResourceParameterType.QUERY, false, new ResourceParameterConstraints.Primitive(TypeKind.INT), context.getContext()));
      parameters.add(new ExplicitRequestParameter(mapping, null, "size", ResourceParameterType.QUERY, false, new ResourceParameterConstraints.Primitive(TypeKind.INT), context.getContext()));
      return true;
    }

    return success;
  }

  private static boolean isMapStringString(TypeMirror candidate, TypeVariableContext variableContext, DecoratedProcessingEnvironment env) {
    Element el = candidate instanceof DeclaredType ? ((DeclaredType)candidate).asElement() :
      candidate instanceof TypeVariable ? ((TypeVariable)candidate).asElement()
      : null;

    if (el instanceof TypeElement) {
      TypeElement element = (TypeElement) el;
      String fqn = element.getQualifiedName().toString();
      if (Object.class.getName().equals(fqn)) {
        return false;
      }
      else if ("org.springframework.util.MultiValueMap".equals(fqn) || Map.class.getName().equalsIgnoreCase(fqn)) {
        TypeMirror resolvedType = variableContext.resolveTypeVariables(candidate, env);
        if (resolvedType instanceof DeclaredType) {
          List<? extends TypeMirror> typeArgs = ((DeclaredType) resolvedType).getTypeArguments();
          if (typeArgs.size() == 2 && ((DecoratedTypeMirror<?>) typeArgs.get(0)).isInstanceOf(String.class) && ((DecoratedTypeMirror<?>) typeArgs.get(1)).isInstanceOf(String.class)) {
            return true;
          }
        }
      }
      else {
        TypeMirror superclass = element.getSuperclass();
        if (superclass != null && superclass.getKind() != TypeKind.NONE) {
          return isMapStringString(superclass, variableContext.push(element.getTypeParameters(), candidate instanceof DeclaredType ? ((DeclaredType) candidate).getTypeArguments() : new ArrayList<TypeMirror>()), env);
        }
      }
    }

    return false;
  }

  private static void gatherFormObjectParameters(TypeMirror type, ArrayList<RequestParameter> params, RequestMapping context) {
    if (type instanceof DeclaredType) {
      Set<String> methods = context.getHttpMethods();
      ResourceParameterType defaultType = methods.contains("POST") ? ResourceParameterType.FORM : ResourceParameterType.QUERY;
      DecoratedTypeElement typeDeclaration = (DecoratedTypeElement) ElementDecorator.decorate(((DeclaredType) type).asElement(), context.getContext().getContext().getProcessingEnvironment());
      for (VariableElement field : ElementFilter.fieldsIn(typeDeclaration.getEnclosedElements())) {
        DecoratedVariableElement decorated = (DecoratedVariableElement) field;
        if (!decorated.isFinal() && !decorated.isTransient() && decorated.isPublic()) {
          params.add(new SimpleRequestParameter(decorated, context, defaultType));
        }
      }

      for (PropertyElement property : typeDeclaration.getProperties()) {
        if (property.getSetter() != null) {
          params.add(new SimpleRequestParameter(property, context, defaultType));
        }
      }

      if (typeDeclaration.getKind() == ElementKind.CLASS) {
        gatherFormObjectParameters(typeDeclaration.getSuperclass(), params, context);
      }
    }
  }
}
