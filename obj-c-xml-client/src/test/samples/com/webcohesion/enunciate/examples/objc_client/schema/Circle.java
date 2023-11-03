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
package com.webcohesion.enunciate.examples.objc_client.schema;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
@XmlType ( propOrder = {"radius", "dots", "stars", "palette"} )
public class Circle extends Shape {

  private int radius;
  private List<Dot> dots;
  private List<Dot> stars;
  private Color[] palette;

  public int getRadius() {
    return radius;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }

  @XmlElementWrapper(name="dots")
  @XmlElement(name="dot")
  public List<Dot> getDots() {
    return dots;
  }

  public void setDots(List<Dot> dots) {
    this.dots = dots;
  }

  @XmlElementWrapper(name="stars")
  @XmlElement(name="star")
  public List<Dot> getStars() {
    return stars;
  }

  public void setStars(List<Dot> stars) {
    this.stars = stars;
  }

  public Color[] getPalette() {
    return palette;
  }

  public void setPalette(Color[] palette) {
    this.palette = palette;
  }
}
