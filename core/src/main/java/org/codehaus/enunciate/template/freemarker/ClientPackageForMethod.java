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
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.*;

import org.codehaus.enunciate.ClientName;

/**
 * Gets the qualified package name for a package or type.
 *
 * @author Ryan Heaton
 */
public class ClientPackageForMethod implements TemplateMethodModelEx {

  private final TreeMap<String, String> conversions;
  private boolean useClientNameConversions = false;

  /**
   * @param conversions The conversions.
   */
  public ClientPackageForMethod(Map<String, String> conversions) {
    this.conversions = new TreeMap<String, String>(new Comparator<String>() {
      public int compare(String package1, String package2) {
        return package2.length() == package1.length() ? package1.compareTo(package2) : package2.length() - package1.length();
      }
    });

    if (conversions != null) {
      this.conversions.putAll(conversions);
    }
  }

  /**
   * Gets the client-side package for the type, type declaration, package, or their string values.
   *
   * @param list The arguments.
   * @return The string value of the client-side package.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The convertPackage method must have the class or package as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    return convertUnwrappedObject(unwrapped);
  }

  /**
   * Converts an unwrapped object.
   *
   * @param unwrapped The unwrapped object to convert.
   * @return The conversion.
   */
  public String convertUnwrappedObject(Object unwrapped) throws TemplateModelException {
    String conversion;
    if (unwrapped instanceof TypeMirror) {
      conversion = convert((TypeMirror) unwrapped);
    }
    else if (unwrapped instanceof TypeDeclaration) {
      conversion = convert((TypeDeclaration) unwrapped);
    }
    else if (unwrapped instanceof PackageDeclaration) {
      conversion = convert((PackageDeclaration) unwrapped);
    }
    else {
      conversion = convert(String.valueOf(unwrapped));
    }
    return conversion;
  }

  /**
   * Returns the client-side package value for the given type.
   *
   * @param typeMirror The type.
   * @return The client-side package value for the type.
   * @throws TemplateModelException If the type mirror cannot be converted for some reason.
   */
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    String conversion;
    if (typeMirror instanceof DeclaredType) {
      conversion = convert(((DeclaredType) typeMirror).getDeclaration());
    }
    else if (typeMirror instanceof ArrayType) {
      conversion = convert(((ArrayType) typeMirror).getComponentType());
    }
    else if (typeMirror instanceof TypeVariable) {
      conversion = "Object";
      TypeParameterDeclaration parameterDeclaration = ((TypeVariable) typeMirror).getDeclaration();
      if (parameterDeclaration != null && parameterDeclaration.getBounds() != null && parameterDeclaration.getBounds().size() > 0) {
        conversion = convert(parameterDeclaration.getBounds().iterator().next());
      }
    }
    else {
      conversion = String.valueOf(typeMirror);
    }
    return conversion;
  }

  /**
   * Returns the client-side package value for the given type declaration.
   *
   * @param declaration The declaration.
   * @return The client-side package value for the declaration.
   */
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    return convert(declaration.getPackage());
  }

  /**
   * Converts the package declaration to its client-side package value.
   *
   * @param packageDeclaration The package declaration.
   * @return The package declaration.
   */
  public String convert(PackageDeclaration packageDeclaration) {
    if (packageDeclaration == null) {
      return "";
    }

    ClientName specifiedName = isUseClientNameConversions() ? packageDeclaration.getAnnotation(ClientName.class) : null;
    return specifiedName == null ? convert(packageDeclaration.getQualifiedName()) : specifiedName.value();
  }

  /**
   * Converts the possible package to the specified client-side package, if any conversions are specified.
   *
   * @param fqn The package to convert.
   * @return The converted package, or the original if no conversions were specified for this value.
   */
  public String convert(String fqn) {
    //todo: support for regular expressions or wildcards?
    if (this.conversions.containsKey(fqn)) {
      return this.conversions.get(fqn);
    }

    for (String pkg : this.conversions.keySet()) {
      if (fqn.startsWith(pkg)) {
        String conversion = conversions.get(pkg);
        return conversion + fqn.substring(pkg.length());
      }
    }

    return fqn;
  }

  /**
   * Whether to use the client name conversions.
   *
   * @return Whether to use the client name conversions.
   */
  public boolean isUseClientNameConversions() {
    return useClientNameConversions;
  }

  /**
   * Whether to use the client name conversions.
   *
   * @param useClientNameConversions Whether to use the client name conversions.
   */
  public void setUseClientNameConversions(boolean useClientNameConversions) {
    this.useClientNameConversions = useClientNameConversions;
  }
}
