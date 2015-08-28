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

package com.webcohesion.enunciate.modules.c_client;

import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;
import java.util.Map;

/**
 * A method used in templates to output the prefix for a given namespace.
 *
 * @author Ryan Heaton
 */
public class PrefixMethod implements TemplateMethodModelEx {

  private final Map<String, String> ns2prefix;

  public PrefixMethod(Map<String, String> ns2prefix) {
    this.ns2prefix = ns2prefix;
  }

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The prefix method must have a namespace as a parameter.");
    }

    String namespace = (String) new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().unwrap((TemplateModel) list.get(0));
    String prefix = this.ns2prefix.get(namespace);
    if (prefix == null) {
      throw new TemplateModelException("No prefix specified for {" + namespace + "}");
    }
    return CXMLClientModule.scrubIdentifier(prefix);
  }

}