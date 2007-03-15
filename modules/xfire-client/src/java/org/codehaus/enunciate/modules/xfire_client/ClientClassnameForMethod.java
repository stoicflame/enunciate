/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.xfire_client;

import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import freemarker.template.TemplateModelException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Converts a fully-qualified class name to its alternate client fully-qualified class name.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends ClientPackageForMethod {

  private boolean jdk15 = false;

  public ClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  protected String convert(TypeMirror typeMirror) throws TemplateModelException {
    boolean isArray = typeMirror instanceof ArrayType;
    String conversion = super.convert(typeMirror);

    //if we're using converting to a java 5+ client code, take into account the type arguments.
    if ((this.jdk15) && (typeMirror instanceof DeclaredType)) {
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
  protected String convert(TypeDeclaration declaration) {
    String convertedPackage;
    PackageDeclaration pckg = declaration.getPackage();
    if (pckg == null) {
      convertedPackage = "";
    }
    else {
      convertedPackage = super.convert(pckg.getQualifiedName());
    }

    return convertedPackage + "." + declaration.getSimpleName();
  }

  @Override
  protected String convert(PackageDeclaration packageDeclaration) {
    throw new UnsupportedOperationException("packages don't have a client classname.");
  }

  /**
   * Whether this converter is enabled to output jdk 15 compatible classes.
   *
   * @return Whether this converter is enabled to output jdk 15 compatible classes.
   */
  public boolean isJdk15() {
    return jdk15;
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
