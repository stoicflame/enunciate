/*
 * Copyright 2006-2008 Web Cohesion
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

package org.codehaus.enunciate.examples.php.schema.structures;

import org.codehaus.enunciate.examples.php.schema.Rectangle;
import org.codehaus.enunciate.examples.php.schema.Triangle;
import org.codehaus.enunciate.examples.php.schema.Circle;
import org.codehaus.enunciate.examples.php.schema.Figure;
import org.codehaus.enunciate.qname.XmlQNameEnumRef;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class House extends Figure {

  private Rectangle base;
  private Triangle roof;
  private Rectangle door;
  private Circle doorKnob;
  private List<Rectangle> windows;
  private DateTime constructedDate;
  private QName type;
  private QName style;
  private URI color;

  @XmlElement (
    required = true
  )
  public Rectangle getBase() {
    return base;
  }

  public void setBase(Rectangle base) {
    this.base = base;
  }

  @XmlElement (
    nillable = true,
    required = true
  )
  public Triangle getRoof() {
    return roof;
  }

  public void setRoof(Triangle roof) {
    this.roof = roof;
  }

  public Rectangle getDoor() {
    return door;
  }

  public void setDoor(Rectangle door) {
    this.door = door;
  }

  public Circle getDoorKnob() {
    return doorKnob;
  }

  public void setDoorKnob(Circle doorKnob) {
    this.doorKnob = doorKnob;
  }

  @XmlElementWrapper (
    nillable = true
  )
  public List<Rectangle> getWindows() {
    return windows;
  }

  public void setWindows(List<Rectangle> windows) {
    this.windows = windows;
  }

  @XmlJavaTypeAdapter (DateTimeXmlAdapter.class)
  public DateTime getConstructedDate() {
    return constructedDate;
  }

  public void setConstructedDate(DateTime constructedDate) {
    this.constructedDate = constructedDate;
  }

  @XmlAttribute
  @XmlQNameEnumRef (HouseType.class)
  public QName getType() {
    return type;
  }

  public void setType(QName type) {
    this.type = type;
  }

  @XmlQNameEnumRef(HouseStyle.class)
  public QName getStyle() {
    return style;
  }

  public void setStyle(QName style) {
    this.style = style;
  }

  @XmlQNameEnumRef(HouseColor.class)
  public URI getColor() {
    return color;
  }

  public void setColor(URI color) {
    this.color = color;
  }
}
