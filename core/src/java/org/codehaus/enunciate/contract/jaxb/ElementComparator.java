/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.jaxb;

import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.bind.annotation.XmlAccessOrder;
import java.util.Comparator;

import com.sun.mirror.util.SourcePosition;

/**
 * A comparator for accessors.
 *
 * @author Ryan Heaton
 */
public class ElementComparator implements Comparator<Element> {

  private final XmlAccessOrder accessOrder;
  private final String[] propOrder;

  /**
   * Instantiate a new comparator, given the sorting parameters.
   *
   * @param propOrder The property order, or null if none is specified.
   * @param order     The accessor order.
   */
  public ElementComparator(String[] propOrder, XmlAccessOrder order) {
    this.accessOrder = order;
    this.propOrder = propOrder;
  }

  // Inherited.
  public int compare(Element accessor1, Element accessor2) {
    String propertyName1 = accessor1.getSimpleName();
    String propertyName2 = accessor2.getSimpleName();

    if (this.propOrder != null) {
      //apply the specified property order
      int propertyIndex1 = find(this.propOrder, propertyName1);
      int propertyIndex2 = find(this.propOrder, propertyName2);

      if (propertyIndex1 < 0) {
        throw new ValidationException(accessor1.getPosition(), "Property '" + propertyName1 + "' isn't included in the specified property order.");
      }
      if (propertyIndex2 < 0) {
        throw new ValidationException(accessor2.getPosition(), "Property '" + propertyName2 + "' isn't included in the specified property order.");
      }

      return propertyIndex1 - propertyIndex2;
    }
    else if (accessOrder == XmlAccessOrder.ALPHABETICAL) {
      return propertyName1.compareTo(propertyName2);
    }

    //If no order is specified, it's undefined. We'll put it in source order.
    SourcePosition position1 = accessor1.getPosition();
    SourcePosition position2 = accessor2.getPosition();
    int comparison = 0;
    if ((position1 != null) && (position2 != null)) {
      comparison = position1.line() - position2.line();
      if (comparison == 0) {
        comparison = accessor1.getPosition().column() - accessor2.getPosition().column();
      }
    }
    else {
      //no order is specified, no source position is available, we'll have to take a random comparison.
      comparison = accessor1.hashCode() - accessor2.hashCode();
    }

    return comparison;
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
