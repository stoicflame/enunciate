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

package org.codehaus.enunciate.contract.rest;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestRESTNoun extends TestCase {

  /**
   * testing the patterns.
   */
  public void testPatterns() throws Exception {
    RESTNoun noContext = new RESTNoun("beast", "");
    assertEquals("beast", noContext.getAntPattern());
    assertEquals(2, noContext.getServletPatterns().size());
    assertTrue(noContext.getServletPatterns().contains("/beast"));
    assertTrue(noContext.getServletPatterns().contains("/beast/*"));

    RESTNoun withContext = new RESTNoun("beast", "some/context/applied");
    assertEquals("some/context/applied/beast", withContext.getAntPattern());
    assertEquals(2, withContext.getServletPatterns().size());
    assertTrue(withContext.getServletPatterns().contains("/some/context/applied/beast"));
    assertTrue(withContext.getServletPatterns().contains("/some/context/applied/beast/*"));

    RESTNoun withContextParams = new RESTNoun("beast", "some/{params}/applied");
    assertEquals("some/*/applied/beast", withContextParams.getAntPattern());
    assertEquals(1, withContextParams.getServletPatterns().size());
    assertEquals("/some/*", withContextParams.getServletPatterns().get(0));
  }

}
