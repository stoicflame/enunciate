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

package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlValue;

/**
 * @author Ryan Heaton
 */
public class ComplexTypeWithValueAndElements {

  private String value;
  private int element1;
  private boolean element2;

  @XmlValue
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public int getElement1() {
    return element1;
  }

  public void setElement1(int element1) {
    this.element1 = element1;
  }

  public boolean isElement2() {
    return element2;
  }

  public void setElement2(boolean element2) {
    this.element2 = element2;
  }

  public class NestedNotAType {

    private String nestedProperty;

    public String getNestedProperty() {
      return nestedProperty;
    }

    public void setNestedProperty(String nestedProperty) {
      this.nestedProperty = nestedProperty;
    }
  }
}
