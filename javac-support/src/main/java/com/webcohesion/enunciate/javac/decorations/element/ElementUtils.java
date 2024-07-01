/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Ryan Heaton
 */
public class ElementUtils {

  private ElementUtils() {
  }

  public static String findDeprecationMessage(DecoratedElement<?> el, JavaDocTagHandler tagHandler) {
    Deprecated deprecation = el.getAnnotation(Deprecated.class);
    String message = null;
    if (deprecation != null) {
      message = "";
    }

    JavaDoc.JavaDocTagList tagList = el.getJavaDoc(tagHandler).get("deprecated");
    if (tagList != null) {
      message = tagList.toString();
    }
    return message;
  }

  public static boolean isCollection(TypeElement declaration) {
    if (declaration != null) {
      String fqn = declaration.getQualifiedName().toString();
      if (Collection.class.getName().equals(fqn)) {
        return true;
      }
      else if (Object.class.getName().equals(fqn)) {
        return false;
      }
      else {
        TypeMirror superclass = declaration.getSuperclass();
        if (superclass instanceof DeclaredType && isCollection((TypeElement) ((DeclaredType) superclass).asElement())) {
          return true;
        }

        for (TypeMirror interfaceType : declaration.getInterfaces()) {
          if (interfaceType instanceof DeclaredType && isCollection((TypeElement) ((DeclaredType) interfaceType).asElement())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static boolean isStream(TypeElement declaration) {
    if (declaration != null) {
      String fqn = declaration.getQualifiedName().toString();
      if (Stream.class.getName().equals(fqn)) {
        return true;
      }
      else if (Object.class.getName().equals(fqn)) {
        return false;
      }
      else {
        TypeMirror superclass = declaration.getSuperclass();
        if (superclass instanceof DeclaredType && isStream((TypeElement) ((DeclaredType) superclass).asElement())) {
          return true;
        }

        for (TypeMirror interfaceType : declaration.getInterfaces()) {
          if (interfaceType instanceof DeclaredType && isStream((TypeElement) ((DeclaredType) interfaceType).asElement())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static boolean isMap(TypeElement declaration) {
    if (declaration != null) {
      String fqn = declaration.getQualifiedName().toString();
      if (Map.class.getName().equals(fqn)) {
        return true;
      }
      else if (Object.class.getName().equals(fqn)) {
        return false;
      }
      else {
        TypeMirror superclass = declaration.getSuperclass();
        if (superclass instanceof DeclaredType && isMap((TypeElement) ((DeclaredType) superclass).asElement())) {
          return true;
        }

        for (TypeMirror interfaceType : declaration.getInterfaces()) {
          if (interfaceType instanceof DeclaredType && isMap((TypeElement) ((DeclaredType) interfaceType).asElement())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static String capitalize(String string) {
    return Character.toUpperCase(string.charAt(0)) + string.substring(1);
  }

  /**
   * Check if the element is a class or record.
   *
   * @param element the element to test
   * @return true if it's a class or record
   */
  public static boolean isClassOrRecord(Element element) {
    return element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD;
  }

  /**
   * Get all fields in the class. If it is a java.lang.Record then get all the record components.
   *
   * @param clazz the element to inspect
   * @return a list of elements
   */
  public static List<Element> fieldsOrRecordComponentsIn(TypeElement clazz) {
    if (clazz.getKind() == ElementKind.RECORD) {
      List<Element> elements = new ArrayList<>();
      for (Element element : clazz.getEnclosedElements()) {
        if (element.getKind() == ElementKind.RECORD_COMPONENT) {
          elements.add(element);
        }
      }
      return elements;
    }
    else {
      return new ArrayList<>(ElementFilter.fieldsIn(clazz.getEnclosedElements()));
    }
  }

  public static class DefaultPropertySpec implements PropertySpec {

    protected final DecoratedProcessingEnvironment env;

    public DefaultPropertySpec(DecoratedProcessingEnvironment env) {
      this.env = env;
    }

    @Override
    public boolean isGetter(DecoratedExecutableElement executable) {
      return executable.isPublic() && executable.isGetter();
    }

    @Override
    public boolean isSetter(DecoratedExecutableElement executable) {
      return executable.isPublic() && executable.isSetter();
    }

    @Override
    public String getPropertyName(DecoratedExecutableElement method) {
      return method.getPropertyName();
    }

    @Override
    public String getSimpleName(DecoratedExecutableElement method) {
      return method.getPropertyName();
    }

    @Override
    public boolean isPaired(DecoratedExecutableElement getter, DecoratedExecutableElement setter) {
      if (getter == null) {
        return false;
      }

      if (!isGetter(getter)) {
        return false;
      }

      if (getter.getParameters().size() != 0) {
        return false;
      }

      if (setter != null) {
        if (!isSetter(setter)) {
          return false;
        }

        if (!getPropertyName(getter).equals(getPropertyName(setter))) {
          return false;
        }

        List<? extends VariableElement> setterParams = setter.getParameters();
        if ((setterParams == null) || (setterParams.size() != 1) || (!this.env.getTypeUtils().isSameType(getter.getReturnType(), setterParams.iterator().next().asType()))) {
          return false;
        }
      }

      return true;
    }

  }
}
