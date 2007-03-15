/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;

import java.util.Arrays;

import org.codehaus.enunciate.modules.xfire_client.jaxws.DummyMethod;
import org.codehaus.enunciate.modules.xfire_client.jaxws.DummyMethodXFireType;
import org.codehaus.xfire.aegis.type.TypeMapping;

/**
 * @author Ryan Heaton
 */
public class TestIntrospectingTypeRegistry extends TestCase {

  /**
   * Tests the initialization of the registry.
   */
  public void testInitialization() throws Exception {
    IntrospectingTypeRegistry registry = new IntrospectingTypeRegistry(Arrays.asList(DummyMethod.class));
    String[] uris = registry.getRegisteredEncodingStyleURIs();
    for (String uri : uris) {
      TypeMapping typeMapping = registry.getTypeMapping(uri);
      assertTrue(typeMapping.isRegistered(DummyMethod.class));
      assertTrue(typeMapping.getType(DummyMethod.class) instanceof DummyMethodXFireType);
    }
  }
}
