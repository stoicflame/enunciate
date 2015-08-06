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

import com.webcohesion.enunciate.metadata.ClientName;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import java.util.Iterator;
import java.util.List;

/**
 * Gets the client-side component type for the specified classname.
 *
 * @author Ryan Heaton
 */
public class SimpleNameWithParamsMethod implements TemplateMethodModelEx {

  private final ClientClassnameForMethod typeConversion;

  public SimpleNameWithParamsMethod(ClientClassnameForMethod typeConversion) {
    this.typeConversion = typeConversion;
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
    BeansWrapper wrapper = new BeansWrapperBuilder(Configuration.getVersion()).build();
    Object unwrapped = wrapper.unwrap(from);
    if (!(unwrapped instanceof TypeElement)) {
      throw new TemplateModelException("A type element must be provided.");
    }

    boolean noParams = list.size() > 1 && Boolean.FALSE.equals(wrapper.unwrap((TemplateModel) list.get(1)));
    TypeElement declaration = (TypeElement) unwrapped;
    String simpleNameWithParams = declaration.getAnnotation(ClientName.class) != null ? declaration.getAnnotation(ClientName.class).value() : declaration.getSimpleName().toString();
    if (!noParams && declaration.getTypeParameters() != null && !declaration.getTypeParameters().isEmpty()) {
      simpleNameWithParams += "<";
      Iterator<? extends TypeParameterElement> paramIt = declaration.getTypeParameters().iterator();
      while (paramIt.hasNext()) {
        simpleNameWithParams += this.typeConversion.convert(paramIt.next());
        if (paramIt.hasNext()) {
          simpleNameWithParams += ", ";
        }
      }
      simpleNameWithParams += ">";
    }
    return simpleNameWithParams;
  }

}