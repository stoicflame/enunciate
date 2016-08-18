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
public class JavaDocTagHandlerFactory {

  private static JavaDocTagHandler INSTANCE;
  private static boolean CHECKED;

  /**
   * The tag handler instance.
   *
   * @return The tag handler instance.
   */
  public static synchronized JavaDocTagHandler getTagHandler() {
    if (!CHECKED) {
      String handlerClassname = System.getProperty(JavaDocTagHandler.class.getName());
      if (handlerClassname != null) {
        try {
          INSTANCE = (JavaDocTagHandler) Class.forName(handlerClassname).newInstance();
          CHECKED = true;
        }
        catch (Exception e) {
          System.getProperties().remove(JavaDocTagHandler.class.getName());
          e.printStackTrace(System.err);
        }
      }
      else {
        INSTANCE = new DefaultJavaDocTagHandler();
        CHECKED = true;
      }
    }

    return INSTANCE;
  }

  /**
   * The tag handler instance.
   *
   * @param tagHandler The tag handler instance.
   */
  public static synchronized void setTagHandler(JavaDocTagHandler tagHandler) {
    INSTANCE = tagHandler;
    CHECKED = true;
  }

}