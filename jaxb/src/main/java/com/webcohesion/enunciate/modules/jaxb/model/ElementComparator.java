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
package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.namespace.QName;
import java.util.Comparator;

/**
 * A comparator for accessors.
 *
 * @author Ryan Heaton
 */
public class ElementComparator implements Comparator<Element> {

  private final XmlAccessOrder accessOrder;
  private final String[] propOrder;
  private final DecoratedProcessingEnvironment env;

  /**
   * Instantiate a new comparator, given the sorting parameters.
   *
   * @param propOrder The property order, or null if none is specified.
   * @param order     The accessor order.
   * @param env       Processing environment.
   */
  public ElementComparator(String[] propOrder, XmlAccessOrder order, DecoratedProcessingEnvironment env) {
    this.accessOrder = order;
    this.propOrder = propOrder;
    this.env = env;
  }

  // Inherited.
  public int compare(Element accessor1, Element accessor2) {
    if (isSameId(accessor1, accessor2)) {
      //if the elements have the same identifier.
      return 0;
    }

    //they're not the same, so now determine relative order:

    String propertyName1 = accessor1.getSimpleName().toString();
    String propertyName2 = accessor2.getSimpleName().toString();

    if (this.propOrder != null) {
      //apply the specified property order
      int propertyIndex1 = find(this.propOrder, propertyName1);
      int propertyIndex2 = find(this.propOrder, propertyName2);

      if (propertyIndex1 < 0) {
        throw new EnunciateException("Property '" + propertyName1 + "' isn't included in the specified property order.");
      }
      if (propertyIndex2 < 0) {
        throw new EnunciateException("Property '" + propertyName2 + "' isn't included in the specified property order.");
      }

      return propertyIndex1 - propertyIndex2;
    }
    else if (accessOrder == XmlAccessOrder.ALPHABETICAL) {
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

  private boolean isSameId(Element accessor1, Element accessor2) {
    if (accessor1.isWrapped() != accessor2.isWrapped()) {
      return false;
    }

    if (accessor1.isWrapped() && accessor2.isWrapped()) {
      String name1 = accessor1.getWrapperName();
      name1 = name1 == null ? "" : name1;
      String name2 = accessor2.getWrapperName();
      name2 = name2 == null ? "" : name2;
      String ns1 = accessor1.getWrapperNamespace();
      ns1 = ns1 == null ? "" : ns1;
      String ns2 = accessor2.getWrapperNamespace();
      ns2 = ns2 == null ? "" : ns2;
      return name1.equals(name2) && ns1.equals(ns2);
    }

    if (accessor1.isElementRefs() || accessor2.isElementRefs()) {
      //bag of element refs are assumed to not have the same id.
      return false;
    }

    QName ref1 = accessor1.getRef();
    QName ref2 = accessor2.getRef();
    if (ref1 != null && ref2 != null && ref1.equals(ref2)) {
      return true;
    }

    String name1 = accessor1.getName();
    name1 = name1 == null ? "" : name1;
    String name2 = accessor2.getName();
    name2 = name2 == null ? "" : name2;
    String ns1 = accessor1.getNamespace();
    ns1 = ns1 == null ? "" : ns1;
    String ns2 = accessor2.getNamespace();
    ns2 = ns2 == null ? "" : ns2;
    return name1.equals(name2) && ns1.equals(ns2);
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
