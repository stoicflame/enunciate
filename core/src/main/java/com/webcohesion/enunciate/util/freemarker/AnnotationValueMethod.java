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

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedAnnotationMirror;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class AnnotationValueMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The annotationValue method must have a declaration as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = DeepUnwrap.unwrap(from);

    String method = "value";
    if (list.size() > 1) {
      method = (String) DeepUnwrap.unwrap((TemplateModel) list.get(1));
    }

    if (unwrapped instanceof DecoratedAnnotationMirror) {
      return invoke(method, ((DecoratedAnnotationMirror) unwrapped));
    }

    throw new EnunciateException(String.format("Unsupported method %s on %s", method, unwrapped));
  }

  private Object invoke(String method, DecoratedAnnotationMirror annotation) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getAllElementValues();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals(method)) {
        return String.valueOf(entry.getValue().getValue());
      }
    }
    return null;
  }

}