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

package org.codehaus.enunciate.modules.docs;

import net.sf.jelly.apt.util.JavaDocTagHandler;

import java.util.Collections;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class DocumentationJavaDocTagHandler implements JavaDocTagHandler {

  private Set<String> strippedTags = Collections.emptySet();

  public Object onInlineTag(String tagName, String tagText) {
    if ("link".equals(tagName)) {
      //todo: if it's a link, return an anchor tag?
      int valueStartStart = tagText.indexOf('#'); //the start index of where we need to start looking for the value.
      if (valueStartStart >= 0) {
        //if there's a '#' char, we have to check for a left-right paren pair before checking for the space.
        valueStartStart = tagText.indexOf('(', valueStartStart);
        if (valueStartStart >= 0) {
          valueStartStart = tagText.indexOf(')', valueStartStart);
        }
      }

      int valueStart = tagText.indexOf(' ', valueStartStart < 0 ? 0 : valueStartStart);
      if (valueStart >= 0 && valueStart + 1 < tagText.length()) {
        tagText = tagText.substring(valueStart + 1, tagText.length());
      }
    }

    return tagText;
  }

  public Object onMarkupTag(String tagName, final String tagBody) {
    if ("code".equalsIgnoreCase(tagName) && tagBody.indexOf('\n') < 0 && tagBody.indexOf('\r') < 0) {
      //if the code is not on more than one line, we assume it's meant to be <tt>
      return "<tt>" + tagBody + "</tt>";
    }
    else if (getStrippedTags().contains(tagName.toLowerCase())) {
      return tagBody;
    }
    else {
      return null;
    }
  }

  /**
   * The markup tags to strip.
   *
   * @return The markup tags to strip.
   */
  public Set<String> getStrippedTags() {
    return strippedTags;
  }

  /**
   * The markup tags to strip.
   *
   * @param strippedTags The markup tags to strip.
   */
  public void setStrippedTags(Set<String> strippedTags) {
    this.strippedTags = strippedTags;
  }
}
