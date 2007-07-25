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

package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.fault.XFireFault;

import java.beans.PropertyDescriptor;

/**
 * Utilities for properties.
 *
 * @author Ryan Heaton
 */
public class PropertyUtil {

  private PropertyUtil() {
  }

  /**
   * Sorts the properties of a given class according to a specified order.
   *
   * @param propertyClass The class that should have the properties.
   * @param pds The property descriptors.
   * @param propOrder The order of the properties.
   * @return The sorted properties.
   */
  public static PropertyDescriptor[] sortProperties(Class propertyClass, PropertyDescriptor[] pds, String[] propOrder) throws XFireFault {
    PropertyDescriptor[] outputProperties = new PropertyDescriptor[propOrder.length];
    RESPONSE_PROPERTY_LOOP:
    for (int i = 0; i < propOrder.length; i++) {
      String property = propOrder[i];
      if ((property.length() > 1) && (!Character.isLowerCase(property.charAt(1)))) {
        //if the second letter is uppercase, javabean spec says the first character of the property is also to be kept uppercase.
        property = capitalize(property);
      }

      for (int j = 0; j < pds.length; j++) {
        PropertyDescriptor descriptor = pds[j];
        if (descriptor.getName().equals(property)) {
          outputProperties[i] = descriptor;
          continue RESPONSE_PROPERTY_LOOP;
        }
      }

      throw new XFireFault("Unknown property " + property + " on wrapper " + propertyClass.getName(), XFireFault.RECEIVER);
    }
    return outputProperties;
  }

  
  /**
   * Capitalizes a string.
   *
   * @param string The string to capitalize.
   * @return The capitalized value.
   */
  public static String capitalize(String string) {
    return Character.toString(string.charAt(0)).toUpperCase() + string.substring(1);
  }

}
