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
package com.webcohesion.enunciate.modules.jackson.model;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;

import java.util.Comparator;

/**
 * A comparator for accessors.
 *
 * @author Ryan Heaton
 */
public class MemberComparator implements Comparator<Member> {

  private final boolean alphabetical;
  private final String[] propOrder;
  private final DecoratedProcessingEnvironment env;

  public MemberComparator(String[] propOrder, boolean alphabetical, DecoratedProcessingEnvironment env) {
    this.alphabetical = alphabetical;
    this.propOrder = propOrder;
    this.env = env;
  }

  // Inherited.
  public int compare(Member accessor1, Member accessor2) {
    if (isSameId(accessor1, accessor2)) {
      //if the elements have the same identifier.
      return 0;
    }

    //they're not the same, so now determine relative order:

    String propertyName1 = accessor1.getSimpleName().toString();
    String propertyName2 = accessor2.getSimpleName().toString();

    if (this.propOrder != null && this.propOrder.length > 0) {
      //apply the specified property order
      int propertyIndex1 = find(this.propOrder, propertyName1);
      int propertyIndex2 = find(this.propOrder, propertyName2);

      if (propertyIndex1 < 0) {
        //not in the property list; just use the hash.
        propertyIndex1 = Math.abs(propertyName1.hashCode());
      }

      if (propertyIndex2 < 0) {
        //not in the property list; just use the hash.
        propertyIndex2 = Math.abs(propertyName2.hashCode());
      }

      return propertyIndex1 - propertyIndex2;
    }
    else if (this.alphabetical) {
      return propertyName1.compareTo(propertyName2);
    }


    //If no order is specified, it's undefined. We'll put it in source order.
    SourcePosition position1 = this.env.findSourcePosition(accessor1);
    SourcePosition position2 = this.env.findSourcePosition(accessor2);
    if (position1 != null && position2 != null) {
      return position1.compareTo(position2);
    }
    else {
      //don't have source positions... just provide a random sort order.
      return accessor1.hashCode() - accessor2.hashCode();
    }
  }

  private boolean isSameId(Member accessor1, Member accessor2) {
    return accessor1.getName().equals(accessor2.getName());
  }

  /**
   * Finds the order index of the specified property.
   *
   * @param propOrder    The property order.
   * @param propertyName The property name.
   * @return The order index of the specified property, or -1 if not found.
   */
  protected int find(String[] propOrder, String propertyName) {
    for (int i = 0; i < propOrder.length; i++) {
      if (propOrder[i].equalsIgnoreCase(propertyName)) {
        return i;
      }
    }

    return -1;
  }
}
