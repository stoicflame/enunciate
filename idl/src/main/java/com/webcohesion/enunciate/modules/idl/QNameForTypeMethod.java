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

package com.webcohesion.enunciate.modules.idl;

import com.sun.mirror.type.ClassType;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Template method used to determine qname for a given type mirror.
 *
 * @author Ryan Heaton
 */
public class QNameForTypeMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The QNameForType method must have a type mirror as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (unwrapped instanceof ClassType) {
      TypeDefinition typeDefinition = ((EnunciateFreemarkerModel) FreemarkerModel.get()).findTypeDefinition(((ClassType) unwrapped).getDeclaration());
      return new QName(typeDefinition.getNamespace() == null ? "" : typeDefinition.getNamespace(), typeDefinition.getName());
    }

    throw new TemplateModelException("Unable to find qname for " + unwrapped);
  }
}