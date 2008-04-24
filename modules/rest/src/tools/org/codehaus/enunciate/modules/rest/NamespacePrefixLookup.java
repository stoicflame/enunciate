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

package org.codehaus.enunciate.modules.rest;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Utility for looking up a namespace prefix.
 *
 * @author Ryan Heaton
 */
public class NamespacePrefixLookup extends ApplicationObjectSupport {

  private final Map<String, String> ns2prefix;

  public NamespacePrefixLookup(Map<String, String> ns2prefix) {
    this.ns2prefix = Collections.unmodifiableMap(ns2prefix);
  }

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    Map prefixAwareBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), NamespacePrefixAware.class);
    for (Object prefixAwareBean : prefixAwareBeans.values()) {
      ((NamespacePrefixAware)prefixAwareBean).setNamespaceLookup(this);
    }
  }

  /**
   * Lookup the prefix for the specified namespace.
   *
   * @param namespace The namespace.
   * @return The prefix, or null if not found.
   */
  public String lookupPrefix(String namespace) {
    return this.ns2prefix.get(namespace);
  }

  /**
   * Get the map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespacesToPrefixes() {
    return this.ns2prefix;
  }
}
