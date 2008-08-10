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

import junit.framework.TestCase;
import org.springframework.util.AntPathMatcher;

/**
 * @author Ryan Heaton
 */
public class TestAntPathMatcher extends TestCase {

  /**
   * test matching the path
   */
  public void testMatchPath() throws Exception {
    AntPathMatcher matcher = new AntPathMatcher();
    matcher.isPattern("/hello*");
    assertTrue(matcher.match("/hello/**", "/hello/my/friend"));
    assertTrue(matcher.match("/hello/**", "/hello"));
    assertFalse(matcher.match("/hello*", "/hello/my/friend"));
    assertFalse(matcher.match("/hello**", "/hello/my/friend"));
  }

}
