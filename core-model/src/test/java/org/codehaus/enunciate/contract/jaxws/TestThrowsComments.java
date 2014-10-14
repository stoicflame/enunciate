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

package org.codehaus.enunciate.contract.jaxws;

import junit.framework.TestCase;
import net.sf.jelly.apt.decorations.JavaDoc;

/**
 * @author Ryan Heaton
 */
public class TestThrowsComments extends TestCase {

  public void testThrowsOnNewLine() throws Exception {
    JavaDoc jd = new JavaDoc("Reads a set of persons from the database.  Intended as an example of\n" +
                  "collections as SOAP parameters.\n" +
                  "@param personIds The ids of the persons to read.\n" +
                  "@return The persons that were read.\n" +
                  "@throws ServiceException\n" +
                  "If the read of one or more of the people failed.");
    String throwsDoc = jd.get("throws").toString();
    assertEquals("ServiceException\n" +
      "If the read of one or more of the people failed.", throwsDoc);

    int spaceIndex = throwsDoc.indexOf(' ');
    if (spaceIndex == -1) {
      spaceIndex = throwsDoc.length();
    }

    String exception = throwsDoc.substring(0, spaceIndex);
    String throwsComment = "";
    if ((spaceIndex + 1) < throwsDoc.length()) {
      throwsComment = throwsDoc.substring(spaceIndex + 1);
    }

    //todo: see https://jira.codehaus.org/browse/ENUNCIATE-751
    //assertEquals("If the read of one or more of the people failed.", throwsComment);

  }

}
