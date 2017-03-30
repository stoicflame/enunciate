package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import java.io.*;

/**
 * @author Ryan Heaton
 */
public class ExampleUtils {

  private ExampleUtils() {}

  public static Example loadCustomExample(Syntax syntax, String tagName, DecoratedElement element, EnunciateContext context) {
    Example example = null;
    JavaDoc.JavaDocTagList tagList = element.getJavaDoc().get(tagName);
    if (tagList != null) {
      for (String value : tagList) {
        int firstSpace = JavaDoc.indexOfFirstWhitespace(value);
        String mediaType = value.substring(0, firstSpace);
        if (syntax.isAssignableToMediaType(mediaType) && (firstSpace + 1) < value.length()) {
          String specifiedExample = value.substring(firstSpace + 1).trim();

          Reader reader;
          try {
            if (specifiedExample.startsWith("classpath:")) {
              reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(specifiedExample.substring(10)), "UTF-8");
            }
            else if (specifiedExample.startsWith("file:")) {
              reader = new FileReader(context.getConfiguration().resolveFile(specifiedExample.substring(5)));
            }
            else {
              reader = new StringReader(specifiedExample);
            }
          }
          catch (IOException e) {
            throw new EnunciateException(e);
          }

          try {
            example = syntax.parseExample(reader);
          }
          catch (Exception e) {
            context.getLogger().warn("Unable to parse @%s tag at %s: %s", tagName, example, e.getMessage());
          }
        }
      }
    }
    return example;
  }
}
