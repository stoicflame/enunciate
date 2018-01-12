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

/**
 * @author Ryan Heaton
 */
public class DefaultJavaDocTagHandler implements JavaDocTagHandler {

  public static final DefaultJavaDocTagHandler INSTANCE = new DefaultJavaDocTagHandler();

  @Override
  public String getTypeId() {
    return "default";
  }

  public String onInlineTag(String tagName, String tagText, DecoratedElement context) {
    if ("link".equals(tagName)) {
      final JavaDocLink link = JavaDocLink.parse(tagText);
      if (link.hasLabel()) {
        tagText = link.getLabel();
      }
    }

    return tagText;
  }

  @Override
  public String onBlockTag(String tagName, String value, DecoratedElement context) {
    return value;
  }
}
