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

package org.codehaus.enunciate.modules.xml;

import junit.framework.TestCase;

import java.util.Arrays;

import freemarker.template.TemplateModelException;

/**
 * @author Ryan Heaton
 */
public class TestPrefixMethod extends TestCase {

  /**
   * Tests looking up the namespace.
   */
  public void testLookupNamespace() throws Exception {
    PrefixMethod prefixMethod = new PrefixMethod() {
      @Override
      protected String lookupPrefix(String namespace) {
        if ("urn:testLookupNamespace".equals(namespace)) {
          return "tln";
        }
        else {
          return null;
        }
      }
    };

    assertEquals("tln", prefixMethod.exec(Arrays.asList("urn:testLookupNamespace")));
    try {
      prefixMethod.exec(Arrays.asList("unknown"));
      fail("Should have thrown a template model exception for an unknown namespace.");
    }
    catch (TemplateModelException e) {

    }
  }
}
