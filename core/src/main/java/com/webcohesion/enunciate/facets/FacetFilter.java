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
package com.webcohesion.enunciate.facets;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class FacetFilter {

  private final Set<String> includes;
  private final Set<String> excludes;

  public FacetFilter(Set<String> includes, Set<String> excludes) {
    this.includes = includes;
    this.excludes = excludes;
  }

  public boolean accept(HasFacets item) {
    if (item == null) {
      return false;
    }

    if ((includes == null || includes.isEmpty()) && (excludes == null || excludes.isEmpty())) {
      return true;
    }

    boolean accept = true;
    if (includes != null && !includes.isEmpty()) {
      boolean included = false;
      for (Facet facet : item.getFacets()) {
        if (includes.contains(facet.getName())) {
          included = true;
          break;
        }
      }
      accept = included;
    }

    //then remove the items that are explicitly excluded.
    if (excludes != null && !excludes.isEmpty()) {
      for (Facet facet : item.getFacets()) {
        if (excludes.contains(facet.getName())) {
          accept = false;
          break;
        }
      }
    }

    return accept;
  }

}
