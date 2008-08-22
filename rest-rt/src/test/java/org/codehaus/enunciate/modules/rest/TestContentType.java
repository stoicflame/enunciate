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

import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestContentType extends TestCase {

  /**
   * tests parse
   */
  public void testContentType() throws Exception {
    ContentType contentType = ContentType.parse("*/*;q=0.8");
    assertEquals("*", contentType.getType());
    assertEquals("*", contentType.getSubtype());
    assertEquals(0.8f, contentType.getQuality());

    contentType = ContentType.parse("text/xml");
    assertEquals("text", contentType.getType());
    assertEquals("xml", contentType.getSubtype());
    assertEquals(1f, contentType.getQuality());

    contentType = ContentType.parse("application/atom+xml");
    assertEquals("application", contentType.getType());
    assertEquals("atom+xml", contentType.getSubtype());
    assertEquals(1f, contentType.getQuality());

    assertTrue(ContentType.parse("*/*").isAcceptable(contentType));
    assertTrue(ContentType.parse("application/*").isAcceptable(contentType));
    assertTrue(ContentType.parse("*/atom+xml").isAcceptable(contentType));

    ContentType[] sorted = new ContentType[6];
    sorted[0] = ContentType.parse("*/*;q=0.2");
    sorted[1] = ContentType.parse("application/xml");
    sorted[2] = ContentType.parse("application/atom+xml;q=0.9");
    sorted[3] = ContentType.parse("image/png");
    sorted[4] = ContentType.parse("image/jpg");
    sorted[5] = ContentType.parse("image/gif;q=0.8");

    Arrays.sort(sorted);

    assertEquals("application", sorted[0].getType());
    assertEquals("xml", sorted[0].getSubtype());
    assertEquals(1f, sorted[0].getQuality());
    assertEquals("image", sorted[1].getType());
    assertEquals("jpg", sorted[1].getSubtype());
    assertEquals(1f, sorted[1].getQuality());
    assertEquals("image", sorted[2].getType());
    assertEquals("png", sorted[2].getSubtype());  
    assertEquals(1f, sorted[2].getQuality());
    assertEquals("application", sorted[3].getType());
    assertEquals("atom+xml", sorted[3].getSubtype());
    assertEquals(0.9f, sorted[3].getQuality());
    assertEquals("image", sorted[4].getType());
    assertEquals("gif", sorted[4].getSubtype());
    assertEquals(0.8f, sorted[4].getQuality());
    assertEquals("*", sorted[5].getType());
    assertEquals("*", sorted[5].getSubtype());
    assertEquals(0.2f, sorted[5].getQuality());

  }

}
