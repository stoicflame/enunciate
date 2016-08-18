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
package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;

import java.util.Comparator;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ResourceComparator implements Comparator<Resource> {

  private final Comparator<String> pathComparator;

  public ResourceComparator(PathSortStrategy strategy) {
    if (strategy == PathSortStrategy.breadth_first) {
      pathComparator = new BreadthFirstResourcePathComparator();
    }
    else {
      pathComparator = new DepthFirstResourcePathComparator();
    }
  }

  @Override
  public int compare(Resource g1, Resource g2) {
    int compare = pathComparator.compare(g1.getPath(), g2.getPath());
    if (compare == 0) {
      List<? extends Method> m1 = g1.getMethods();
      List<? extends Method> m2 = g2.getMethods();
      if (m1.size() == 1 && m2.size() == 1) {
        compare = (m1.get(0).getHttpMethod().compareTo(m2.get(0).getHttpMethod()));
      }
    }
    return compare;
  }
}
