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
    int comparison = accessor1.getPosition().line() - accessor2.getPosition().line();
    if (comparison == 0) {
      comparison = accessor1.getPosition().column() - accessor2.getPosition().column();
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
