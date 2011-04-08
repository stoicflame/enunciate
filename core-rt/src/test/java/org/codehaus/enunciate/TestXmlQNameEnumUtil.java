package org.codehaus.enunciate;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Ryan Heaton
 */
public class TestXmlQNameEnumUtil extends TestCase {

  /**
   * tests to/from qname enum.
   */
  public void testToFrom() throws Exception {
    assertEquals(new QName("urn:special", "appropriate"), XmlQNameEnumUtil.toQName(SpecialQNameEnum.appropriate));
    assertEquals(new QName("urn:special", "best"), XmlQNameEnumUtil.toQName(SpecialQNameEnum.best));
    assertEquals(new QName("urn:definite", "unique"), XmlQNameEnumUtil.toQName(SpecialQNameEnum.certain));
    assertEquals(new QName("urn:definite", "chief"), XmlQNameEnumUtil.toQName(SpecialQNameEnum.chief));

    assertEquals(new QName("urn:enunciate", "unusual"), XmlQNameEnumUtil.toQName(AnotherSpecialQNameEnum.unusual));
    assertEquals(new QName("urn:enunciate", "uncommon"), XmlQNameEnumUtil.toQName(AnotherSpecialQNameEnum.uncommon));
    assertEquals(new QName("urn:enunciate", "specific"), XmlQNameEnumUtil.toQName(AnotherSpecialQNameEnum.specific));
    try {
      XmlQNameEnumUtil.toQName(AnotherSpecialQNameEnum.not_a_qname_enum);
    }
    catch (IllegalArgumentException e) {}
    try {
      XmlQNameEnumUtil.toQName(AnotherSpecialQNameEnum.other);
    }
    catch (IllegalArgumentException e) {}
    try {
      XmlQNameEnumUtil.toQName(RetentionPolicy.CLASS);
    }
    catch (IllegalArgumentException e) {}

    assertEquals(SpecialQNameEnum.appropriate, XmlQNameEnumUtil.fromQName(new QName("urn:special", "appropriate"), SpecialQNameEnum.class));
    assertEquals(SpecialQNameEnum.best, XmlQNameEnumUtil.fromQName(new QName("urn:special", "best"), SpecialQNameEnum.class));
    assertEquals(SpecialQNameEnum.certain, XmlQNameEnumUtil.fromQName(new QName("urn:definite", "unique"), SpecialQNameEnum.class));
    assertEquals(SpecialQNameEnum.chief, XmlQNameEnumUtil.fromQName(new QName("urn:definite", "chief"), SpecialQNameEnum.class));
    assertNull(XmlQNameEnumUtil.fromQName(new QName("urn:something", "chief"), SpecialQNameEnum.class));
    assertNull(XmlQNameEnumUtil.fromQName(new QName("urn:definite", "howdy"), SpecialQNameEnum.class));
    try {
      XmlQNameEnumUtil.fromQName(new QName("urn:definite", "howdy"), RetentionPolicy.class);
      fail();
    }
    catch (IllegalArgumentException e) {}

    assertEquals(AnotherSpecialQNameEnum.unusual, XmlQNameEnumUtil.fromQName(new QName("urn:enunciate", "unusual"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.uncommon, XmlQNameEnumUtil.fromQName(new QName("urn:enunciate", "uncommon"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.specific, XmlQNameEnumUtil.fromQName(new QName("urn:enunciate", "specific"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.other, XmlQNameEnumUtil.fromQName(new QName("urn:something", "specific"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.other, XmlQNameEnumUtil.fromQName(new QName("urn:enunciate", "something"), AnotherSpecialQNameEnum.class));
    assertEquals(AnotherSpecialQNameEnum.other, XmlQNameEnumUtil.fromQName(new QName("urn:enunciate", "not_a_qname_enum"), AnotherSpecialQNameEnum.class));
  }

}
