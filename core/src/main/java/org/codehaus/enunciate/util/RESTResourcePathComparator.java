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

package org.codehaus.enunciate.util;

import java.util.Comparator;

/**
 * Sorts the path of {@link org.codehaus.enunciate.contract.common.rest.RESTResource}s by path, then alphabetically.
 *
 * @author Ryan Heaton
 */
public class RESTResourcePathComparator implements Comparator<String> {

  public int compare(String resource1Path, String resource2Path) {
    String[] path1Segments = resource1Path.split("/");
    String[] path2Segments = resource2Path.split("/");
    int comparison = path1Segments.length - path2Segments.length;
    if (comparison == 0) {
      int index = 0;
      while (path1Segments.length > index && path2Segments.length > index && comparison == 0) {
        String subpath1 = path1Segments[index];
        String subpath2 = path2Segments[index];
        comparison = subpath1.compareTo(subpath2);
        index++;
      }
    }

    return comparison;
  }
}
