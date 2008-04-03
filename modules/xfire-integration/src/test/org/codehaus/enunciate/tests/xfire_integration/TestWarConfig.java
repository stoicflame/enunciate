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

package org.codehaus.enunciate.tests.xfire_integration;

import junit.framework.TestCase;

import java.util.jar.Manifest;

/**
 * @author Ryan Heaton
 */
public class TestWarConfig extends TestCase {

  /**
   * Tests the manifest entry.
   */
  public void testManifestEntry() throws Exception {
    Manifest mf = new Manifest(getClass().getResourceAsStream("/war-manifest.mf"));
    assertEquals("whatever.jar", mf.getMainAttributes().getValue("Class-Path"));
  }

}
