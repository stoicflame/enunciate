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

package org.codehaus.enunciate.config;

import junit.framework.TestCase;
import org.codehaus.enunciate.modules.DeploymentModule;

import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
public class TestEnunciateConfiguration extends TestCase {

  /**
   * Tests loading the configuration from a stream.
   */
  public void testLoad() throws Exception {
    EnunciateConfiguration config = new EnunciateConfiguration(new ArrayList<DeploymentModule>());
    config.load(TestEnunciateConfiguration.class.getResourceAsStream("basic.config.xml"));
    assertTrue(config.getValidator() instanceof MockValidator);
    assertEquals(3, config.getNamespacesToPrefixes().size());
    assertEquals("pre1", config.getNamespacesToPrefixes().get("urn:org.codehaus.enunciate.config.TestEnunciateConfiguration.pre1"));
    assertEquals("pre2", config.getNamespacesToPrefixes().get("urn:org.codehaus.enunciate.config.TestEnunciateConfiguration.pre2"));
    assertEquals("pre3", config.getNamespacesToPrefixes().get("urn:org.codehaus.enunciate.config.TestEnunciateConfiguration.pre3"));

    //load a file that doesn't validate against the schema.
    config = new EnunciateConfiguration(new ArrayList<DeploymentModule>());
    //as of 1.8, doesn't throw an exception.
    config.load(getClass().getResourceAsStream("invalid.config.xml"));

    //validate the "module" element processing...
    ArrayList<DeploymentModule> list = new ArrayList<DeploymentModule>();
    DeploymentModuleOne module1 = new DeploymentModuleOne();
    DeploymentModuleTwo module2 = new DeploymentModuleTwo();
    list.add(module1);
    list.add(module2);
    config = new EnunciateConfiguration(list);
    config.load(getClass().getResourceAsStream("module.config.xml"));
    assertEquals("attribute1", module1.getAttribute());
    assertEquals(3, module1.elementMap.size());
    assertEquals("value1", module1.elementMap.get("element1"));
    assertEquals("value2", module1.elementMap.get("element2"));
    assertEquals("value3", module1.elementMap.get("element3"));
    assertEquals("attribute2", module2.getAttribute());
    assertEquals(3, module2.elementMap.size());
    assertEquals("value4", module2.elementMap.get("element4"));
    assertEquals("value5", module2.elementMap.get("element5"));
    assertEquals("value6", module2.elementMap.get("element6"));

  }

}
