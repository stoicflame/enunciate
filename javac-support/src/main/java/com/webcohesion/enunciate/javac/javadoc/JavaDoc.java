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

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;

public class JavaDoc extends HashMap<String, JavaDoc.JavaDocTagList> {

  public static final JavaDoc EMPTY = new JavaDoc("");

  private static final Pattern INLINE_TAG_PATTERN = Pattern.compile("\\{@([^\\} ]+) ?(.*?)\\}");
  private static final Pattern INHERITDOC_PATTERN = Pattern.compile("\\{@inheritDoc(.*?)\\}");
  private static final Pattern RAW_LINK_PATTERN = Pattern.compile("(?:^|[^>=\"'])(http.[^\"'<\\s]+)(?![^<>]*>|[^\"]*?<\\/a)");
  private static final char[] WHITESPACE_CHARS = new char[]{' ', '\t', '\n', 0x0B, '\f', '\r'};

  protected String value;

  private JavaDoc(String value) {
    this.value = value;
  }

  public JavaDoc(String docComment, JavaDocTagHandler tagHandler, DecoratedElement context, DecoratedProcessingEnvironment env) {
    init(docComment, tagHandler, context, env);
  }

  protected void init(String docComment, JavaDocTagHandler tagHandler, DecoratedElement context, DecoratedProcessingEnvironment env) {
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

    assumeInheritedComments(context, env, tagHandler);

    if (tagHandler != null) {
      this.value = resolveJavaDocSemantics(null, this.value, tagHandler, context);
      for (Map.Entry<String, JavaDocTagList> entry : entrySet()) {
        JavaDocTagList tagValues = entry.getValue();
        for (int i = 0; i < tagValues.size(); i++) {
          String value = tagValues.get(i);
          tagValues.set(i, resolveJavaDocSemantics(entry.getKey(), value, tagHandler, context));
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

  public static int indexOfWhitespaceFrom(String line, int from) {
    int result = line.length();
    for (char ws : WHITESPACE_CHARS) {
      int spaceIndex = line.indexOf(ws, from);
      spaceIndex = spaceIndex == -1 ? result : spaceIndex;
      result = min(spaceIndex, result);
    }
    return result;
  }

  /**
   * Handles all the semantic tokens of the JavaDoc.
   *
   * @param section The section name (null for main description).
   * @param value The value.
   * @param handler The handler.
   * @param context The context of the tags.
   * @return The replacement value.
   */
  private String resolveJavaDocSemantics(String section, String value, JavaDocTagHandler handler, DecoratedElement context) {
    //first pass through the inline tags...
    StringBuilder builder = new StringBuilder();

    Matcher matcher = INLINE_TAG_PATTERN.matcher(value);
    int lastStart = 0;
    while (matcher.find()) {
      builder.append(value.substring(lastStart, matcher.start()));
      String replacement = handler.onInlineTag(matcher.group(1), matcher.group(2), context);
      if (replacement != null) {
        builder.append(replacement);
      }
      else {
        builder.append(value.substring(matcher.start(), matcher.end()));
      }
      lastStart = matcher.end();
    }
    builder.append(value.substring(lastStart, value.length()));

	String result = handler.onBlockTag(section, builder.toString(), context);
	// replace all remaining raw links
	return RAW_LINK_PATTERN.matcher(result).replaceAll(" <a target=\"_blank\" href=\"$1\">$1</a>");
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

  private void assumeInheritedComments(DecoratedElement context, DecoratedProcessingEnvironment env, JavaDocTagHandler tagHandler) {
    //algorithm defined per http://docs.oracle.com/javase/6/docs/technotes/tools/solaris/javadoc.html#inheritingcomments
    if (context instanceof TypeElement) {
      assumeInheritedTypeComments((TypeElement) context, tagHandler);
    }
    else if (context instanceof ExecutableElement) {
      assumeInheritedExecutableComments((ExecutableElement) context, env, tagHandler);
    }
  }

  private void assumeInheritedExecutableComments(ExecutableElement context, DecoratedProcessingEnvironment env, JavaDocTagHandler tagHandler) {
    if (assumeInheritedExecutableComments(context, EMPTY)) {
      //all comments have already been assumed.
      return;
    }

    Element el = context.getEnclosingElement();
    if (el instanceof TypeElement) {
      TypeElement typeElement = (TypeElement) el;
      List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
      for (TypeMirror iface : interfaces) {
        Element superType = iface instanceof DeclaredType ? ((DeclaredType) iface).asElement() : null;
        if (superType != null) {
          List<ExecutableElement> methods = ElementFilter.methodsIn(superType.getEnclosedElements());
          for (ExecutableElement candidate : methods) {
            if (env.getElementUtils().overrides(context, candidate, typeElement) && candidate instanceof DecoratedElement) {
              JavaDoc inheritedDocs = ((DecoratedElement) candidate).getJavaDoc(tagHandler);
              if (assumeInheritedExecutableComments(context, inheritedDocs)) {
                return;
              }
            }
          }
        }
      }

      TypeMirror superclass = typeElement.getSuperclass();
      if (superclass != null && superclass instanceof DeclaredType) {
        Element superType = ((DeclaredType) superclass).asElement();
        if (superType != null) {
          List<ExecutableElement> methods = ElementFilter.methodsIn(superType.getEnclosedElements());
          for (ExecutableElement candidate : methods) {
            if (env.getElementUtils().overrides(context, candidate, typeElement) && candidate instanceof DecoratedElement) {
              JavaDoc inheritedDocs = ((DecoratedElement) candidate).getJavaDoc(tagHandler);
              assumeInheritedExecutableComments(context, inheritedDocs);
              return;
            }
          }
        }
      }
    }
  }

  private boolean assumeInheritedExecutableComments(ExecutableElement context, JavaDoc inherited) {
    boolean assumed = true;

    if (valueInherits(this.value)) {
      String inheritedValue = inherited.toString();
      if (!inheritedValue.isEmpty()) {
        if (this.value.isEmpty()) {
          this.value = inheritedValue;
        }
        else {
          this.value = INHERITDOC_PATTERN.matcher(this.value).replaceAll(inheritedValue);
        }
      }
      else {
        assumed = false;
      }
    }

    if (context.getReturnType() != null && context.getReturnType().getKind() != TypeKind.VOID) {
      //need a return tag.
      JavaDocTagList returnTag = get("return");
      String returnValue = returnTag == null ? "" : returnTag.toString();
      if (valueInherits(returnValue)) {
        JavaDocTagList inheritedTag = inherited.get("return");
        String inheritedValue = inheritedTag == null ? "" : inheritedTag.toString();
        if (!inheritedValue.isEmpty()) {
          if (returnValue.isEmpty()) {
            put("return", new JavaDocTagList(inheritedValue));
          }
          else {
            returnValue = INHERITDOC_PATTERN.matcher(returnValue).replaceAll(inheritedValue);
            put("return", new JavaDocTagList(returnValue));
          }
        }
        else {
          assumed = false;
        }
      }
    }


    List<? extends VariableElement> parameterNames = context.getParameters();
    if (parameterNames != null && !parameterNames.isEmpty()) {
      JavaDocTagList paramTags = get("param");
      for (VariableElement param : parameterNames) {
        String paramName = param.getSimpleName().toString();
        String paramValue = "";
        int paramIndex = -1;
        if (paramTags != null) {
          for (int i = 0; i < paramTags.size(); i++) {
            String paramTag = paramTags.get(i);
            if (paramName.equals(paramTag.substring(0, indexOfFirstWhitespace(paramTag)))) {
              paramValue = paramTag;
              paramIndex = i;
              break;
            }
          }
        }

        if (valueInherits(paramValue)) {
          JavaDocTagList inheritedTags = inherited.get("param");
          String inheritedValue = "";
          if (inheritedTags != null) {
            for (String inheritedTag : inheritedTags) {
              if (paramName.equals(inheritedTag.substring(0, indexOfFirstWhitespace(inheritedTag)))) {
                inheritedValue = inheritedTag;
                break;
              }
            }
          }
          if (!inheritedValue.isEmpty()) {
            if (paramIndex < 0) {
              pushValue("param", inheritedValue);
            }
            else {
              paramValue = INHERITDOC_PATTERN.matcher(paramValue).replaceAll(inheritedValue);
              paramTags.set(paramIndex, paramValue);
            }
          }
          else {
            assumed = false;
          }
        }
      }
    }

    //todo: inherit 'throws'.

    return assumed;
  }

  private void assumeInheritedTypeComments(TypeElement e, JavaDocTagHandler tagHandler) {
    if (valueInherits(this.value)) {
      String inheritedValue = "";
      List<? extends TypeMirror> interfaces = e.getInterfaces();
      for (TypeMirror iface : interfaces) {
        Element el = iface instanceof DeclaredType ? ((DeclaredType)iface).asElement() : null;
        if (el instanceof DecoratedElement) {
          inheritedValue = ((DecoratedElement) el).getJavaDoc(tagHandler).toString();
          if (!inheritedValue.isEmpty()) {
            break;
          }
        }
      }

      if (inheritedValue.isEmpty()) {
        TypeMirror superclass = e.getSuperclass();
        if (superclass instanceof DeclaredType) {
          Element el = ((DeclaredType) superclass).asElement();
          if (el instanceof DecoratedElement) {
            inheritedValue = ((DecoratedElement) el).getJavaDoc().toString();
          }
        }
      }

      if (!inheritedValue.isEmpty()) {
        if (this.value.isEmpty()) {
          this.value = inheritedValue;
        }
        else {
          this.value = INHERITDOC_PATTERN.matcher(this.value).replaceAll(inheritedValue);
        }
      }
    }
  }

  private boolean valueInherits(String value) {
    return value == null || value.isEmpty() || INHERITDOC_PATTERN.matcher(value).find();
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
