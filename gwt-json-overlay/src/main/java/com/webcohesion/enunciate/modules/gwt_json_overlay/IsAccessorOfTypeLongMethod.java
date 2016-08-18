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
package com.webcohesion.enunciate.modules.gwt_json_overlay;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jackson.model.Accessor;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

import javax.lang.model.type.TypeKind;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Template method used to determine whether a given accessor is of type long (special handing for gwt overlay types).
 *
 * @author Ryan Heaton
 */
public class IsAccessorOfTypeLongMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The isDefinedGlobally method must have a local element declaration as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = DeepUnwrap.unwrap(from);
    if (unwrapped instanceof Accessor) {
      Accessor accessor = (Accessor) unwrapped;
      DecoratedTypeMirror decorated = accessor.getAccessorType();
      if (decorated.isPrimitive()) {
        return decorated.getKind() == TypeKind.LONG;
      }
      else if (decorated.isInstanceOf(Long.class.getName())) {
        return true;
      }
      else if (decorated.isInstanceOf(Date.class.getName())) {
        return true;
      }
      else if (decorated.isInstanceOf(Calendar.class.getName())) {
        return true;
      }
    }
    else if (unwrapped instanceof com.webcohesion.enunciate.modules.jackson1.model.Accessor) {
      com.webcohesion.enunciate.modules.jackson1.model.Accessor accessor = (com.webcohesion.enunciate.modules.jackson1.model.Accessor) unwrapped;
      DecoratedTypeMirror decorated = accessor.getAccessorType();
      if (decorated.isPrimitive()) {
        return decorated.getKind() == TypeKind.LONG;
      }
      else if (decorated.isInstanceOf(Long.class.getName())) {
        return true;
      }
      else if (decorated.isInstanceOf(Date.class.getName())) {
        return true;
      }
      else if (decorated.isInstanceOf(Calendar.class.getName())) {
        return true;
      }
    }
    else {
      throw new TemplateModelException("The IsAccessorOfTypeLong method must have an accessor as a parameter.");
    }

    return false;
  }

}