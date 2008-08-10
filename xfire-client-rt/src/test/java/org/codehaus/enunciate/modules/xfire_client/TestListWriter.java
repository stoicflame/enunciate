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

package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.MessageContext;

import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestListWriter extends TestCase {

  /**
   * tests writing a list.
   */
  public void testWriteList() throws Exception {
    TypeMapping typeMapping = new DefaultTypeMappingRegistry(true).getDefaultTypeMapping();
    ListWriter writer = new ListWriter(new long[]{1234567890, 987654321, 6789054321L}, typeMapping, new MessageContext());
    assertEquals("1234567890 987654321 6789054321", writer.getValue());

    writer = new ListWriter(Arrays.asList("twinkle,", "twinkle", "little", "star"), typeMapping, new MessageContext());
    assertEquals("twinkle, twinkle little star", writer.getValue());
  }
}
