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
package com.webcohesion.enunciate.util.freemarker;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.metadata.ClientName;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gets the qualified package name for a package or type.
 *
 * @author Ryan Heaton
 */
public class ClientPackageForMethod implements TemplateMethodModelEx {

  protected final TreeMap<String, String> conversions;
  protected final EnunciateContext context;

  /**
   * @param conversions The conversions.
   */
  public ClientPackageForMethod(Map<String, String> conversions, EnunciateContext context) {
    this.conversions = new TreeMap<String, String>(new Comparator<String>() {
      public int compare(String package1, String package2) {
        return package2.length() == package1.length() ? package1.compareTo(package2) : package2.length() - package1.length();
      }
    });

    if (conversions != null) {
      this.conversions.putAll(conversions);
    }

    this.context = context;
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

    Object unwrapped = unwrap(list.get(0));
    return convertUnwrappedObject(unwrapped);
  }

  protected Object unwrap(Object wrapped) throws TemplateModelException {
    return wrapped instanceof TemplateModel ? new BeansWrapperBuilder(Configuration.getVersion()).build().unwrap((TemplateModel) wrapped) : wrapped;
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
    else if (unwrapped instanceof TypeElement) {
      conversion = convert((TypeElement) unwrapped);
    }
    else if (unwrapped instanceof PackageElement) {
      conversion = convert((PackageElement) unwrapped);
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
      conversion = convert((TypeElement) ((DeclaredType) typeMirror).asElement());
    }
    else if (typeMirror instanceof ArrayType) {
      conversion = convert(((ArrayType) typeMirror).getComponentType());
    }
    else if (typeMirror instanceof TypeVariable) {
      conversion = "Object";
      VariableElement parameterDeclaration = (VariableElement) ((TypeVariable) typeMirror).asElement();
      if (parameterDeclaration != null && ((TypeVariable) typeMirror).getUpperBound() != null) {
        conversion = convert(((TypeVariable) typeMirror).getUpperBound());
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
  public String convert(TypeElement declaration) throws TemplateModelException {
    return convert(this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration));
  }

  /**
   * Converts the package declaration to its client-side package value.
   *
   * @param packageDeclaration The package declaration.
   * @return The package declaration.
   */
  public String convert(PackageElement packageDeclaration) {
    if (packageDeclaration == null) {
      return "";
    }

    ClientName specifiedName = packageDeclaration.getAnnotation(ClientName.class);
    return specifiedName == null ? convert(packageDeclaration.getQualifiedName().toString()) : specifiedName.value();
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

}
