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

/**
 * Handler to be used to define logic to perform for tags in JavaDoc comments.
 * 
 * @author Ryan Heaton
 */
public interface JavaDocTagHandler {

  /**
   * What to do with an inline JavaDoc tag.
   *
   * @param tagName The tag name.
   * @param tagText The tag text.
   * @return The text to replace the entire tag, or null for no replacement. If the replacement
   * implements {@link JavaDocTagHandler.TextToBeHandled}, it will also
   * be handled (recursively).
   */
  Object onInlineTag(String tagName, String tagText);

  /**
   * Marker interface for text that is also to be handled.
   */
  public static interface TextToBeHandled {}
}
