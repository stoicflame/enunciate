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
package com.webcohesion.enunciate.modules.spring_web.model;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings ( "unchecked" )
public class SpringControllerTest {

  @Test
  public void testExtractPathComponents() throws Exception {
    List<PathSegment> components = SpringController.extractPathComponents("/path/{id}");
    assertEquals(2, components.size());
    assertEquals("/path/", components.get(0).getValue());
    assertNull(components.get(0).getVariable());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}", components.get(1).getValue());
    assertEquals("id", components.get(1).getVariable());
    assertNull(components.get(1).getRegex());

    components = SpringController.extractPathComponents("/path/{id: [0-9]+}");
    assertEquals(2, components.size());
    assertEquals("/path/", components.get(0).getValue());
    assertNull(components.get(0).getVariable());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}", components.get(1).getValue());
    assertEquals("id", components.get(1).getVariable());
    assertEquals(" [0-9]+", components.get(1).getRegex());

    components = SpringController.extractPathComponents("/path/{id: [0-9]+}/other");
    assertEquals(2, components.size());
    assertEquals("/path/", components.get(0).getValue());
    assertNull(components.get(0).getVariable());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}/other", components.get(1).getValue());
    assertEquals("id", components.get(1).getVariable());
    assertEquals(" [0-9]+", components.get(1).getRegex());

    components = SpringController.extractPathComponents("/path/{id: [0-9]+}/other/");
    assertEquals(2, components.size());
    assertEquals("/path/", components.get(0).getValue());
    assertNull(components.get(0).getVariable());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}/other/", components.get(1).getValue());
    assertEquals("id", components.get(1).getVariable());
    assertEquals(" [0-9]+", components.get(1).getRegex());

    components = SpringController.extractPathComponents("/path/{file: [\\/A-Za-z0-9_\\-\\.]+.jpg}");
    assertEquals(2, components.size());
    assertEquals("/path/", components.get(0).getValue());
    assertNull(components.get(0).getVariable());
    assertNull(components.get(0).getRegex());
    assertEquals("{file}", components.get(1).getValue());
    assertEquals("file", components.get(1).getVariable());
    assertEquals(" [\\/A-Za-z0-9_\\-\\.]+.jpg", components.get(1).getRegex());

    components = SpringController.extractPathComponents("/spring-web/{symbolicName:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{extension:\\.[a-z]+}");
    assertEquals(4, components.size());
    assertEquals("/spring-web/", components.get(0).getValue());
    assertNull(components.get(0).getVariable());
    assertNull(components.get(0).getRegex());
    assertEquals("{symbolicName}-", components.get(1).getValue());
    assertEquals("symbolicName", components.get(1).getVariable());
    assertEquals("[a-z-]+", components.get(1).getRegex());
    assertEquals("{version}", components.get(2).getValue());
    assertEquals("version", components.get(2).getVariable());
    assertEquals("\\d\\.\\d\\.\\d", components.get(2).getRegex());
    assertEquals("{extension}", components.get(3).getValue());
    assertEquals("extension", components.get(3).getVariable());
    assertEquals("\\.[a-z]+", components.get(3).getRegex());
  }

}