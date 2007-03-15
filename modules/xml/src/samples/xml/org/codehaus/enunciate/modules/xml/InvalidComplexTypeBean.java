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

package org.codehaus.enunciate.modules.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class InvalidComplexTypeBean {

  private String attribute1;
  private short property1;
  private Collection<Double> doubles;

  @XmlAttribute (
    namespace = "urn:different"
  )
  public String getAttribute1() {
    return attribute1;
  }

  public void setAttribute1(String attribute1) {
    this.attribute1 = attribute1;
  }

  @XmlElement (
    namespace = "urn:different"
  )
  public short getProperty1() {
    return property1;
  }

  public void setProperty1(short property1) {
    this.property1 = property1;
  }

  @XmlElementWrapper (
    namespace = "urn:different"
  )
  public Collection<Double> getDoubles() {
    return doubles;
  }

  public void setDoubles(Collection<Double> doubles) {
    this.doubles = doubles;
  }

}
