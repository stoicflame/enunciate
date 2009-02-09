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

package org.codehaus.enunciate.examples.xfire_client.schema.draw;

import org.codehaus.enunciate.examples.xfire_client.schema.structures.House;
import org.codehaus.enunciate.examples.xfire_client.schema.animals.Cat;
import org.codehaus.enunciate.examples.xfire_client.schema.vehicles.Bus;
import org.codehaus.enunciate.examples.xfire_client.schema.*;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class Canvas {

  private Collection figures;
  private Collection shapes;
  private Collection<Line> lines;
  private int dimensionX;
  private int dimensionY;
  private DataHandler backgroundImage;

  private byte[] explicitBase64Attachment;
  private Collection<CanvasAttachment> otherAttachments;

  @XmlElementRefs (
    {
      @XmlElementRef ( type = Circle.class ),
      @XmlElementRef ( type = Rectangle.class ),
      @XmlElementRef ( type = Triangle.class )
    }
  )
  public Collection getShapes() {
    return shapes;
  }

  public void setShapes(Collection shapes) {
    this.shapes = shapes;
  }

  @XmlElements (
    {
      @XmlElement ( name="cat", type = Cat.class ),
      @XmlElement ( name="house", type = House.class ),
      @XmlElement ( name="bus", type = Bus.class )
    }
  )
  public Collection getFigures() {
    return figures;
  }

  public void setFigures(Collection figures) {
    this.figures = figures;
  }

  public Collection<Line> getLines() {
    return lines;
  }

  public void setLines(Collection<Line> lines) {
    this.lines = lines;
  }

  public int getDimensionX() {
    return dimensionX;
  }

  public void setDimensionX(int dimensionX) {
    this.dimensionX = dimensionX;
  }

  public int getDimensionY() {
    return dimensionY;
  }

  public void setDimensionY(int dimensionY) {
    this.dimensionY = dimensionY;
  }

  @XmlAttachmentRef
  public DataHandler getBackgroundImage() {
    return backgroundImage;
  }

  public void setBackgroundImage(DataHandler backgroundImage) {
    this.backgroundImage = backgroundImage;
  }

  @XmlInlineBinaryData
  public byte[] getExplicitBase64Attachment() {
    return explicitBase64Attachment;
  }

  public void setExplicitBase64Attachment(byte[] explicitBase64Attachment) {
    this.explicitBase64Attachment = explicitBase64Attachment;
  }

  @XmlElementWrapper (
    name = "otherAttachments"
  )
  @XmlElement (
    name = "attachment"
  )
  public Collection<CanvasAttachment> getOtherAttachments() {
    return otherAttachments;
  }

  public void setOtherAttachments(Collection<CanvasAttachment> otherAttachments) {
    this.otherAttachments = otherAttachments;
  }
}
