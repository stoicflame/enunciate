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

package org.codehaus.enunciate.modules.jersey;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import java.util.Properties;

/**
 * A namespace prefix mapper.
 *
 * @author Ryan Heaton
 */
public class PrefixMapper extends NamespacePrefixMapper {

  public static final String DEFAULT_NAMESPACE_ALIAS = "--DEFAULT_NAMESPACE_ALIAS--";

  private final Properties ns2prefix;

  public PrefixMapper(Properties ns2prefix) {
    this.ns2prefix = new Properties();
    if (ns2prefix != null) {
      this.ns2prefix.putAll(ns2prefix);
    }
  }

  public String getPreferredPrefix(String nsuri, String suggestion, boolean requirePrefix) {
    String prefix = this.ns2prefix.containsKey(nsuri) ? this.ns2prefix.getProperty(nsuri) : suggestion;
    if (!requirePrefix && nsuri.equals(this.ns2prefix.get(DEFAULT_NAMESPACE_ALIAS))) {
      prefix = "";
    }
    return prefix;
  }
}