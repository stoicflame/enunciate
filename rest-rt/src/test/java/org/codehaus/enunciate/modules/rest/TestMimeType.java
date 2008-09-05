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

import org.codehaus.enunciate.rest.MimeType;

/**
 * @author Ryan Heaton
 */
public class TestMimeType extends TestCase {

  /**
   * tests parse
   */
  public void testMimeType() throws Exception {
    MimeType mimeType = MimeType.parse("*/*;q=0.8");
    assertEquals("*", mimeType.getType());
    assertEquals("*", mimeType.getSubtype());
    assertEquals(0.8f, mimeType.getQuality());

    mimeType = MimeType.parse("text/xml");
    assertEquals("text", mimeType.getType());
    assertEquals("xml", mimeType.getSubtype());
    assertEquals(1f, mimeType.getQuality());

    mimeType = MimeType.parse("application/atom+xml");
    assertEquals("application", mimeType.getType());
    assertEquals("atom+xml", mimeType.getSubtype());
    assertEquals(1f, mimeType.getQuality());

    assertTrue(MimeType.parse("*/*").isAcceptable(mimeType));
    assertTrue(MimeType.parse("application/*").isAcceptable(mimeType));
    assertTrue(MimeType.parse("*/atom+xml").isAcceptable(mimeType));

    MimeType[] sorted = new MimeType[6];
    sorted[0] = MimeType.parse("*/*;q=0.2");
    sorted[1] = MimeType.parse("application/xml");
    sorted[2] = MimeType.parse("application/atom+xml;q=0.9");
    sorted[3] = MimeType.parse("image/png");
    sorted[4] = MimeType.parse("image/jpg");
    sorted[5] = MimeType.parse("image/gif;q=0.8");

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
