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
package com.webcohesion.enunciate.javac.javadoc;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;

public class JavaDoc extends HashMap<String, JavaDoc.JavaDocTagList> {

  private static final Pattern INLINE_TAG_PATTERN = Pattern.compile("\\{@([^\\} ]+) ?(.*?)\\}");
  private static final char[] WHITESPACE_CHARS = new char[]{' ', '\t', '\n', 0x0B, '\f', '\r'};

  protected String value;
  protected final DecoratedElement context;

  public JavaDoc(String docComment, JavaDocTagHandler tagHandler, DecoratedElement context) {
    this.context = context;
    init(docComment, tagHandler);
  }

  protected void init(String docComment, JavaDocTagHandler tagHandler) {
    if (docComment == null) {
      value = "";
    }
    else {
      BufferedReader reader = new BufferedReader(new StringReader(docComment));
      StringWriter currentValue = new StringWriter();
      PrintWriter out = new PrintWriter(currentValue);
      String currentTag = null;
      boolean preformatting = false;
      try {
        String line = reader.readLine();
        while (line != null) {
          if (!preformatting) {
            line = line.trim();
          }
          if (line.startsWith("@")) { //it's a javadoc block tag.

            //push and clear our current value.
            pushValue(currentTag, currentValue.toString());

            int spaceIndex = indexOfFirstWhitespace(line);

            currentTag = line.substring(1, spaceIndex);
            String value = "";
            if ((spaceIndex + 1) < line.length()) {
              value = line.substring(spaceIndex + 1);
            }

            currentValue = new StringWriter();
            out = new PrintWriter(currentValue);
            out.println(value);
          }
          else {
            out.println(line);
          }
          preformatting = (line.contains("<pre") || preformatting) && !line.contains("</pre");

          line = reader.readLine();
        }

        //push the last value.
        pushValue(currentTag, currentValue.toString());
      }
      catch (IOException e) {
        //fall through.
      }
    }

    if (doTagHandling(tagHandler)) {
      this.value = handleAllTags(null, this.value, tagHandler);
      for (Map.Entry<String, JavaDocTagList> entry : entrySet()) {
        JavaDocTagList tagValues = entry.getValue();
        for (int i = 0; i < tagValues.size(); i++) {
          String value = tagValues.get(i);
          tagValues.set(i, handleAllTags(null, value, tagHandler));
        }
      }
    }
  }

  public static int indexOfFirstWhitespace(String line) {
    int result = line.length();
    for (char ws : WHITESPACE_CHARS) {
      int spaceIndex = line.indexOf(ws);
      spaceIndex = spaceIndex == -1 ? result : spaceIndex;
      result = min(spaceIndex, result);
    }
    return result;
  }

  protected boolean doTagHandling(JavaDocTagHandler tagHandler) {
    return tagHandler != null;
  }

  /**
   * Handles all the tags with the given handler.
   *
   * @param section The section name (null for main description).
   * @param value The value.
   * @param handler The handler.
   * @return The replacement value.
   */
  protected String handleAllTags(String section, String value, JavaDocTagHandler handler) {
    //first pass through the inline tags...
    StringBuilder builder = new StringBuilder();

    Matcher matcher = INLINE_TAG_PATTERN.matcher(value);
    int lastStart = 0;
    while (matcher.find()) {
      builder.append(value.substring(lastStart, matcher.start()));
      String replacement = handler.onInlineTag(matcher.group(1), matcher.group(2), this.context);
      if (replacement != null) {
        builder.append(replacement);
      }
      else {
        builder.append(value.substring(matcher.start(), matcher.end()));
      }
      lastStart = matcher.end();
    }
    builder.append(value.substring(lastStart, value.length()));

    return handler.onBlockTag(section, builder.toString(), this.context);
  }

  /**
   * Pushes a value onto a tag.
   *
   * @param tag The tag onto which to push the value.  (null indicates no tag.)
   * @param value The value of the tag.
   */
  private void pushValue(String tag, String value) {
    value = value.trim(); //trim the value.
    
    if (tag == null) {
      this.value = value;
    }
    else {
      JavaDocTagList tagList = get(tag);
      if (tagList == null) {
        tagList = new JavaDocTagList(value);
        put(tag, tagList);
      }
      else {
        tagList.add(value);
      }
    }
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String toString() {
    return value;
  }

  /**
   * A list of values for a javadoc tag.
   */
  public static class JavaDocTagList extends ArrayList<String> {

    /**
     * To construct a tag list, at least one value must be supplied.
     *
     * @param firstValue The first value.
     */
    public JavaDocTagList(String firstValue) {
      add(firstValue);
    }

    /**
     * @return The first value in the list.
     */
    public String toString() {
      return get(0);
    }

  }

}
