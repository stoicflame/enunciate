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

import com.sun.mirror.declaration.EnumConstantDeclaration;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.contract.Facet;
import org.codehaus.enunciate.contract.HasFacets;
import org.codehaus.enunciate.util.FacetFilter;

import java.util.Collection;
import java.util.List;

/**
 * A method used in templates to output the prefix for a given namespace.
 *
 * @author Ryan Heaton
 */
public class IsFacetExcludedMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The IsFacetExcluded method must have a declaration as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (unwrapped instanceof Collection) {
      if (!((Collection)unwrapped).isEmpty()) {
        for (Object item : (Collection) unwrapped) {
          if (HasFacets.class.isInstance(item) && !FacetFilter.accept((HasFacets) item)) {
            return false;
          }
        }
        return true;
      }
      return false;
    }
    else {
      return HasFacets.class.isInstance(unwrapped) && !FacetFilter.accept((HasFacets) unwrapped);
    }
  }

}