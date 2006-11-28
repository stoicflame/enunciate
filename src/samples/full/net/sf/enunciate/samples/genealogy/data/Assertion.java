package net.sf.enunciate.samples.genealogy.data;

import net.sf.enunciate.samples.genealogy.cite.InfoSet;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * An assertion of a piece of information, usually associated with a source.
 *
 * @author Ryan Heaton
 */
public abstract class Assertion {

  private String id;
  private String note;
  private InfoSet infoSet;

  @XmlID
  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  @XmlIDREF
  public InfoSet getInfoSet() {
    return infoSet;
  }

  public void setInfoSet(InfoSet infoSet) {
    this.infoSet = infoSet;
  }
}
