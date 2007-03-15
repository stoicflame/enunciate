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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Ryan Heaton
 */
@XmlType (
  namespace = "urn:attributebean"
)
public class AttributeBean {

  private String property1;
  private int property2;
  private boolean property3;

  @XmlAttribute (
    namespace = "urn:other"
  )
  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

  @XmlAttribute (
    name="dummyname"
  )
  public int getProperty2() {
    return property2;
  }

  public void setProperty2(int property2) {
    this.property2 = property2;
  }

  @XmlAttribute (
    required = true
  )
  public boolean isProperty3() {
    return property3;
  }

  public void setProperty3(boolean property3) {
    this.property3 = property3;
  }

}
