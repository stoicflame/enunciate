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

import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * A JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public class SpringControllerAdvice extends DecoratedTypeElement {

  private final EnunciateSpringWebContext context;

  public SpringControllerAdvice(TypeElement delegate, EnunciateSpringWebContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;
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

  public List<RequestMappingAdvice> findRequestMappingAdvice(RequestMapping requestMapping) {
    List<AdviceScope> scope = new ArrayList<AdviceScope>();
    ControllerAdvice adviceInfo = getAnnotation(ControllerAdvice.class);
    if (adviceInfo != null) {
      Set<String> allPackages = new TreeSet<String>();
      allPackages.addAll(Arrays.asList(adviceInfo.value()));
      allPackages.addAll(Arrays.asList(adviceInfo.basePackages()));

      try {
        Class<?>[] classes = adviceInfo.basePackageClasses();
        for (Class<?> clazz : classes) {
          allPackages.add(clazz.getPackage().getName());
        }
      }
      catch (MirroredTypesException e) {
        List<? extends TypeMirror> mirrors = e.getTypeMirrors();
        for (TypeMirror mirror : mirrors) {
          if (mirror instanceof DeclaredType) {
            Element element = ((DeclaredType) mirror).asElement();
            while (element != null && (!(element instanceof PackageElement))) {
              element = element.getEnclosingElement();
            }

            if (element != null) {
              allPackages.add(((PackageElement) element).getQualifiedName().toString());
            }
          }
        }
      }
      scope.add(new PackageAdviceScope(allPackages));


      Set<String> allClasses = new TreeSet<String>();
      try {
        Class<?>[] classes = adviceInfo.assignableTypes();
        for (Class<?> clazz : classes) {
          allClasses.add(clazz.getName());
        }
      }
      catch (MirroredTypesException e) {
        List<? extends TypeMirror> mirrors = e.getTypeMirrors();
        for (TypeMirror mirror : mirrors) {
          if (mirror instanceof DeclaredType) {
            Element element = ((DeclaredType) mirror).asElement();
            if (element instanceof TypeElement) {
              allClasses.add(((TypeElement) element).getQualifiedName().toString());
            }
          }
        }
      }
      scope.add(new ClassAdviceScope(allClasses));

      Set<String> allAnnotations = new TreeSet<String>();
      try {
        Class<?>[] classes = adviceInfo.annotations();
        for (Class<?> clazz : classes) {
          allAnnotations.add(clazz.getName());
        }
      }
      catch (MirroredTypesException e) {
        List<? extends TypeMirror> mirrors = e.getTypeMirrors();
        for (TypeMirror mirror : mirrors) {
          if (mirror instanceof DeclaredType) {
            Element element = ((DeclaredType) mirror).asElement();
            if (element instanceof TypeElement) {
              allAnnotations.add(((TypeElement) element).getQualifiedName().toString());
            }
          }
        }
      }
      scope.add(new AnnotationAdviceScope(allAnnotations));

      if (allPackages.isEmpty() && allClasses.isEmpty() && allAnnotations.isEmpty()) {
        scope.clear();
        scope.add(new GlobalScope());
      }
    }
    else {
      scope.add(new ClassAdviceScope(new TreeSet<String>(Collections.singletonList(getQualifiedName().toString()))));
    }

    return findRequestMappingAdvice(requestMapping, this, scope, new TypeVariableContext());
  }

  protected List<RequestMappingAdvice> findRequestMappingAdvice(RequestMapping requestMapping, TypeElement controllerAdvice, List<AdviceScope> scope, TypeVariableContext variableContext) {
    if (controllerAdvice == null || controllerAdvice.getQualifiedName().toString().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    ArrayList<RequestMappingAdvice> advice = new ArrayList<RequestMappingAdvice>();
    for (ExecutableElement method : ElementFilter.methodsIn(controllerAdvice.getEnclosedElements())) {
      boolean applies = false;
      for (AdviceScope adviceScope : scope) {
        if (adviceScope.applies(requestMapping)) {
          applies = true;
          break;
        }
      }

      if (applies) {
        org.springframework.web.bind.annotation.ModelAttribute modelAttribute = method.getAnnotation(org.springframework.web.bind.annotation.ModelAttribute.class);
        if (modelAttribute != null) {
          advice.add(new RequestMappingAdvice(requestMapping, modelAttribute, method, this, variableContext, this.context));
        }
      }
    }

    //some methods may be specified by a superclass and/or implemented interface.  But the annotations on the current class take precedence.
    for (TypeMirror interfaceType : controllerAdvice.getInterfaces()) {
      if (interfaceType instanceof DeclaredType) {
        DeclaredType declared = (DeclaredType) interfaceType;
        TypeElement element = (TypeElement) declared.asElement();
        List<RequestMappingAdvice> interfaceMethods = findRequestMappingAdvice(requestMapping, element, scope, variableContext.push(element.getTypeParameters(), declared.getTypeArguments()));
        for (RequestMappingAdvice interfaceMethod : interfaceMethods) {
          if (!isOverridden(interfaceMethod, advice)) {
            advice.add(interfaceMethod);
          }
        }
      }
    }

    if (ElementUtils.isClassOrRecord(controllerAdvice)) {
      TypeMirror superclass = controllerAdvice.getSuperclass();
      if (superclass instanceof DeclaredType && ((DeclaredType)superclass).asElement() != null) {
        DeclaredType declared = (DeclaredType) superclass;
        TypeElement element = (TypeElement) declared.asElement();
        List<RequestMappingAdvice> superMethods = findRequestMappingAdvice(requestMapping, element, scope, variableContext.push(element.getTypeParameters(), declared.getTypeArguments()));
        for (RequestMappingAdvice superMethod : superMethods) {
          if (!isOverridden(superMethod, advice)) {
            advice.add(superMethod);
          }
        }
      }
    }

    return advice;
  }
}
