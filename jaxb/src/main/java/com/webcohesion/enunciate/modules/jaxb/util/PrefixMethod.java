/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxb.util;

import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
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

  private final Map<String, String> namespacePrefixes;

  public PrefixMethod(Map<String, String> namespacePrefixes) {
    this.namespacePrefixes = namespacePrefixes;
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

    Object param1 = list.get(0);
    String namespace = param1 instanceof String ? (String) param1 : (String) FreemarkerUtil.unwrap((TemplateModel) param1);
    String prefix = lookupPrefix(namespace);
    if (prefix == null) {
      throw new TemplateModelException("No prefix specified for {" + namespace + "}");
    }
    return prefix;
  }

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected String lookupPrefix(String namespace) {
    return this.namespacePrefixes.get(namespace);
  }

}
