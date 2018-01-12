package com.webcohesion.enunciate.javac.javadoc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavaDocLinkTest {
  @Test
  public void empty() {
    final JavaDocLink link = JavaDocLink.parse("");
    assertEquals("", link.getClassName());
    assertEquals("", link.getMemberName());
    assertEquals(null, link.getLabel());
  }

  @Test
  public void spaces() {
    final JavaDocLink link = JavaDocLink.parse(" ");
    assertEquals("", link.getClassName());
    assertEquals("", link.getMemberName());
    assertEquals(null, link.getLabel());
  }

  @Test
  public void classFieldLabel() {
    final JavaDocLink link = JavaDocLink.parse("Foo#bar foobar");
    assertEquals("Foo", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals("foobar", link.getLabel());
  }

  @Test
  public void classFieldLabelSpaces() {
    final JavaDocLink link = JavaDocLink.parse(" Foo#bar foobar ");
    assertEquals("Foo", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals("foobar", link.getLabel());
  }

  @Test
  public void classMethodLabelSpaces() {
    final JavaDocLink link = JavaDocLink.parse(" Foo#bar(int, int) foobar ");
    assertEquals("Foo", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals("foobar", link.getLabel());
  }

  @Test
  public void classField() {
    final JavaDocLink link = JavaDocLink.parse("Foo#bar");
    assertEquals("Foo", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals(null, link.getLabel());
  }

  @Test
  public void classMethod() {
    final JavaDocLink link = JavaDocLink.parse("Foo#bar(int, int)");
    assertEquals("Foo", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals(null, link.getLabel());
  }

  @Test
  public void classFieldSpaces() {
    final JavaDocLink link = JavaDocLink.parse(" Foo#bar ");
    assertEquals("Foo", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals(null, link.getLabel());
  }

  @Test
  public void classMethodSpaces() {
    final JavaDocLink link = JavaDocLink.parse(" Foo#bar(int, int) ");
    assertEquals("Foo", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals(null, link.getLabel());
  }

  @Test
  public void field() {
    final JavaDocLink link = JavaDocLink.parse("#bar");
    assertEquals("", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals(null, link.getLabel());
  }

  @Test
  public void method() {
    final JavaDocLink link = JavaDocLink.parse("#bar(int, int)");
    assertEquals("", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals(null, link.getLabel());
  }


  @Test
  public void fieldLabel() {
    final JavaDocLink link = JavaDocLink.parse("#bar foobar");
    assertEquals("", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals("foobar", link.getLabel());
  }

  @Test
  public void methodLabel() {
    final JavaDocLink link = JavaDocLink.parse("#bar(int, int) foobar");
    assertEquals("", link.getClassName());
    assertEquals("bar", link.getMemberName());
    assertEquals("foobar", link.getLabel());
  }


  @Test
  public void classLabel() {
    final JavaDocLink link = JavaDocLink.parse("Foo foobar");
    assertEquals("Foo", link.getClassName());
    assertEquals("", link.getMemberName());
    assertEquals("foobar", link.getLabel());
  }

  @Test
  public void classLabelSpaces() {
    final JavaDocLink link = JavaDocLink.parse(" Foo foobar ");
    assertEquals("Foo", link.getClassName());
    assertEquals("", link.getMemberName());
    assertEquals("foobar", link.getLabel());
  }

  @Test
  public void classOnly() {
    final JavaDocLink link = JavaDocLink.parse("Foo");
    assertEquals("Foo", link.getClassName());
    assertEquals("", link.getMemberName());
    assertEquals(null, link.getLabel());
  }
}
