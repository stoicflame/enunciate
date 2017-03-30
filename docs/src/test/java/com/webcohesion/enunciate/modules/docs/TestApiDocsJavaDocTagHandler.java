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
    Matcher rawLink = ApiDocsJavaDocTagHandler.RAW_LINK_PATTERN.matcher("You can see http://blah.com for more info");
    assertTrue(rawLink.find());
    assertEquals("http://blah.com", rawLink.group(1));
    assertEquals("You can see <a href=\"http://blah.com\">http://blah.com</a> for more info", rawLink.replaceAll(" <a href=\"$1\">$1</a> "));
  }

}