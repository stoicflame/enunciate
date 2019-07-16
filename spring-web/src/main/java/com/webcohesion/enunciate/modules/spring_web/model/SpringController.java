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

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;
import com.webcohesion.enunciate.util.IgnoreUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.security.RolesAllowed;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.lang.annotation.IncompleteAnnotationException;
import java.util.*;

/**
 * A Spring web controller.
 *
 * @author Ryan Heaton
 */
public class SpringController extends DecoratedTypeElement implements HasFacets {

  private final EnunciateSpringWebContext context;
  private final Set<String> paths;
  private final Set<String> consumesMime;
  private final Set<String> producesMime;
  private final org.springframework.web.bind.annotation.RequestMapping mappingInfo;
  private final List<RequestMapping> requestMappings;
  private final Set<Facet> facets = new TreeSet<Facet>();

  public SpringController(TypeElement delegate, EnunciateSpringWebContext context) {
    this(delegate, delegate.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class), context);
  }

  private SpringController(TypeElement delegate, org.springframework.web.bind.annotation.RequestMapping mappingInfo, EnunciateSpringWebContext context) {
    this(delegate, loadPaths(mappingInfo), mappingInfo, context);
  }

  private static Set<String> loadPaths(org.springframework.web.bind.annotation.RequestMapping mappingInfo) {
    TreeSet<String> paths = new TreeSet<String>();
    if (mappingInfo != null) {
      try {
        paths.addAll(Arrays.asList(mappingInfo.path()));
      }
      catch (IncompleteAnnotationException e) {
        //fall through; 'mappingInfo.path' was added in 4.2.
      }

      paths.addAll(Arrays.asList(mappingInfo.value()));
    }
    if (paths.isEmpty()) {
      paths.add("");
    }
    return paths;
  }

  private SpringController(TypeElement delegate, Set<String> paths, org.springframework.web.bind.annotation.RequestMapping mappingInfo, EnunciateSpringWebContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());

    this.context = context;
    this.paths = paths;
    this.mappingInfo = mappingInfo;

    Set<String> consumes = new TreeSet<String>();
    if (mappingInfo != null && mappingInfo.consumes().length > 0) {
      for (String mt : mappingInfo.consumes()) {
        if (mt.startsWith("!")) {
          continue;
        }

        int colonIndex = mt.indexOf(';');
        if (colonIndex > 0) {
          mt = mt.substring(0, colonIndex);
        }

        consumes.add(mt);
      }
    }
    else {
      consumes.add("*/*");
    }
    this.consumesMime = Collections.unmodifiableSet(consumes);

    Set<String> produces = new TreeSet<String>();
    if (mappingInfo != null && mappingInfo.produces().length > 0) {
      for (String mt : mappingInfo.produces()) {
        if (mt.startsWith("!")) {
          continue;
        }

        int colonIndex = mt.indexOf(';');
        if (colonIndex > 0) {
          mt = mt.substring(0, colonIndex);
        }

        produces.add(mt);
      }
    }
    else {
      produces.add("*/*");
    }
    this.producesMime = Collections.unmodifiableSet(produces);

    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    this.requestMappings = Collections.unmodifiableList(getRequestMappings(delegate, new TypeVariableContext(), context));
  }

  /**
   * Get all the resource methods for the specified type.
   *
   * @param delegate The type.
   * @param context The context
   * @return The resource methods.
   */
  protected List<RequestMapping> getRequestMappings(final TypeElement delegate, TypeVariableContext variableContext, EnunciateSpringWebContext context) {
    if (delegate == null || delegate.getQualifiedName().toString().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    ArrayList<RequestMapping> requestMappings = new ArrayList<RequestMapping>();
    for (ExecutableElement method : ElementFilter.methodsIn(delegate.getEnclosedElements())) {
      if (IgnoreUtils.isIgnored(method)) {
        continue;
      }

      RequestMethod[] requestMethods = findRequestMethods(method);
      if (requestMethods != null) {
        String[] consumes = findConsumes(method);
        String[] produces = findProduces(method);
        Set<String> subpaths = findSubpaths(method);

        if (subpaths.isEmpty()) {
          subpaths.add("");
        }

        for (String path : getPaths()) {
          for (String subpath : subpaths) {
            if (!path.endsWith("/") && !subpath.isEmpty() && !subpath.startsWith("/")) {
              path = path + "/";
            }
            requestMappings.add(new RequestMapping(extractPathComponents(path + subpath), requestMethods, consumes, produces, method, this, variableContext, context));
          }
        }

        if (requestMappings.isEmpty()) {
          requestMappings.add(new RequestMapping(new ArrayList<PathSegment>(), requestMethods, consumes, produces, method, this, variableContext, context));
        }

      }
    }

    //some methods may be specified by a superclass and/or implemented interface.  But the annotations on the current class take precedence.
    for (TypeMirror interfaceType : delegate.getInterfaces()) {
      if (interfaceType instanceof DeclaredType) {
        DeclaredType declared = (DeclaredType) interfaceType;
        TypeElement element = (TypeElement) declared.asElement();
        List<RequestMapping> interfaceMethods = getRequestMappings(element, variableContext.push(element.getTypeParameters(), declared.getTypeArguments()), context);
        for (RequestMapping interfaceMethod : interfaceMethods) {
          if (!isOverridden(interfaceMethod, requestMappings)) {
            requestMappings.add(interfaceMethod);
          }
        }
      }
    }

    if (delegate.getKind() == ElementKind.CLASS) {
      TypeMirror superclass = delegate.getSuperclass();
      if (superclass instanceof DeclaredType && ((DeclaredType)superclass).asElement() != null) {
        DeclaredType declared = (DeclaredType) superclass;
        TypeElement element = (TypeElement) declared.asElement();
        List<RequestMapping> superMethods = getRequestMappings(element, variableContext.push(element.getTypeParameters(), declared.getTypeArguments()), context);
        for (RequestMapping superMethod : superMethods) {
          if (!isOverridden(superMethod, requestMappings)) {
            requestMappings.add(superMethod);
          }
        }
      }
    }

    return requestMappings;
  }

  private RequestMethod[] findRequestMethods(ExecutableElement method) {
    org.springframework.web.bind.annotation.RequestMapping requestMapping = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
    if (requestMapping != null) {
      return requestMapping.method();
    }
    else {
      List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
      if (annotations != null) {
        for (AnnotationMirror annotation : annotations) {
          DeclaredType annotationType = annotation.getAnnotationType();
          if (annotationType != null) {
            Element annotationElement = annotationType.asElement();
            if (annotationElement != null) {
              requestMapping = annotationElement.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
              if (requestMapping != null) {
                return requestMapping.method();
              }
            }
          }
        }
      }
    }
    return null;
  }

  private String[] findConsumes(ExecutableElement method) {
    org.springframework.web.bind.annotation.RequestMapping requestMapping = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
    if (requestMapping != null) {
      return requestMapping.consumes();
    }
    else {
      List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
      if (annotations != null) {
        for (AnnotationMirror annotation : annotations) {
          DeclaredType annotationType = annotation.getAnnotationType();
          if (annotationType != null) {
            Element annotationElement = annotationType.asElement();
            if (annotationElement != null) {
              requestMapping = annotationElement.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
              if (requestMapping != null) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                  if (entry.getKey().getSimpleName().contentEquals("consumes")) {
                    Object value = entry.getValue().getValue();
                    if (value instanceof List) {
                      String[] consumes = new String[((List)value).size()];
                      for (int i = 0; i < ((List) value).size(); i++) {
                        AnnotationValue valueItem = (AnnotationValue) ((List) value).get(i);
                        consumes[i] = String.valueOf(valueItem.getValue());
                      }
                      return consumes;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  private Set<String> findSubpaths(ExecutableElement method) {
    org.springframework.web.bind.annotation.RequestMapping requestMapping = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
    if (requestMapping != null) {
      Set<String> subpaths = new TreeSet<String>();
      try {
        subpaths.addAll(Arrays.asList(requestMapping.path()));
      }
      catch (IncompleteAnnotationException e) {
        //fall through; 'mappingInfo.path' was added in 4.2.
      }
      subpaths.addAll(Arrays.asList(requestMapping.value()));
      return subpaths;
    }
    else {
      List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
      if (annotations != null) {
        for (AnnotationMirror annotation : annotations) {
          DeclaredType annotationType = annotation.getAnnotationType();
          if (annotationType != null) {
            Element annotationElement = annotationType.asElement();
            if (annotationElement != null) {
              requestMapping = annotationElement.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
              if (requestMapping != null) {
                Set<String> subpaths = new TreeSet<String>();
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                  if (entry.getKey().getSimpleName().contentEquals("value") || entry.getKey().getSimpleName().contentEquals("path")) {
                    Object value = entry.getValue().getValue();
                    if (value instanceof List) {
                      for (int i = 0; i < ((List) value).size(); i++) {
                        AnnotationValue valueItem = (AnnotationValue) ((List) value).get(i);
                        subpaths.add(String.valueOf(valueItem.getValue()));
                      }
                    }
                  }
                }

                return subpaths;
              }
            }
          }
        }
      }
    }
    return null;
  }

  private String[] findProduces(ExecutableElement method) {
    org.springframework.web.bind.annotation.RequestMapping requestMapping = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
    if (requestMapping != null) {
      return requestMapping.produces();
    }
    else {
      List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();
      if (annotations != null) {
        for (AnnotationMirror annotation : annotations) {
          DeclaredType annotationType = annotation.getAnnotationType();
          if (annotationType != null) {
            Element annotationElement = annotationType.asElement();
            if (annotationElement != null) {
              requestMapping = annotationElement.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
              if (requestMapping != null) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                  if (entry.getKey().getSimpleName().contentEquals("produces")) {
                    Object value = entry.getValue().getValue();
                    if (value instanceof List) {
                      String[] produces = new String[((List)value).size()];
                      for (int i = 0; i < ((List) value).size(); i++) {
                        AnnotationValue valueItem = (AnnotationValue) ((List) value).get(i);
                        produces[i] = String.valueOf(valueItem.getValue());
                      }
                      return produces;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Extracts out the components of a path.
   *
   * @param path The path.
   */
  protected static List<PathSegment> extractPathComponents(String path) {
    List<PathSegment> components = new ArrayList<PathSegment>();
    if (path != null) {
      StringBuilder value = new StringBuilder();
      if (!path.startsWith("/")) {
        value.append("/");//first path segment should always start with "/"
      }

      StringBuilder variable = new StringBuilder();
      StringBuilder regexp = new StringBuilder();
      int inBrace = 0;
      boolean definingRegexp = false;
      for (int i = 0; i < path.length(); i++) {
        char ch = path.charAt(i);
        if (ch == '{') {
          inBrace++;

          if (inBrace == 1) {
            //outer brace defines new path segment
            if (value.length() > 0) {
              components.add(new PathSegment(value.toString(), variable.length() > 0 ? variable.toString() : null, regexp.length() > 0 ? regexp.toString() : null));
            }

            value = new StringBuilder();
            variable = new StringBuilder();
            regexp = new StringBuilder();
          }
        }
        else if (ch == '}') {
          inBrace--;
          if (inBrace == 0) {
            definingRegexp = false;
          }
        }
        else if (inBrace == 1 && ch == ':') {
          definingRegexp = true;
          continue;
        }
        else if (!definingRegexp && !Character.isWhitespace(ch) && inBrace > 0) {
          variable.append(ch);
        }

        if (definingRegexp) {
          regexp.append(ch);
        }
        else if (!Character.isWhitespace(ch)) {
          value.append(ch);
        }
      }

      if (value.length() > 0) {
        components.add(new PathSegment(value.toString(), variable.length() > 0 ? variable.toString() : null, regexp.length() > 0 ? regexp.toString() : null));
      }
    }
    return components;
  }

  /**
   * Whether the specified method is overridden by any of the methods in the specified list.
   *
   * @param method The method.
   * @param resourceMethods The method list.
   * @return If the methdo is overridden by any of the methods in the list.
   */
  protected boolean isOverridden(ExecutableElement method, ArrayList<? extends ExecutableElement> resourceMethods) {
    Elements decls = this.env.getElementUtils();


    for (ExecutableElement resourceMethod : resourceMethods) {
      if (decls.overrides(resourceMethod, method, (TypeElement) resourceMethod.getEnclosingElement())) {
        return true;
      }
    }

    return false;
  }

  public EnunciateSpringWebContext getContext() {
    return context;
  }

  /**
   * The path to this resource.
   *
   * @return The path to this resource.
   */
  public final Set<String> getPaths() {
    return this.paths;
  }

  /**
   * The MIME types that the methods on this resource consumes (possibly overridden).
   *
   * @return The MIME types that the methods on this resource consumes.
   */
  public Set<String> getConsumesMime() {
    return consumesMime;
  }

  /**
   * The MIME types that the methods on this resource consumes (possibly overridden).
   *
   * @return The MIME types that the methods on this resource consumes.
   */
  public Set<String> getProducesMime() {
    return producesMime;
  }

  /**
   * The resource methods.
   *
   * @return The resource methods.
   */
  public List<RequestMapping> getRequestMappings() {
    return requestMappings;
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

  /**
   * Get the request methods applicable to this controller.
   *
   * @return The request methods applicable to this controller.
   */
  public Set<RequestMethod> getApplicableMethods() {
    EnumSet<RequestMethod> applicableMethods = EnumSet.allOf(RequestMethod.class);
    if (this.mappingInfo != null) {
      RequestMethod[] methods = this.mappingInfo.method();
      if (methods.length > 0) {
        applicableMethods.retainAll(Arrays.asList(methods));
      }
    }
    return applicableMethods;
  }

  /**
   * The security roles for this resource.
   *
   * @return The security roles for this resource.
   */
  public Set<String> getSecurityRoles() {
    TreeSet<String> roles = new TreeSet<String>();
    RolesAllowed rolesAllowed = getAnnotation(RolesAllowed.class);
    if (rolesAllowed != null) {
      Collections.addAll(roles, rolesAllowed.value());
    }
    return roles;
  }
}
