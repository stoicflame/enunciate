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

package org.codehaus.enunciate.template.freemarker;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Used to output a reference for an accessor or parameter or declaration.
 *
 * @author Ryan Heaton
 */
public class ClassnameForMethod extends ClientPackageForMethod {

  private static final ThreadLocal<Boolean> FORCE_NOT_15 = new ThreadLocal<Boolean>();

  private boolean jdk15 = false;

  public ClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  public Object exec(List list) throws TemplateModelException {
    FORCE_NOT_15.set(list.size() > 1 && Boolean.TRUE.equals(BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(0))));
    try {
      return super.exec(list);
    }
    finally {
      FORCE_NOT_15.remove();
    }
  }

  @Override
  public String convertUnwrappedObject(Object unwrapped) throws TemplateModelException {
    if (unwrapped instanceof Accessor) {
      return convert((Accessor) unwrapped);
    }
    else if (unwrapped instanceof ImplicitChildElement) {
      return convert((ImplicitChildElement) unwrapped);
    }
    else {
      return super.convertUnwrappedObject(unwrapped);
    }
  }

  /**
   * Converts the specified implicit child element.
   *
   * @param childElement The implicit child element.
   * @return The conversion.
   */
  public String convert(ImplicitChildElement childElement) throws TemplateModelException {
    TypeMirror elementType = childElement.getType();
    return convert(elementType);
  }

  /**
   * Converts the type of an accessor.
   *
   * @param accessor The accessor.
   * @return The accessor.
   */
  public String convert(Accessor accessor) throws TemplateModelException {
    TypeMirror accessorType = accessor.getAccessorType();
    return convert(accessorType);
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    String conversion = super.convert(typeMirror);
    boolean isArray = typeMirror instanceof ArrayType;

    //if we're using converting to a java 5+ client code, take into account the type arguments.
    if ((isJdk15()) && (typeMirror instanceof DeclaredType)) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      Collection<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments();
      if (actualTypeArguments.size() > 0) {
        StringBuilder typeArgs = new StringBuilder("<");
        Iterator<TypeMirror> it = actualTypeArguments.iterator();
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
    return conversion;

  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    PackageDeclaration pkg = declaration.getPackage();
    String convertedPackage = convertPackage(pkg);
    String simpleName = declaration.getSimpleName();
    return convertedPackage + getPackageSeparator() + simpleName;
  }

  protected String convertPackage(PackageDeclaration pkg) {
    return super.convert(pkg);
  }

  protected String getPackageSeparator() {
    return ".";
  }

  @Override
  public String convert(PackageDeclaration packageDeclaration) {
    throw new UnsupportedOperationException("packages don't have a classname.");
  }

  /**
   * Whether this converter is enabled to output jdk 15 compatible classes.
   *
   * @return Whether this converter is enabled to output jdk 15 compatible classes.
   */
  public boolean isJdk15() {
    return jdk15 && (FORCE_NOT_15.get() == null || !FORCE_NOT_15.get());
  }

  /**
   * Whether this converter is enabled to output jdk 15 compatible classes.
   *
   * @param jdk15 Whether this converter is enabled to output jdk 15 compatible classes.
   */
  public void setJdk15(boolean jdk15) {
    this.jdk15 = jdk15;
  }
}