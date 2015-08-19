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

package com.webcohesion.enunciate.modules.jaxb.util;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * Gets the RootElement for the supplied TypeDefinition
 *
 * @author Ryan Heaton
 */
public class FindRootElementMethod implements TemplateMethodModelEx {

  private final EnunciateJaxbContext context;

  public FindRootElementMethod(EnunciateJaxbContext context) {
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
      throw new TemplateModelException("The findRootElementMethod method must have a class declaration as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = new BeansWrapperBuilder(Configuration.getVersion()).build().unwrap(from);
    if (!(unwrapped instanceof TypeElement)) {
      throw new TemplateModelException("A type element must be provided.");
    }

    TypeElement def = (TypeElement) unwrapped;
    return context.findElementDeclaration(def);
  }

}