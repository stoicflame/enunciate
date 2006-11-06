package net.sf.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class MultiAccessorTypeBean {

  private String id;
  private MultiAccessorTypeBean sibling;
  private Object specificType;
  private byte[] attachment;
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
  public byte[] getAttachment() {
    return attachment;
  }

  public void setAttachment(byte[] attachment) {
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
