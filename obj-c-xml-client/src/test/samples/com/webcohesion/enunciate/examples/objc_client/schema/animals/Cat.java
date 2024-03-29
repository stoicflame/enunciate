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
package com.webcohesion.enunciate.examples.objc_client.schema.animals;

import com.webcohesion.enunciate.examples.objc_client.schema.Circle;
import com.webcohesion.enunciate.examples.objc_client.schema.Figure;
import com.webcohesion.enunciate.examples.objc_client.schema.Line;
import com.webcohesion.enunciate.examples.objc_client.schema.Triangle;

import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Cat extends Figure {

  private Circle face;
  private Triangle[] eyes;
  private Collection<Triangle> ears;
  private Line nose;
  private Collection<Line> whiskers;
  private Line mouth;

  @XmlAttribute
  @XmlIDREF
  public Line getMouth() {
    return mouth;
  }

  public void setMouth(Line mouth) {
    this.mouth = mouth;
  }

  @XmlElementRef
  public Circle getFace() {
    return face;
  }

  public void setFace(Circle face) {
    this.face = face;
  }

  public Triangle[] getEyes() {
    return eyes;
  }

  public void setEyes(Triangle[] eyes) {
    this.eyes = eyes;
  }

  @XmlIDREF
  public Collection<Triangle> getEars() {
    return ears;
  }

  public void setEars(Collection<Triangle> ears) {
    this.ears = ears;
  }

  @XmlIDREF
  public Line getNose() {
    return nose;
  }

  public void setNose(Line nose) {
    this.nose = nose;
  }

  public Collection<Line> getWhiskers() {
    return whiskers;
  }

  public void setWhiskers(Collection<Line> whiskers) {
    this.whiskers = whiskers;
  }
}
