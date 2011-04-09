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

package org.codehaus.enunciate.examples.objc.schema.vehicles;

import org.codehaus.enunciate.examples.objc.schema.Figure;
import org.codehaus.enunciate.examples.objc.schema.Rectangle;
import org.codehaus.enunciate.examples.objc.schema.Circle;
import org.codehaus.enunciate.qname.XmlQNameEnumRef;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;

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

  @XmlQNameEnumRef(BusType.class)
  public QName getType() {
    return type;
  }

  public void setType(QName type) {
    this.type = type;
  }


}
