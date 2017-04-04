package com.webcohesion.enunciate.modules.docs;

import junit.framework.TestCase;

import java.util.regex.Matcher;

@SuppressWarnings ( "unchecked" )
public class TestApiDocsJavaDocTagHandler extends TestCase {

  public void testRawLinkParsing() throws Exception {
    assertFalse(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("<a href=\"http://blah.com\">blah</a>").find());
    assertFalse(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("<a href='http://blah.com'>blah</a>").find());
    assertFalse(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("<a href=http://blah.com>blah</a>").find());
    assertFalse(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("<a href=\"http://blah.com\">http://blah.com</a>").find());
    assertFalse(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("<a href=\"http://blah.com\">the thing for http://blah.com</a>").find());
    assertFalse(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("<img src=\"http://blah.com/img.png\"/>").find());
    assertFalse(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("Some inline image <img src=\"http://blah.com/img.png\"/> in the middle of text").find());
    assertTrue(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("http://blah.com").find());
    assertTrue(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("Something to see on http://blah.com").find());
    assertTrue(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("http://blah.com has more info").find());
    assertTrue(ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("Just in case, http://blah.com has more info").find());
    Matcher rawLink = ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("You can see http://blah.com for more info");
    assertTrue(rawLink.find());
    assertEquals("http://blah.com", rawLink.group(1));
    assertEquals("You can see <a href=\"http://blah.com\">http://blah.com</a> for more info", rawLink.replaceAll(" <a href=\"$1\">$1</a>"));
    assertEquals("You can see <a target=\"_blank\" href=\"http://blah.com\">http://blah.com</a> for more info", rawLink.replaceAll(" <a target=\"_blank\" href=\"$1\">$1</a>"));
  }

}