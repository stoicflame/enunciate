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

package org.codehaus.enunciate.modules.rest.xml;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * A namespace prefix mapper.
 * 
 * @author Ryan Heaton
 */
public class PrefixMapper extends NamespacePrefixMapper {

  private final Map<String, String> ns2prefix;

  public PrefixMapper(Map<String, String> ns2prefix) {
    if (ns2prefix == null) {
      ns2prefix = new HashMap<String, String>();
    }
    this.ns2prefix = ns2prefix;
  }

  public String getPreferredPrefix(String nsuri, String suggestion, boolean defaultPossible) {
    return this.ns2prefix.containsKey(nsuri) ? this.ns2prefix.get(nsuri) : suggestion;
  }
}
