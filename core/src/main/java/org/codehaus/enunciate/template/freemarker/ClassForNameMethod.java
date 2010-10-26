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

import com.sun.mirror.declaration.TypeDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Outputs a string representing a class that can be loaded by Class.forName.
 *
 * @author Ryan Heaton
 */
public class ClassForNameMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The ClassForNameMethod method must have a type definition as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (unwrapped instanceof TypeDeclaration) {
      TypeDeclaration typeDecl = (TypeDeclaration) unwrapped;
      StringBuilder builder = new StringBuilder();
      if (typeDecl.getPackage() != null) {
        builder.append(typeDecl.getPackage().getQualifiedName());
      }
      LinkedList<String> innerClassStack = new LinkedList<String>();
      innerClassStack.addFirst(typeDecl.getSimpleName());
      while (typeDecl.getDeclaringType() != null) {
        typeDecl = typeDecl.getDeclaringType();
        innerClassStack.addFirst(typeDecl.getSimpleName());
      }

      builder.append('.');
      Iterator<String> it = innerClassStack.iterator();
      while (it.hasNext()) {
        builder.append(it.next());
        if (it.hasNext()) {
          builder.append('$');
        }
      }
      
      return builder.toString();
    }
    else {
      throw new TemplateModelException("The ClassForNameMethod method must have a type definition as a parameter.");
    }
  }

}