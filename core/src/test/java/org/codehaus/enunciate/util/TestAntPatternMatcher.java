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

package org.codehaus.enunciate.util;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestAntPatternMatcher extends TestCase {

  /**
   * tests the match
   */
  public void testMatch() throws Exception {
    AntPatternMatcher matcher = new AntPatternMatcher();
    assertFalse(matcher.match("org.codehaus.enunciate.*", "org.codehaus.enunciate.subpackage.SomeClass"));
    assertTrue(matcher.match("org.codehaus.enunciate.**", "org.codehaus.enunciate.subpackage.SomeClass"));
  }

  /**
   * tests the match with backslash
   */
  public void testMatch2() throws Exception {
    AntPatternMatcher matcher = new AntPatternMatcher();
    matcher.setPathSeparator("\\");
    assertFalse(matcher.match("org\\codehaus\\enunciate\\*", "org\\codehaus\\enunciate\\subpackage\\SomeClass"));
    assertTrue(matcher.match("org\\codehaus\\enunciate\\**", "org\\codehaus\\enunciate\\subpackage\\SomeClass"));
  }

}
