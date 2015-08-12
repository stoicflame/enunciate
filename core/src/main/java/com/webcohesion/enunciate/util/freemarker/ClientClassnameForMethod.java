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

package com.webcohesion.enunciate.util.freemarker;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Used to output a reference for an accessor or parameter or declaration.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends ClientPackageForMethod {

  private final LinkedList<String> typeParameterDeclarationStack = new LinkedList<String>();

  public ClientClassnameForMethod(Map<String, String> conversions, EnunciateContext context) {
    super(conversions, context);
  }

  @Override
  public String convertUnwrappedObject(Object unwrapped) throws TemplateModelException {
    if (unwrapped instanceof HasClientConvertibleType) {
      return convert((HasClientConvertibleType) unwrapped);
    }
    else if (unwrapped instanceof TypeParameterElement) {
      return convert((TypeParameterElement) unwrapped);
    }
    else {
      return super.convertUnwrappedObject(unwrapped);
    }
  }

  /**
   * Converts the specified type parameter declaration.
   *
   * @param typeParameterElement The type parameter declaration.
   * @return The type parameter declaration.
   */
  public String convert(TypeParameterElement typeParameterElement) throws TemplateModelException {
    String conversion = typeParameterElement.getSimpleName().toString();
    if (typeParameterElement.getBounds() != null && !typeParameterElement.getBounds().isEmpty() && !typeParameterDeclarationStack.contains(conversion)) {
      typeParameterDeclarationStack.addFirst(conversion);
      try {
        conversion += " extends " + convert(typeParameterElement.getBounds().iterator().next());
      }
      finally {
        typeParameterDeclarationStack.removeFirst();
      }
    }

    return conversion;
  }

  /**
   * Converts the specified implicit child element.
   *
   * @param element The implicit child element.
   * @return The conversion.
   */
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    TypeMirror elementType = element.getClientConvertibleType();
    return convert(elementType);
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    String conversion;

    if (typeMirror instanceof WildcardType) {
      WildcardType wildCard = (WildcardType) typeMirror;
      if (wildCard.getSuperBound() != null) {
        conversion = "? super " + convert(wildCard.getSuperBound());
      }
      else if (wildCard.getExtendsBound() != null) {
        conversion = "? extends " + convert(wildCard.getExtendsBound());
      }
      else {
        conversion = "?";
      }
    }
    else if (typeMirror instanceof TypeVariable) {
      TypeParameterElement declaration = (TypeParameterElement) ((TypeVariable) typeMirror).asElement();
      if (declaration != null && declaration.getBounds() != null && !declaration.getBounds().isEmpty()) {
        conversion = convert(declaration.getBounds().get(0));
      }
      else {
        conversion = convert(Object.class.getName());
      }
    }
    else {
      conversion = super.convert(typeMirror);
      boolean isArray = typeMirror.getKind() == TypeKind.ARRAY;

      if (typeMirror instanceof DeclaredType) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> actualTypeArguments = declaredType.getTypeArguments();
        if (actualTypeArguments.size() > 0) {
          StringBuilder typeArgs = new StringBuilder("<");
          Iterator<? extends TypeMirror> it = actualTypeArguments.iterator();
          while (it.hasNext()) {
            TypeMirror mirror = it.next();
            typeArgs.append(convert(mirror));
            if (it.hasNext()) {
              typeArgs.append(", ");
            }
          }
          typeArgs.append(">");
          conversion += typeArgs;
        }
      }

      if (isArray) {
        conversion += "[]";
      }
    }

    return conversion;

  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    PackageElement pkg = this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration);
    String convertedPackage = convertPackage(pkg);
    String simpleName = declaration.getSimpleName().toString();
    return convertedPackage + getPackageSeparator() + simpleName;
  }

  protected String convertPackage(PackageElement pkg) {
    return super.convert(pkg);
  }

  protected String getPackageSeparator() {
    return ".";
  }

  @Override
  public String convert(PackageElement packageDeclaration) {
    throw new UnsupportedOperationException("packages don't have a classname.");
  }
}