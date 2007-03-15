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
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeCreator;
import org.codehaus.enunciate.modules.xfire_client.jaxws.DummyMethod;
import org.codehaus.enunciate.modules.xfire_client.jaxws.DummyMethodXFireType;

/**
 * @author Ryan Heaton
 */
public class TestIntrospectingTypeCreator extends TestCase {

  /**
   * tests creating a type.
   */
  public void testCreateType() throws Exception {
    DefaultTypeMappingRegistry registry = new DefaultTypeMappingRegistry(true);
    TypeCreator typeCreator = registry.getTypeCreator();
    typeCreator.setTypeMapping(registry.getDefaultTypeMapping());
    IntrospectingTypeCreator introspectingCreator = new IntrospectingTypeCreator(typeCreator);
    assertTrue(introspectingCreator.createType(GeneratedWrapperBeanExample.class) instanceof GeneratedWrapperBeanType);
    assertTrue(introspectingCreator.createType(DummyMethod.class) instanceof DummyMethodXFireType);
    assertNotNull(introspectingCreator.createType(String.class));
  }
}
