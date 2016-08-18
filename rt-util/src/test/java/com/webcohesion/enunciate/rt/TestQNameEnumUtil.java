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
package com.webcohesion.enunciate.rt;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;

/**
 * @author Ryan Heaton
 */
public class TestQNameEnumUtil extends TestCase {

  /**
   * tests to/from qname enum.
   */
  public void testToFrom() throws Exception {
    assertEquals(new QName("urn:special", "appropriate"), QNameEnumUtil.toQName(SpecialQNameEnum.appropriate));
    assertEquals(new QName("urn:special", "best"), QNameEnumUtil.toQName(SpecialQNameEnum.best));
    assertEquals(new QName("urn:definite", "unique"), QNameEnumUtil.toQName(SpecialQNameEnum.certain));
    assertEquals(new QName("urn:definite", "chief"), QNameEnumUtil.toQName(SpecialQNameEnum.chief));

    assertEquals(new QName("urn:enunciate", "unusual"), QNameEnumUtil.toQName(AnotherSpecialQNameEnum.unusual));
    assertEquals(new QName("urn:enunciate", "uncommon"), QNameEnumUtil.toQName(AnotherSpecialQNameEnum.uncommon));
    assertEquals(new QName("urn:enunciate", "specific"), QNameEnumUtil.toQName(AnotherSpecialQNameEnum.specific));
    try {
      QNameEnumUtil.toQName(AnotherSpecialQNameEnum.not_a_qname_enum);
    }
    catch (IllegalArgumentException e) {}
    try {
      QNameEnumUtil.toQName(AnotherSpecialQNameEnum.other);
    }
    catch (IllegalArgumentException e) {}
    try {
      QNameEnumUtil.toQName(RetentionPolicy.CLASS);
    }
    catch (IllegalArgumentException e) {}

    assertEquals(SpecialQNameEnum.appropriate, QNameEnumUtil.fromQName(new QName("urn:special", "appropriate"), SpecialQNameEnum.class));
    assertEquals(SpecialQNameEnum.best, QNameEnumUtil.fromQName(new QName("urn:special", "best"), SpecialQNameEnum.class));
    assertEquals(SpecialQNameEnum.certain, QNameEnumUtil.fromQName(new QName("urn:definite", "unique"), SpecialQNameEnum.class));
    assertEquals(SpecialQNameEnum.chief, QNameEnumUtil.fromQName(new QName("urn:definite", "chief"), SpecialQNameEnum.class));
    assertNull(QNameEnumUtil.fromQName(new QName("urn:something", "chief"), SpecialQNameEnum.class));
    assertNull(QNameEnumUtil.fromQName(new QName("urn:definite", "howdy"), SpecialQNameEnum.class));
    try {
      QNameEnumUtil.fromQName(new QName("urn:definite", "howdy"), RetentionPolicy.class);
      fail();
    }
    catch (IllegalArgumentException e) {}

    assertEquals(AnotherSpecialQNameEnum.unusual, QNameEnumUtil.fromQName(new QName("urn:enunciate", "unusual"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.uncommon, QNameEnumUtil.fromQName(new QName("urn:enunciate", "uncommon"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.specific, QNameEnumUtil.fromQName(new QName("urn:enunciate", "specific"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.other, QNameEnumUtil.fromQName(new QName("urn:something", "specific"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.other, QNameEnumUtil.fromQName(new QName("urn:enunciate", "something"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.other, QNameEnumUtil.fromQName(new QName("urn:enunciate", "not_a_qname_enum"), AnotherSpecialQNameEnum.class));
  }

  /**
   * tests to/from qname enum.
   */
  public void testToFromUri() throws Exception {
    assertEquals("urn:special#appropriate", QNameEnumUtil.toURI(SpecialURIEnum.appropriate));
    assertEquals("urn:special#best", QNameEnumUtil.toURI(SpecialURIEnum.best));
    assertEquals("urn:definite#unique", QNameEnumUtil.toURI(SpecialURIEnum.certain));
    assertEquals("urn:definite#chief", QNameEnumUtil.toURI(SpecialURIEnum.chief));
    QNameEnumUtil.setDefaultBaseUri("urn:definite#");
    assertEquals("urn:definite#chief", QNameEnumUtil.toURI(SpecialURIEnum.chief));
    QNameEnumUtil.setWriteRelativeUris(true);
    assertEquals("chief", QNameEnumUtil.toURI(SpecialURIEnum.chief));
    QNameEnumUtil.setDefaultBaseUri(null);

    assertEquals(SpecialURIEnum.appropriate, QNameEnumUtil.fromURI("urn:special#appropriate", SpecialURIEnum.class));
    assertEquals(SpecialURIEnum.best, QNameEnumUtil.fromURI("urn:special#best", SpecialURIEnum.class));
    assertEquals(SpecialURIEnum.certain, QNameEnumUtil.fromURI("urn:definite#unique", SpecialURIEnum.class));
    assertEquals(SpecialURIEnum.chief, QNameEnumUtil.fromURI("urn:definite#chief", SpecialURIEnum.class));
    assertNull(QNameEnumUtil.fromURI("urn:something#chief", SpecialURIEnum.class));
    assertNull(QNameEnumUtil.fromURI("urn:definite#howdy", SpecialURIEnum.class));
    QNameEnumUtil.setDefaultBaseUri("urn:special#");
    assertEquals(SpecialURIEnum.chief, QNameEnumUtil.fromURI("urn:definite#chief", SpecialURIEnum.class));
    QNameEnumUtil.setDefaultBaseUri("http://domain.com/definite/sure/");
    assertEquals(SpecialURIEnum.cool, QNameEnumUtil.fromURI("cool", SpecialURIEnum.class));
    QNameEnumUtil.setDefaultBaseUri(null);
    try {
      QNameEnumUtil.fromURI("urn:definite#howdy", RetentionPolicy.class);
      fail();
    }
    catch (IllegalArgumentException e) {}
  }

}
