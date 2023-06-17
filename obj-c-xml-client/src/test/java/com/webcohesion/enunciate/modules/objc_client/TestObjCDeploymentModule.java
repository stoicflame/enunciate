/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.objc_client;

import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestObjCDeploymentModule extends TestCase {

  /**
   * tests scrubbing a c identifie.
   */
  public void testScrubIdentifier() throws Exception {
    assertEquals("hello_me", ObjCXMLClientModule.scrubIdentifier("hello-me"));
  }

  public void testPackageIdentifier() throws Exception {
    String[] subpackages = "com.webcohesion.enunciate.samples.objc.whatever".split("\\.", 9);
    assertEquals("ENUNCIATECOMOBJC", String.format("%3$S%1$S%5$S", (Object[]) subpackages));

  }

}
