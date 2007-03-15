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

package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.activation.DataHandler;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class MultiAccessorTypeBean {

  private String id;
  private MultiAccessorTypeBean sibling;
  private Object specificType;
  private DataHandler attachment;
  private int simple;
  private Collection<Long> simples;

  @XmlID
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @XmlIDREF
  public MultiAccessorTypeBean getSibling() {
    return sibling;
  }

  public void setSibling(MultiAccessorTypeBean sibling) {
    this.sibling = sibling;
  }

  @XmlSchemaType (
    name = "integer"
  )
  public Object getSpecificType() {
    return specificType;
  }

  public void setSpecificType(Object specificType) {
    this.specificType = specificType;
  }

  @XmlAttachmentRef
  public DataHandler getAttachment() {
    return attachment;
  }

  public void setAttachment(DataHandler attachment) {
    this.attachment = attachment;
  }

  public int getSimple() {
    return simple;
  }

  public void setSimple(int simple) {
    this.simple = simple;
  }

  public Collection<Long> getSimples() {
    return simples;
  }

  public void setSimples(Collection<Long> simples) {
    this.simples = simples;
  }
}
