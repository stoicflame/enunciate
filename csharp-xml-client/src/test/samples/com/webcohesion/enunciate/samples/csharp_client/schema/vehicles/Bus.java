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
package com.webcohesion.enunciate.samples.csharp_client.schema.vehicles;

import com.webcohesion.enunciate.samples.csharp_client.schema.Figure;
import com.webcohesion.enunciate.samples.csharp_client.schema.Rectangle;
import com.webcohesion.enunciate.samples.csharp_client.schema.Circle;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumRef;

import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Bus extends Figure {

  private Rectangle frame;
  private Circle[] wheels;
  private Collection<Rectangle> windows;
  private Rectangle door;
  private QName type;

  public Rectangle getFrame() {
    return frame;
  }

  public void setFrame(Rectangle frame) {
    this.frame = frame;
  }

  public Circle[] getWheels() {
    return wheels;
  }

  public void setWheels(Circle[] wheels) {
    this.wheels = wheels;
  }

  @XmlElementWrapper (
    name = "windows"
  )
  public Collection<Rectangle> getWindows() {
    return windows;
  }

  public void setWindows(Collection<Rectangle> windows) {
    this.windows = windows;
  }

  public Rectangle getDoor() {
    return door;
  }

  public void setDoor(Rectangle door) {
    this.door = door;
  }

  @XmlQNameEnumRef (BusType.class)
  public QName getType() {
    return type;
  }

  public void setType(QName type) {
    this.type = type;
  }
}
