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

import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;

import java.util.Comparator;

/**
 * Sorts {@link org.codehaus.enunciate.contract.jaxrs.ResourceMethod}s by path, then alphabetically.
 *
 * @author Ryan Heaton
 */
public class ResourceMethodComparator implements Comparator<ResourceMethod> {

  public int compare(ResourceMethod method1, ResourceMethod method2) {
    String[] path1Segments = method1.getFullpath().split("/");
    String[] path2Segments = method2.getFullpath().split("/");
    int index = 0;
    while (path1Segments.length > index && path2Segments.length > index) {
      String subpath1 = path1Segments[index];
      String subpath2 = path2Segments[index];
      int comparison = subpath1.compareTo(subpath2);
      if (comparison != 0) {
        return comparison;
      }
      index++;
    }

    return path1Segments.length - path2Segments.length;
  }
}
